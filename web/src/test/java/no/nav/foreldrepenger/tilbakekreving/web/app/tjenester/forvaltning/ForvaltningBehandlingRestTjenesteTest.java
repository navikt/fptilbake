package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.KobleBehandlingTilGrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.KorrigertHenvisningDto;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@CdiDbAwareTest
class ForvaltningBehandlingRestTjenesteTest {

    @Inject
    EntityManager entityManager;
    @Inject
    BehandlingRepositoryProvider repositoryProvider;
    @Inject
    BehandlingRepository behandlingRepository;
    @Inject
    EksternBehandlingRepository eksternBehandlingRepository;
    @Inject
    BehandlingresultatRepository behandlingresultatRepository;
    @Inject
    ØkonomiMottattXmlRepository mottattXmlRepository;
    @Inject
    HistorikkinnslagRepository historikkRepository;
    @Inject
    KravgrunnlagTjeneste kravgrunnlagTjeneste;
    @Inject
    BehandlingModellRepository behandlingModellRepository;

    ProsessTaskTjeneste taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
    PersonOrganisasjonWrapper mockTpsAdapterWrapper = Mockito.mock(PersonOrganisasjonWrapper.class);

    KravgrunnlagMapper kravgrunnlagMapper = new KravgrunnlagMapper(mockTpsAdapterWrapper);

    BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    ForvaltningBehandlingRestTjeneste forvaltningBehandlingRestTjeneste;

    ScenarioSimple scenario = ScenarioSimple.simple();
    Behandling behandling;

    @BeforeEach
    void setup() {
        BehandlingskontrollEventPubliserer eventPubliserer = mock(BehandlingskontrollEventPubliserer.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, behandlingModellRepository, eventPubliserer));

        forvaltningBehandlingRestTjeneste = new ForvaltningBehandlingRestTjeneste(repositoryProvider, taskTjeneste, behandlingresultatRepository,
            mottattXmlRepository, kravgrunnlagMapper, kravgrunnlagTjeneste, eksternBehandlingRepository, null, null);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    void skal_ikke_tvinge_henlegg_behandling_når_behandling_er_allerede_avsluttet() {
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_tvinge_henlegg_behandling() {
        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertProsessTask(TaskType.forProsessTask(TvingHenlegglBehandlingTask.class));
    }

    @Test
    void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_er_avsluttet() {
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_ikke_er_på_vent() {
        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_tvinge_gjenoppta_behandling() {
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTaskData = captor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(FortsettBehandlingTask.class));
        assertThat(prosessTaskData.getPropertyValue(FortsettBehandlingTask.MANUELL_FORTSETTELSE)).isEqualTo("true");

        //for klønete å sette behandlingStegTilstander til å legge til:
        //assertThat(prosessTaskData.getPropertyValue(FortsettBehandlingTask.GJENOPPTA_STEG)).isEqualTo(BehandlingStegType.TBKGSTEG.getKode());
    }

    @Test
    void skal_ikke_tvinge_koble_grunnlag_når_behandling_er_ikke_på_vent() {
        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), 1l));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_ikke_tvinge_koble_grunnlag_når_behandling_er_ikke_på_vent_på_tilbakekrevingsgrunnlag() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), 1l));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_ikke_tvinge_koble_grunnlag_når_mottattXml_er_status_melding() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?><urn:endringKravOgVedtakstatus xmlns:urn=\"urn:no:nav:tilbakekreving:status:v1\"/>");

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_ikke_tvinge_koble_grunnlag_når_mottattXml_er_allerede_koblet() {
        //kravgrunnlag ankommer
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(true));
        //behandling 1 opprettes og kobles
        Behandling kobletBehandling = opprettBehandling(this.behandling.getFagsak());
        Henvisning henvisning = Henvisning.fraEksternBehandlingId(100000001L);
        eksternBehandlingRepository.lagre(new EksternBehandling(kobletBehandling, henvisning, UUID.randomUUID()));
        mottattXmlRepository.opprettTilkobling(mottattXmlId);
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, behandling.getFagsak().getSaksnummer().getVerdi(), mottattXmlId);

        //behandling 2 er på vent
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        //forsøk å koble kravgrunnlag
        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(this.behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void skal_kunne_tvinge_koble_grunnlag_når_mottattXml_er_koblet_til_en_henlagt_behandling() {
        when(mockTpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(anyString(), any(GjelderType.class))).thenReturn(
            "123");

        // Les kravgrunnlag og opprett kobling
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(true));
        mottattXmlRepository.opprettTilkobling(mottattXmlId);

        // Lagre henvisning og saksnummer
        var henvisning = Henvisning.fraEksternBehandlingId(123455L);
        mottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, behandling.getFagsak().getSaksnummer().getVerdi(), mottattXmlId);
        eksternBehandlingRepository.lagre(new EksternBehandling(behandling, henvisning, UUID.randomUUID()));

        // Henlegg original behandling
        behandlingresultatRepository.lagre(Behandlingsresultat.builder().medBehandling(behandling).medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET).build());
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.henleggBehandling(kontekst, BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD);

        // Opprett ny behandling og sett på vent
        var nyBehandling = opprettBehandling(behandling.getFagsak());
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(nyBehandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(nyBehandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    void skal_tvinge_koble_grunnlag_når_mottattXml_er_grunnlag() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(true));

        when(mockTpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(anyString(), any(GjelderType.class))).thenReturn(
            "123");

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(repositoryProvider.getGrunnlagRepository().harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isTrue();
    }

    @Test
    void skal_ikke_tvinge_koble_grunnlag_når_kravgrunnlaget_er_ugyldig() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(false));

        when(mockTpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(anyString(), any(GjelderType.class))).thenReturn(
            "123");

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(repositoryProvider.getGrunnlagRepository().harGrunnlagForBehandlingId(behandling.getId())).isFalse();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isFalse();
    }

    @Test
    void skal_opprette_prosess_tasken_når_henvisning_korrigeres() {
        UUID eksternUuid = UUID.randomUUID();
        KorrigertHenvisningDto korrigertHenvisningDto = new KorrigertHenvisningDto(behandling.getId(), eksternUuid);

        Response respons = forvaltningBehandlingRestTjeneste.korrigerHenvisning(korrigertHenvisningDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ProsessTaskData korrigertHenvisningProsessTask = assertProsessTask(TaskType.forProsessTask(KorrigertHenvisningTask.class));
        assertThat(korrigertHenvisningProsessTask.getBehandlingIdAsLong()).isEqualTo(behandling.getId());
        assertThat(korrigertHenvisningProsessTask.getPropertyValue(KorrigertHenvisningTask.PROPERTY_EKSTERN_UUID)).isEqualTo(eksternUuid.toString());
    }

    @Test
    void skal_flytte_behandling_til_fakta_steg_når_behandling_er_i_foreslå_steg() {
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FORESLÅ_VEDTAK);

        forvaltningBehandlingRestTjeneste.tilbakeførBehandlingTilFaktaSteg(new BehandlingReferanse(behandling.getId()));

        assertEquals(BehandlingStegType.FAKTA_FEILUTBETALING, behandling.getAktivtBehandlingSteg());
        var historikkinnslags = historikkRepository.hent(behandling.getId());
        assertThat(historikkinnslags).hasSize(1);
        var historikkinnslag = historikkinnslags.getFirst();
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getLinjer()).hasSize(1);
        assertThat(historikkinnslag.getLinjer().getFirst().getTekst()).contains(KravgrunnlagTjeneste.BEGRUNNELSE_BEHANDLING_STARTET_FORFRA);
    }

    @Test
    void skal_ikke_flytte_behandling_til_fakta_steg_når_behandling_allerede_er_avsluttet() {
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tilbakeførBehandlingTilFaktaSteg(
            new BehandlingReferanse(behandling.getId()));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void skal_ikke_flytte_behandling_til_fakta_steg_når_behandling_er_på_vent() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        Response response = forvaltningBehandlingRestTjeneste.tilbakeførBehandlingTilFaktaSteg(
            new BehandlingReferanse(behandling.getId()));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    private Behandling opprettBehandling(Fagsak fagsak) {
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }

    private String getKravgrunnlagXml(boolean gyldig) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<urn:detaljertKravgrunnlagMelding xmlns:urn=\"urn:no:nav:tilbakekreving:kravgrunnlag:detalj:v1\"\n"
            + "                                  xmlns:mmel=\"urn:no:nav:tilbakekreving:typer:v1\">\n"
            + "    <urn:detaljertKravgrunnlag>\n" + "        <urn:kravgrunnlagId>123456789</urn:kravgrunnlagId>\n"
            + "        <urn:vedtakId>100</urn:vedtakId>\n" + "        <urn:kodeStatusKrav>NY</urn:kodeStatusKrav>\n"
            + "        <urn:kodeFagomraade>FP</urn:kodeFagomraade>\n"
            + "        <urn:fagsystemId>100001</urn:fagsystemId>\n" + "        <!--Optional:-->\n"
            + "        <urn:datoVedtakFagsystem>2007-10-26</urn:datoVedtakFagsystem>\n"
            + "        <urn:vedtakGjelderId>12345678901</urn:vedtakGjelderId>\n"
            + "        <urn:typeGjelderId>PERSON</urn:typeGjelderId>\n"
            + "        <urn:utbetalesTilId>12345678901</urn:utbetalesTilId>\n"
            + "        <urn:typeUtbetId>PERSON</urn:typeUtbetId>\n"
            + "        <urn:enhetAnsvarlig>8020</urn:enhetAnsvarlig>\n"
            + "        <urn:enhetBosted>8020</urn:enhetBosted>\n"
            + "        <urn:enhetBehandl>8020</urn:enhetBehandl>\n"
            + "        <urn:kontrollfelt>kontrolll-123</urn:kontrollfelt>\n"
            + "        <urn:saksbehId>Z111111</urn:saksbehId>\n" + "        <!--Optional:-->\n"
            + "        <urn:referanse>100000001</urn:referanse>\n" + "        <!--1 or more repetitions:-->\n"
            + "        <urn:tilbakekrevingsPeriode>\n" + "            <urn:periode>\n"
            + "                <mmel:fom>2018-11-01+01:00</mmel:fom>\n"
            + "                <mmel:tom>2018-11-22+02:00</mmel:tom>\n" + "            </urn:periode>\n"
            + "            <urn:belopSkattMnd>0.00</urn:belopSkattMnd>\n"
            + "            <!--1 or more repetitions:-->\n" + "            <urn:tilbakekrevingsBelop>\n"
            + "                <urn:kodeKlasse>FPATORD</urn:kodeKlasse>\n"
            + "                <urn:typeKlasse>YTEL</urn:typeKlasse>\n"
            + "                <urn:belopOpprUtbet>9000.00</urn:belopOpprUtbet>\n"
            + "                <urn:belopNy>0.00</urn:belopNy>\n"
            + "                <urn:belopTilbakekreves>9000.00</urn:belopTilbakekreves>\n" + String.format(
            "  <urn:skattProsent>%d.0000</urn:skattProsent>\n", gyldig ? 0 : 100)
            + "            </urn:tilbakekrevingsBelop>\n" + "            <urn:tilbakekrevingsBelop>\n"
            + "                <urn:kodeKlasse>KL_KODE_FEIL_KORTTID</urn:kodeKlasse>\n"
            + "                <urn:typeKlasse>FEIL</urn:typeKlasse>\n"
            + "                <urn:belopOpprUtbet>0.00</urn:belopOpprUtbet>\n"
            + "                <urn:belopNy>9000.00</urn:belopNy>\n"
            + "                <urn:belopTilbakekreves>0.00</urn:belopTilbakekreves>\n"
            + "                <urn:skattProsent>0.0000</urn:skattProsent>\n"
            + "            </urn:tilbakekrevingsBelop>\n" + "        </urn:tilbakekrevingsPeriode>\n"
            + "    </urn:detaljertKravgrunnlag>\n" + "</urn:detaljertKravgrunnlagMelding>\n";
    }

    private ProsessTaskData assertProsessTask(TaskType taskType) {
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTaskData = captor.getValue();
        assertThat(prosessTaskData.taskType()).isEqualTo(taskType);
        return prosessTaskData;
    }

}
