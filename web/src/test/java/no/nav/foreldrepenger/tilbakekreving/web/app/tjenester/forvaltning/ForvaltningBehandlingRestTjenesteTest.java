package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktafeilutbetaling.FaktaFeilutbetalingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.MottattGrunnlagSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.IverksetteVedtakSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.HentKorrigertKravgrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.KobleBehandlingTilGrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.KorrigertHenvisningDto;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class ForvaltningBehandlingRestTjenesteTest {

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingRepositoryProvider repositoryProvider;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private HistorikkRepository historikkRepository;
    private ØkonomiMottattXmlRepository mottattXmlRepository;

    private PersonOrganisasjonWrapper mockTpsAdapterWrapper;
    private BehandlingModellRepository mockBehandlingModellRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private InternalManipulerBehandling manipulerBehandling;
    private ForvaltningBehandlingRestTjeneste forvaltningBehandlingRestTjeneste;

    private Behandling behandling;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        historikkRepository = repositoryProvider.getHistorikkRepository();
        mottattXmlRepository = new ØkonomiMottattXmlRepository(entityManager);
        FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories = new FellesQueriesForBehandlingRepositories(
            entityManager);
        BehandlingKandidaterRepository behandlingKandidaterRepository = new BehandlingKandidaterRepository(
            fellesQueriesForBehandlingRepositories);
        BehandlingVenterRepository behandlingVenterRepository = new BehandlingVenterRepository(
            fellesQueriesForBehandlingRepositories);
        VarselresponsRepository varselresponsRepository = new VarselresponsRepository(entityManager);
        mockTpsAdapterWrapper = mock(PersonOrganisasjonWrapper.class);
        KravgrunnlagMapper kravgrunnlagMapper = new KravgrunnlagMapper(mockTpsAdapterWrapper);
        mockBehandlingModellRepository = mock(BehandlingModellRepository.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(repositoryProvider,
            mockBehandlingModellRepository, mock(BehandlingskontrollEventPubliserer.class));
        ØkonomiSendtXmlRepository økonomiSendtXmlRepository = new ØkonomiSendtXmlRepository(entityManager);
        TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste = mock(TilbakekrevingsvedtakTjeneste.class);
        SlettGrunnlagEventPubliserer mockSlettGrunnlagEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);
        manipulerBehandling = new InternalManipulerBehandling(repositoryProvider);
        VarselresponsTjeneste varselresponsTjeneste = new VarselresponsTjeneste(varselresponsRepository);
        GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjeneste(taskTjeneste,
            behandlingKandidaterRepository, behandlingVenterRepository, repositoryProvider, varselresponsTjeneste);
        KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider,
            gjenopptaBehandlingTjeneste, behandlingskontrollTjeneste, mockSlettGrunnlagEventPubliserer);
        forvaltningBehandlingRestTjeneste = new ForvaltningBehandlingRestTjeneste(repositoryProvider,
            taskTjeneste, mottattXmlRepository, kravgrunnlagMapper, økonomiSendtXmlRepository,
            tilbakekrevingsvedtakTjeneste, kravgrunnlagTjeneste);

        behandling = lagBehandling();
    }

    @Test
    public void skal_ikke_tvinge_henlegg_behandling_når_behandling_er_allerede_avsluttet() {
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_tvinge_henlegg_behandling() {
        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertProsessTask(TaskType.forProsessTask(TvingHenlegglBehandlingTask.class));
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_er_avsluttet() {
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_ikke_er_på_vent() {
        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(
            new BehandlingReferanse(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertProsessTask(TaskType.forProsessTask(GjenopptaBehandlingTask.class));
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_behandling_er_ikke_på_vent() {
        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), 1l));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_behandling_er_ikke_på_vent_på_tilbakekrevingsgrunnlag() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), 1l));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_mottattXml_er_status_melding() {
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
    public void skal_ikke_tvinge_koble_grunnlag_når_mottattXml_er_allerede_koblet() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(true));
        mottattXmlRepository.opprettTilkobling(mottattXmlId);

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(
            new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_tvinge_koble_grunnlag_når_mottattXml_er_grunnlag() {
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
    public void skal_ikke_tvinge_koble_grunnlag_når_kravgrunnlaget_er_ugyldig() {
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
    public void skal_hente_korrigert_kravgrunnlag() {
        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningBehandlingRestTjeneste.hentKorrigertKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertProsessTask(TaskType.forProsessTask(HentKorrigertKravgrunnlagTask.class));
    }

    @Test
    public void skal_ikke_hente_korrigert_kravgrunnlag_når_behandling_er_avsluttet() {
        behandling.avsluttBehandling();
        HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto = new HentKorrigertKravgrunnlagDto(behandling.getId(),
            "");
        Response respons = forvaltningBehandlingRestTjeneste.hentKorrigertKravgrunnlag(hentKorrigertKravgrunnlagDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_opprette_prosess_tasken_når_henvisning_korrigeres() {
        UUID eksternUuid = UUID.randomUUID();
        KorrigertHenvisningDto korrigertHenvisningDto = new KorrigertHenvisningDto(behandling.getId(), eksternUuid);

        Response respons = forvaltningBehandlingRestTjeneste.korrigerHenvisning(korrigertHenvisningDto);
        assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ProsessTaskData korrigertHenvisningProsessTask = assertProsessTask(TaskType.forProsessTask(KorrigertHenvisningTask.class));
        assertThat(korrigertHenvisningProsessTask.getBehandlingId()).isEqualTo(String.valueOf(behandling.getId()));
        assertThat(korrigertHenvisningProsessTask.getPropertyValue("eksternUuid")).isEqualTo(eksternUuid.toString());
    }

    @Test
    public void skal_flytte_behandling_til_fakta_steg_når_behandling_er_i_iverksettelse_steg() {
        when(mockBehandlingModellRepository.getBehandlingStegKonfigurasjon()).thenReturn(
            BehandlingStegKonfigurasjon.lagDummy());
        when(mockBehandlingModellRepository.getModell(any(BehandlingType.class))).thenReturn(
            lagDummyBehandlingsModell());
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.IVERKSETT_VEDTAK);

        forvaltningBehandlingRestTjeneste.tilbakeførBehandlingTilFaktaSteg(new BehandlingReferanse(behandling.getId()));
        assertEquals(BehandlingStegType.FAKTA_FEILUTBETALING, behandling.getAktivtBehandlingSteg());
        List<Historikkinnslag> historikkinnslags = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslags).isNotEmpty().hasSize(1);
        Historikkinnslag historikkinnslag = historikkinnslags.get(0);
        assertEquals(HistorikkinnslagType.BEH_STARTET_FORFRA, historikkinnslag.getType());
        assertEquals(HistorikkAktør.VEDTAKSLØSNINGEN, historikkinnslag.getAktør());
        assertThat(historikkinnslag.getHistorikkinnslagDeler()).isNotEmpty().hasSize(1);
        boolean begrunnelseFinnes = historikkinnslag.getHistorikkinnslagDeler()
            .stream()
            .anyMatch(historikkinnslagDel -> historikkinnslagDel.getBegrunnelse().isPresent()
                && KravgrunnlagTjeneste.BEGRUNNELSE_BEHANDLING_STARTET_FORFRA.equals(
                historikkinnslagDel.getBegrunnelse().get()));
        assertTrue(begrunnelseFinnes);
    }

    @Test
    public void skal_ikke_flytte_behandling_til_fakta_steg_når_behandling_allerede_er_avsluttet() {
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tilbakeførBehandlingTilFaktaSteg(
            new BehandlingReferanse(behandling.getId()));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void skal_ikke_flytte_behandling_til_fakta_steg_når_behandling_er_på_vent() {
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling,
            AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(3),
            Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        Response response = forvaltningBehandlingRestTjeneste.tilbakeførBehandlingTilFaktaSteg(
            new BehandlingReferanse(behandling.getId()));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    private Behandling lagBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
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

    private BehandlingModell lagDummyBehandlingsModell() {
        List<TestStegKonfig> steg = Lists.newArrayList(
            new TestStegKonfig(BehandlingStegType.TBKGSTEG, BehandlingType.TILBAKEKREVING, new MottattGrunnlagSteg()),
            new TestStegKonfig(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingType.TILBAKEKREVING,
                new FaktaFeilutbetalingSteg(behandlingRepository, null)),
            new TestStegKonfig(BehandlingStegType.IVERKSETT_VEDTAK, BehandlingType.TILBAKEKREVING,
                new IverksetteVedtakSteg(repositoryProvider, null)));

        BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> finnSteg = map(steg);
        BehandlingModellImpl modell = new BehandlingModellImpl(BehandlingType.TILBAKEKREVING, finnSteg);

        steg.forEach(konfig -> modell.leggTil(konfig.getBehandlingStegType(), konfig.getBehandlingType()));
        return modell;
    }

    private static BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> map(List<TestStegKonfig> input) {
        Map<List<?>, BehandlingSteg> resolver = new HashMap<>();
        for (TestStegKonfig konfig : input) {
            List<?> key = Arrays.asList(konfig.getBehandlingStegType(), konfig.getBehandlingType());
            resolver.put(key, konfig.getSteg());
        }
        BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> func = (t, u) -> resolver.get(
            Arrays.asList(t, u));
        return func;
    }

}
