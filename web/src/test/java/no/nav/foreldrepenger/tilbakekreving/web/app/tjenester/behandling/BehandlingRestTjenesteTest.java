package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler.DokumentBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.FpsakUuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.KlageTilbakekrevingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.OpprettBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeBehandlingsmenyEnum;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;

class BehandlingRestTjenesteTest {

    @BeforeAll
    static void setupAlle() {
        System.setProperty("app.name", "fptilbake");
    }

    @AfterAll
    static void teardown() {
        System.clearProperty("app.name");
    }


    static final String GYLDIG_AKTØR_ID = "12345678901";
    static final String GYLDIG_SAKSNR = "123456";
    static final String UGYLDIG_SAKSNR = "(#2141##";
    static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();
    private static final FagsakYtelseType FP_YTELSE_TYPE = FagsakYtelseType.FORELDREPENGER;

    private ProsessTaskTjeneste taskTjeneste = mock(ProsessTaskTjeneste.class);
    private BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjenesteMock = mock(GjenopptaBehandlingTjeneste.class);
    private BehandlingDtoTjeneste behandlingDtoTjenesteMock = mock(BehandlingDtoTjeneste.class);
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste = mock(BehandlingsprosessApplikasjonTjeneste.class);
    private HenleggBehandlingTjeneste henleggBehandlingTjenesteMock = mock(HenleggBehandlingTjeneste.class);
    private BehandlingRevurderingTjeneste revurderingTjenesteMock = mock(BehandlingRevurderingTjeneste.class);
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjenesteMock = mock(BehandlingskontrollAsynkTjeneste.class);
    private BehandlendeEnhetTjeneste behandlendeEnhetTjenesteMock = mock(BehandlendeEnhetTjeneste.class);
    private VergeTjeneste vergeTjenesteMock = mock(VergeTjeneste.class);
    private HistorikkTjeneste historikkTjeneste = mock(HistorikkTjeneste.class);

    private BehandlingRestTjeneste behandlingRestTjeneste = new BehandlingRestTjeneste(behandlingTjenesteMock, gjenopptaBehandlingTjenesteMock,
        revurderingTjenesteMock, behandlendeEnhetTjenesteMock, behandlingDtoTjenesteMock, taskTjeneste, vergeTjenesteMock,
        mock(TotrinnTjeneste.class), mock(DokumentBehandlingTjeneste.class), henleggBehandlingTjenesteMock,
        behandlingsprosessTjeneste, behandlingskontrollAsynkTjenesteMock, historikkTjeneste);

    private static SaksnummerDto saksnummerDto = new SaksnummerDto(GYLDIG_SAKSNR);
    private static FpsakUuidDto fpsakUuidDto = new FpsakUuidDto(EKSTERN_BEHANDLING_UUID);
    private static BehandlingReferanse behandlingReferanse = new BehandlingReferanse(1L);
    private static UuidDto uuidDto = new UuidDto(UUID.randomUUID());

    @Test
    void test_opprett_behandling_skal_feile_med_ugyldig_saksnummer() {
        OpprettBehandlingDto dto = opprettBehandlingDto(UGYLDIG_SAKSNR, EKSTERN_BEHANDLING_UUID, FP_YTELSE_TYPE);

        assertThatThrownBy(() -> behandlingRestTjeneste.opprettBehandling(mock(HttpServletRequest.class), dto)) // ved rest-kall vil jax validering slå inn og resultere i en FeltFeil
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ugyldig saksnummer");
    }

    @Test
    void test_skal_opprette_ny_behandling() throws URISyntaxException {
        var behandling = mockBehandling();
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        when(behandlingTjenesteMock.opprettKunBehandlingManuell(any(), any(), any(), any())).thenReturn(behandling);

        behandlingRestTjeneste.opprettBehandling(mock(HttpServletRequest.class), opprettBehandlingDto(GYLDIG_SAKSNR, EKSTERN_BEHANDLING_UUID, FP_YTELSE_TYPE));

        verify(behandlingTjenesteMock).opprettKunBehandlingManuell(any(Saksnummer.class), any(UUID.class), any(FagsakYtelseType.class), any(BehandlingType.class));
    }

    @Test
    void test_skal_opprette_ny_behandling_for_revurdering() throws URISyntaxException {
        when(behandlingskontrollAsynkTjenesteMock.asynkProsesserBehandling(any(Behandling.class))).thenReturn("1");
        when(behandlingTjenesteMock.hentEnhetForEksternBehandling(any())).thenReturn(new OrganisasjonsEnhet("9999", "Generisk"));
        when(revurderingTjenesteMock.opprettRevurdering(any(Long.class), any(BehandlingÅrsakType.class), any(OrganisasjonsEnhet.class), any(String.class)))
                .thenReturn(mockBehandling());
        KontekstHolder.setKontekst(RequestKontekst.forRequest("bruker", "bruker", IdentType.InternBruker, null, UUID.randomUUID(), Set.of()));
        OpprettBehandlingDto opprettBehandlingDto = opprettBehandlingDto(GYLDIG_SAKSNR, EKSTERN_BEHANDLING_UUID, FP_YTELSE_TYPE);
        opprettBehandlingDto.setBehandlingType(BehandlingType.REVURDERING_TILBAKEKREVING);
        opprettBehandlingDto.setBehandlingArsakType(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        opprettBehandlingDto.setBehandlingId(1L);

        Response response = behandlingRestTjeneste.opprettBehandling(mock(HttpServletRequest.class), opprettBehandlingDto);

        verify(revurderingTjenesteMock, atLeastOnce()).opprettRevurdering(any(Long.class), any(BehandlingÅrsakType.class), any(OrganisasjonsEnhet.class), any(String.class));
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_ACCEPTED);
        KontekstHolder.fjernKontekst();
    }

    @Test
    void test_skal_kalle_på_henlegg_behandling() {
        long versjon = 2L;
        BehandlingResultatType årsak = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        String begrunnelse = "begrunnelse";
        String fritekst = "fritekst";

        behandlingRestTjeneste.henleggBehandling(opprettHenleggBehandlingDto(1234l, versjon, årsak, begrunnelse, fritekst));

        verify(henleggBehandlingTjenesteMock).henleggBehandlingManuelt(1234l, årsak, begrunnelse, fritekst);
    }

    @Test
    void test_kan_behandling_opprettes_med_tilbakekreving() {
        when(behandlingTjenesteMock.kanOppretteBehandling(any(Saksnummer.class), any(UUID.class))).thenReturn(true);

        Response response = behandlingRestTjeneste.kanOpprettesBehandling(saksnummerDto, fpsakUuidDto);
        assertThat(response.getEntity()).isNotNull();
        boolean result = (boolean) response.getEntity();
        assertThat(result).isTrue();
    }

    @Test
    void test_kan_behandling_opprettes_med_revurdering() {
        when(behandlingTjenesteMock.kanOppretteBehandling(any(Saksnummer.class), any(UUID.class))).thenReturn(false);
        when(revurderingTjenesteMock.hentEksternBehandling(anyLong())).thenReturn(opprettEksternBehandling());
        when(revurderingTjenesteMock.kanOppretteRevurdering(any(UUID.class))).thenReturn(true);

        Response response = behandlingRestTjeneste.kanOpprettesRevurdering(behandlingReferanse);
        assertThat(response.getEntity()).isNotNull();
        boolean result = (boolean) response.getEntity();
        assertThat(result).isTrue();
    }

    @Test
    void skal_ha_åpen_tilbakekreving_hvis_tilbakekreving_ikke_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        when(behandlingTjenesteMock.hentBehandlinger(any(Saksnummer.class))).thenReturn(List.of(behandling));
        Response response = behandlingRestTjeneste.harÅpenTilbakekrevingBehandling(saksnummerDto);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response.getEntity()).isEqualTo(true);
    }

    @Test
    void skal_ikke_ha_åpen_tilbakekreving_hvis_tilbakekreving_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        behandling.avsluttBehandling();
        when(behandlingTjenesteMock.hentBehandlinger(any(Saksnummer.class))).thenReturn(List.of(behandling));

        Response response = behandlingRestTjeneste.harÅpenTilbakekrevingBehandling(saksnummerDto);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response.getEntity()).isEqualTo(false);
    }

    @Test
    void skal_gi_rettigheter_for_vise_verge() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        when(behandlingTjenesteMock.hentBehandlinger(any(Saksnummer.class))).thenReturn(List.of(behandling));
        when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
        var behandlingUuid = new UuidDto(behandling.getUuid());
        var response = behandlingRestTjeneste.hentMenyOpsjoner(behandlingUuid);
        assertThat(response.getVergeBehandlingsmeny()).isEqualTo(VergeBehandlingsmenyEnum.OPPRETT);
        assertThat(response.isBehandlingKanGjenopptas()).isFalse();
        assertThat(response.isBehandlingKanSettesPaVent()).isTrue();

        var sakResponse = behandlingRestTjeneste.hentRettigheterSak(saksnummerDto);
        assertThat(sakResponse.getBehandlingTypeKanOpprettes().stream().filter(b -> BehandlingType.TILBAKEKREVING.equals(b.getBehandlingType())).findAny())
                .hasValueSatisfying(v -> assertThat(v.isKanOppretteBehandling()).isFalse());
        assertThat(sakResponse.getBehandlingTypeKanOpprettes().stream().filter(b -> BehandlingType.REVURDERING_TILBAKEKREVING.equals(b.getBehandlingType())).findAny())
                .hasValueSatisfying(v -> assertThat(v.isKanOppretteBehandling()).isFalse());

        when(vergeTjenesteMock.hentVergeInformasjon(anyLong())).thenReturn(Optional.of(VergeEntitet.builder()
                .medBegrunnelse("bla").medKilde("bla").medOrganisasjonnummer("999999999").medNavn("Vegard Verge").medVergeType(VergeType.ADVOKAT).build()));
        response = behandlingRestTjeneste.hentMenyOpsjoner(behandlingUuid);
        assertThat(response.getVergeBehandlingsmeny()).isEqualTo(VergeBehandlingsmenyEnum.FJERN);

        behandling.avsluttBehandling();
        response = behandlingRestTjeneste.hentMenyOpsjoner(behandlingUuid);
        assertThat(response.isBehandlingKanHenlegges()).isFalse();

        sakResponse = behandlingRestTjeneste.hentRettigheterSak(saksnummerDto);
        assertThat(sakResponse.getBehandlingTypeKanOpprettes().stream().filter(b -> BehandlingType.TILBAKEKREVING.equals(b.getBehandlingType())).findAny())
                .hasValueSatisfying(v -> assertThat(v.isKanOppretteBehandling()).isTrue());
        assertThat(sakResponse.getBehandlingTypeKanOpprettes().stream().filter(b -> BehandlingType.REVURDERING_TILBAKEKREVING.equals(b.getBehandlingType())).findAny())
                .hasValueSatisfying(v -> assertThat(v.isKanOppretteBehandling()).isFalse());

        when(revurderingTjenesteMock.kanRevurderingOpprettes(any(Behandling.class))).thenReturn(true);
        response = behandlingRestTjeneste.hentMenyOpsjoner(behandlingUuid);
        assertThat(response.isBehandlingKanHenlegges()).isFalse();
        sakResponse = behandlingRestTjeneste.hentRettigheterSak(saksnummerDto);
        assertThat(sakResponse.getBehandlingTypeKanOpprettes().stream().filter(b -> BehandlingType.TILBAKEKREVING.equals(b.getBehandlingType())).findAny())
                .hasValueSatisfying(v -> assertThat(v.isKanOppretteBehandling()).isTrue());
        assertThat(sakResponse.getBehandlingTypeKanOpprettes().stream().filter(b -> BehandlingType.REVURDERING_TILBAKEKREVING.equals(b.getBehandlingType())).findAny())
                .hasValueSatisfying(v -> assertThat(v.isKanOppretteBehandling()).isTrue());
    }

    @Test
    void skal_ikke_returnere_vedtak_info_hvis_tilbakekreving_ikke_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
        var e = assertThrows(TekniskException.class, () -> behandlingRestTjeneste.hentTilbakekrevingsVedtakInfo(uuidDto));
        assertThat(e.getMessage()).contains("FPT-763492");
    }

    @Test
    void skal_returnere_vedtak_info_hvis_tilbakekreving_er_avsluttet_og_vedtak_info_finnes() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
                .medBehandling(behandling)
                .medBehandlingResultatType(BehandlingResultatType.FULL_TILBAKEBETALING).build();
        var vedtakDato = LocalDate.now();
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
                .medBehandlingsresultat(behandlingsresultat)
                .medVedtaksdato(vedtakDato)
                .medIverksettingStatus(IverksettingStatus.IVERKSATT)
                .medAnsvarligSaksbehandler("VL").build();
        behandling.avsluttBehandling();

        when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
        when(behandlingTjenesteMock.hentBehandlingvedtakForBehandlingId(any(Long.class))).thenReturn(Optional.of(behandlingVedtak));

        Response response = behandlingRestTjeneste.hentTilbakekrevingsVedtakInfo(uuidDto);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        KlageTilbakekrevingDto klageTilbakekrevingDto = (KlageTilbakekrevingDto) response.getEntity();
        assertThat(klageTilbakekrevingDto.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(klageTilbakekrevingDto.getBehandlingId()).isNotNull();
        assertThat(klageTilbakekrevingDto.getVedtakDato()).isEqualTo(vedtakDato);
    }


    private OpprettBehandlingDto opprettBehandlingDto(String saksnr, UUID eksternUuid, FagsakYtelseType ytelseType) {
        OpprettBehandlingDto dto = new OpprettBehandlingDto();
        dto.setSaksnummer(new SaksnummerDto(saksnr));
        dto.setEksternUuid(eksternUuid);
        dto.setBehandlingType(BehandlingType.TILBAKEKREVING);
        dto.setFagsakYtelseType(ytelseType);
        return dto;
    }

    private HenleggBehandlingDto opprettHenleggBehandlingDto(long behandlingId, long versjon, BehandlingResultatType årsak,
                                                             String begrunnelse, String fritekst) {
        HenleggBehandlingDto dto = new HenleggBehandlingDto();
        dto.setBegrunnelse(begrunnelse);
        dto.setBehandlingId(behandlingId);
        dto.setBehandlingVersjon(versjon);
        dto.setÅrsakKode(årsak.getKode());
        dto.setFritekst(fritekst);
        return dto;
    }

    private Behandling mockBehandling() {
        var sak = Fagsak.opprettNy(new Saksnummer(GYLDIG_SAKSNR), NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.NB));
        sak.setId(10L);
        var behandling = Behandling.nyBehandlingFor(sak, BehandlingType.REVURDERING_TILBAKEKREVING).build();
        behandling.setId(20L);
        return behandling;
    }

    private Optional<EksternBehandling> opprettEksternBehandling() {
        return Optional.of(new EksternBehandling(mockBehandling(), Henvisning.fraEksternBehandlingId(1L), EKSTERN_BEHANDLING_UUID));
    }

}
