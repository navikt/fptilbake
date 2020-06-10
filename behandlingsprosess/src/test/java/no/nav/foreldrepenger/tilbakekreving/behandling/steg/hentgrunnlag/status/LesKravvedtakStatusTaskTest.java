package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class LesKravvedtakStatusTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository = new KravVedtakStatusRepository(repoRule.getEntityManager());
    private BehandlingresultatRepository behandlingresultatRepository = new BehandlingresultatRepositoryImpl(repoRule.getEntityManager());

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, prosessTaskRepository, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository, prosessTaskRepository, repositoryProvider, henleggBehandlingTjeneste, behandlingskontrollTjeneste);
    private KravVedtakStatusMapper kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);
    private LesKravvedtakStatusTask lesKravvedtakStatusTask = new LesKravvedtakStatusTask(mottattXmlRepository, repositoryProvider,        kravVedtakStatusTjeneste, kravVedtakStatusMapper, fpsakKlientMock);

    private Long mottattXmlId;

    //TODO bør ikke bruke samme navn som i parent-klassens konstanter, det skaper lett forvirring
    private static final Long FPSAK_BEHANDLING_ID = 1174551l;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID);

    @Before
    public void setup() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, HENVISNING, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_sper_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(mottattXmlRepository.finnForHenvisning(HENVISNING)).isPresent();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));
        assertTilkobling();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_manu_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_MANU.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(mottattXmlRepository.finnForHenvisning(HENVISNING)).isPresent();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.MANUELL));
        assertTilkobling();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_avsl_melding_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(mottattXmlRepository.finnForHenvisning(HENVISNING)).isPresent();
        assertThat(behandling.erAvsluttet()).isTrue();
        Optional<Behandlingsresultat> resultat = behandlingresultatRepository.hent(behandling);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.AVSLUTTET));

        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandling.getId());
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
        assertTilkobling();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_mottatt_ugyldig_status_melding() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigstatus.xml")); // den xml-en har ugyldig status kode

        expectedException.expectMessage("FPT-107928");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_finnes_ikke_iFpsak() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fpsakKlientMock.finnesBehandlingIFpsak(anyString(), any(Henvisning.class))).thenReturn(false);

        expectedException.expectMessage("FPT-587196");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_når_fptilbake_har_ingen_åpenBehandling() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldig.xml"));
        when(fpsakKlientMock.finnesBehandlingIFpsak(anyString(), any(Henvisning.class))).thenReturn(true);

        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEmpty();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_er_ugyldig() {
        // den xml-en har behandlngId som finnes ikke i EksternBehandling
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ugyldigreferanse.xml"));

        expectedException.expectMessage("Mottok et kravOgVedtakStatus fra Økonomi med henvisning i ikke-støttet format, henvisning=ABC. KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!");
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag() {
        repoRule.getEntityManager().createQuery("delete from EksternBehandling").executeUpdate();
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(100000001L), FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        assertThat(mottattXmlRepository.finnForHenvisning(HENVISNING)).isPresent();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));

        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
        assertTilkobling();
    }

    @Test
    public void skal_utføre_leskravvedtakstatus_task_for_behandling_som_allerede_har_grunnlag_med_samme_referanse() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        List<ØkonomiXmlMottatt> xmlMottatt = finnAlleForHenvisning(HENVISNING);
        assertThat(xmlMottatt.size()).isEqualTo(2);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)).isNotNull();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.SPERRET));
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isTrue();
    }

    @Test
    public void skal_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_med_gyldig_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isEqualTo(HENVISNING);

        List<ØkonomiXmlMottatt> xmlMottatt = finnAlleForHenvisning(HENVISNING);
        assertThat(xmlMottatt.size()).isEqualTo(3);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEqualTo(Optional.of(KravStatusKode.ENDRET));
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();

        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(FortsettBehandlingTaskProperties.TASKTYPE);
    }

    @Test
    public void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_når_grunnlag_ikke_finnes() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-107929");

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_ikke_utføre_leskravvedtakststatustask_for_mottatt_endr_melding_når_grunnlag_ikke_sperret() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-107929");

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR_samme_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));
    }

    @Test
    public void skal_håndtere_sper_melding_når_siste_ekstern_behandling_har_henlagt_behandling() {
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_FEIL_samme_referanse.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData(mottattXmlId, LesKravgrunnlagTask.TASKTYPE));
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), BehandlingResultatType.HENLAGT_FEILOPPRETTET);

        when(fpsakKlientMock.finnesBehandlingIFpsak(anyString(), any(Henvisning.class))).thenReturn(true);
        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER_annen_referanse.xml"));
        lesKravvedtakStatusTask.doTask(lagProsessTaskData(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE));

        Optional<ØkonomiXmlMottatt> xmlMottatt = mottattXmlRepository.finnForHenvisning(Henvisning.fraEksternBehandlingId(100000001L));
        assertThat(xmlMottatt).isPresent();
        assertThat(xmlMottatt.get().getSaksnummer()).isEqualTo("139015144");
        assertThat(xmlMottatt.get().isTilkoblet()).isFalse();
    }

    private void assertTilkobling() {
        Optional<ØkonomiXmlMottatt> økonomiXmlMottatt = mottattXmlRepository.finnForHenvisning(HENVISNING);
        assertThat(økonomiXmlMottatt).isPresent();
        assertThat(økonomiXmlMottatt.get().isTilkoblet()).isTrue();
    }

    public List<ØkonomiXmlMottatt> finnAlleForHenvisning(Henvisning henvisning) {
        TypedQuery<ØkonomiXmlMottatt> query = repoRule.getEntityManager().createQuery("from ØkonomiXmlMottatt where henvisning=:henvisning", ØkonomiXmlMottatt.class);
        query.setParameter("henvisning", henvisning);
        return query.getResultList();
    }

}
