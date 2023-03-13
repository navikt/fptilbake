package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

class FinnGrunnlagTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository;

    private FinnGrunnlagTask finnGrunnlagTask;

    private String saksnummer;
    private static final Long FPSAK_ANNEN_BEHANDLING_ID = 1174551L;
    private static final Henvisning ANNEN_HENVISNING = Henvisning.fraEksternBehandlingId(FPSAK_ANNEN_BEHANDLING_ID);

    @BeforeEach
    void setup() {
        kravVedtakStatusRepository = new KravVedtakStatusRepository(entityManager);
        HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider,
                taskTjeneste, behandlingskontrollTjeneste, historikkinnslagTjeneste);
        KravVedtakStatusTjeneste kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository,
                taskTjeneste, repositoryProvider, henleggBehandlingTjeneste,
                behandlingskontrollTjeneste);
        KravVedtakStatusMapper kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);
        finnGrunnlagTask = new FinnGrunnlagTask(repositoryProvider, mottattXmlRepository,
                kravVedtakStatusTjeneste, behandlingskontrollTjeneste, kravVedtakStatusMapper, kravgrunnlagMapper, fagsystemKlientMock);

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, HENVISNING, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
        saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.HENTGRUNNLAGSTEG,
                LocalDateTime.now().plusDays(10), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }

    @Test
    void skal_finne_og_koble_ny_grunnlag_til_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertTilkobling();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    @Test
    void skal_finne_og_koble_endre_grunnlag_til_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandling.getId());
        assertThat(kravgrunnlag431.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.NYTT);
        assertTilkobling();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    @Test
    void skal_finne_og_håndtere_avsl_melding_for_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.erAvsluttet()).isTrue();
        Optional<Behandlingsresultat> behandlingsresultat = repositoryProvider.getBehandlingresultatRepository().hent(behandling);
        assertThat(behandlingsresultat).isNotEmpty();
        assertThat(behandlingsresultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertTilkobling();
        assertThat(behandling.erAvsluttet()).isTrue();
    }

    @Test
    void skal_finne_og_håndtere_sper_melding_for_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);
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
    void skal_finne_og_håndtere_ny_grunnlag_når_kravgrunnlag_finnes_med_samme_saksnummer_men_annen_ekstern_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR_samme_referanse.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, mottattXmlId);

        mockFagsystemKlientRespons();

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        Optional<ØkonomiXmlMottatt> økonomiXmlMottatt = mottattXmlRepository.finnForHenvisning(ANNEN_HENVISNING);
        assertThat(økonomiXmlMottatt).isPresent();
        assertThat(økonomiXmlMottatt.get().isTilkoblet()).isTrue();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(eksternBehandlingRepository.hentFraHenvisning(ANNEN_HENVISNING)).isNotEmpty();
    }

    @Test
    void skal_finne_og_håndtere_ny_grunnlag_når_kravgrunnlag_finnes_med_samme_saksnummer_men_annen_ekstern_behandling_etter_første_grunnlaget_er_avsluttet() {
        Long førsteGrunnlagXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, førsteGrunnlagXmlId);

        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        Long andreGrunnlagXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR_samme_referanse.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, andreGrunnlagXmlId);

        mockFagsystemKlientRespons();

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(førsteGrunnlagXmlId)).isTrue();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(eksternBehandlingRepository.hentFraHenvisning(ANNEN_HENVISNING)).isNotEmpty();

        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(andreGrunnlagXmlId)).isTrue();
        assertThat(eksternBehandlingRepository.hentFraHenvisning(HENVISNING)).isEmpty();
        assertThat(behandling.erAvsluttet()).isFalse();
    }


    @Test
    void skal_finne_og_håndtere_grunnlag_og_oppdatere_fpsak_referanse_når_kravgrunnlag_referanse_er_forskjellige_enn_fpsak_referanse() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR_samme_referanse.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, mottattXmlId);

        mockFagsystemKlientRespons();
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        Optional<ØkonomiXmlMottatt> økonomiXmlMottatt = mottattXmlRepository.finnForHenvisning(ANNEN_HENVISNING);
        assertThat(økonomiXmlMottatt).isPresent();
        assertThat(økonomiXmlMottatt.get().isTilkoblet()).isTrue();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(eksternBehandlingRepository.hentFraHenvisning(ANNEN_HENVISNING)).isNotEmpty();
    }

    @Test
    void skal_ikke_finne_og_håndtere_grunnlag_når_kravgrunnlag_har_feil_referanse() {
        EksternBehandlingsinfoDto førsteVedtak = new EksternBehandlingsinfoDto();
        førsteVedtak.setHenvisning(HENVISNING);
        førsteVedtak.setUuid(FPSAK_BEHANDLING_UUID);
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(saksnummer)).thenReturn(Lists.newArrayList(førsteVedtak));

        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR_samme_referanse.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        var e = assertThrows(TekniskException.class, () -> finnGrunnlagTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-783524");
    }

    @Test
    void skal_ikke_finne_og_håndtere_sper_melding_for_behandling_når_grunnlag_xml_ikke_finnes() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isFalse();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();
    }

    @Test
    void skal_ikke_finne_og_håndtere_endr_melding_for_behandling_når_grunnlag_xml_ikke_finnes() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isFalse();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();
    }

    @Test
    void skal_finne_og_håndtere_endr_melding_for_behandling_når_grunnlag_er_sperret() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTasker = captor.getAllValues();
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).taskType()).isEqualTo(TaskType.forProsessTask(FortsettBehandlingTask.class));
    }

    @Test
    void skal_ikke_finne_og_håndtere_endr_melding_for_behandling_når_xml_rekkefølge_ikke_riktig() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);


        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        assertThatThrownBy(() -> finnGrunnlagTask.doTask(prosessTaskData))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FPT-107929");
    }

    @Test
    void skal_finne_og_håndtere_flere_endr_meldinger_for_en_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR_samme_referanse.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_ENDR.xml"));
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(ANNEN_HENVISNING, saksnummer, mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(grunnlagRepository.erKravgrunnlagSperret(behandling.getId())).isFalse();

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(2)).lagre(captor.capture());
        var prosessTasker = captor.getAllValues();
        assertThat(prosessTasker.get(0).taskType()).isEqualTo(TaskType.forProsessTask(FortsettBehandlingTask.class));
        assertThat(prosessTasker.get(1).taskType()).isEqualTo(TaskType.forProsessTask(FortsettBehandlingTask.class));
    }

    private ProsessTaskData opprettFinngrunnlagProsessTask() {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(FinnGrunnlagTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        return prosessTaskData;
    }

    private void assertTilkobling() {
        List<ØkonomiXmlMottatt> xmlMeldinger = mottattXmlRepository.finnAlleForSaksnummerSomIkkeErKoblet(fagsak.getSaksnummer().toString());
        assertThat(xmlMeldinger).isEmpty();
    }

    private void mockFagsystemKlientRespons() {
        EksternBehandlingsinfoDto førsteVedtak = new EksternBehandlingsinfoDto();
        førsteVedtak.setHenvisning(HENVISNING);
        førsteVedtak.setUuid(FPSAK_BEHANDLING_UUID);

        EksternBehandlingsinfoDto andreVedtak = new EksternBehandlingsinfoDto();
        førsteVedtak.setHenvisning(ANNEN_HENVISNING);
        andreVedtak.setUuid(UUID.randomUUID());

        when(fagsystemKlientMock.hentBehandlingForSaksnummer(saksnummer)).thenReturn(Lists.newArrayList(førsteVedtak, andreVedtak));
    }

}
