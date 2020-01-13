package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabellRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.util.FPDateUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FinnGrunnlagTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository = new KravVedtakStatusRepository(repoRule.getEntityManager());
    private KodeverkTabellRepository kodeverkTabellRepository = new KodeverkTabellRepositoryImpl(repoRule.getEntityManager());

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository, prosessTaskRepository, repositoryProvider, henleggBehandlingTjeneste, behandlingskontrollTjeneste);
    private KravVedtakStatusMapper kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);

    private FinnGrunnlagTask finnGrunnlagTask = new FinnGrunnlagTask(repositoryProvider, mottattXmlRepository, kodeverkTabellRepository, kravVedtakStatusTjeneste, behandlingskontrollTjeneste, kravVedtakStatusMapper, kravgrunnlagMapper);

    private String saksnummer;

    @Before
    public void setup() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
        saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG,
            FPDateUtil.nå().plusDays(10), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }

    @Test
    public void skal_finne_og_koble_ny_grunnlag_til_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertTilkobling();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    @Test
    public void skal_finne_og_koble_endre_grunnlag_til_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandling.getId());
        assertThat(kravgrunnlag431.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.NYTT);
        assertTilkobling();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    @Test
    public void skal_finne_og_håndtere_avsl_melding_for_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.erAvsluttet()).isTrue();
        Optional<Behandlingsresultat> behandlingsresultat = repositoryProvider.getBehandlingresultatRepository().hent(behandling);
        assertThat(behandlingsresultat).isNotEmpty();
        assertThat(behandlingsresultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertTilkobling();
    }

    @Test
    public void skal_finne_og_håndtere_sper_melding_for_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
        assertTilkobling();
    }

    @Test
    public void skal_finne_og_håndtere_ny_grunnlag_når_kravgrunnlag_finnes_med_samme_saksnummer_men_annen_ekstern_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR_samme_referanse.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(1174551), saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        Optional<ØkonomiXmlMottatt> økonomiXmlMottatt = mottattXmlRepository.finnForEksternBehandlingId(String.valueOf(1174551));
        assertThat(økonomiXmlMottatt).isPresent();
        assertThat(økonomiXmlMottatt.get().isTilkoblet()).isTrue();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    @Test
    public void skal_ikke_finne_og_håndtere_sper_melding_for_behandling_når_grunnlag_xml_ikke_finnes() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isFalse();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();
    }

    @Test
    public void skal_ikke_finne_og_håndtere_endr_melding_for_behandling_når_grunnlag_xml_ikke_finnes() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isFalse();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();
    }

    @Test
    public void skal_finne_og_håndtere_endr_melding_for_behandling_når_grunnlag_er_sperret() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();

        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(FortsettBehandlingTaskProperties.TASKTYPE);
    }

    @Test
    public void skal_ikke_finne_og_håndtere_endr_melding_for_behandling_når_xml_rekkefølge_ikke_riktig() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingIdOgSaksnummer(String.valueOf(FPSAK_BEHANDLING_ID), saksnummer, mottattXmlId);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-107929");

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

    }

    private ProsessTaskData opprettFinngrunnlagProsessTask() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(FinnGrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        return prosessTaskData;
    }

    private void assertTilkobling() {
        List<ØkonomiXmlMottatt> xmlMeldinger = mottattXmlRepository.finnAlleForSaksnummerSomIkkeErKoblet(String.valueOf(FPSAK_BEHANDLING_ID));
        assertThat(xmlMeldinger).isEmpty();
    }

}
