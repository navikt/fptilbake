package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingsTjenesteProvider;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingManglerKravgrunnlagFristenEndretEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.FpsakUuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.KlageTilbakekrevingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.OpprettBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.vedtak.exception.TekniskException;

public class BehandlingRestTjenesteTest {

    public static final String GYLDIG_AKTØR_ID = "12345678901";
    public static final String GYLDIG_SAKSNR = "123456";
    public static final String UGYLDIG_SAKSNR = "(#2141##";
    public static final String EKSTERN_BEHANDLING_UUID = UUID.randomUUID().toString();
    private static final FagsakYtelseType FP_YTELSE_TYPE = FagsakYtelseType.FORELDREPENGER;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjenesteMock = mock(GjenopptaBehandlingTjeneste.class);
    private BehandlingDtoTjeneste behandlingDtoTjenesteMock = mock(BehandlingDtoTjeneste.class);
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste = mock(BehandlingsprosessApplikasjonTjeneste.class);
    private HenleggBehandlingTjeneste henleggBehandlingTjenesteMock = mock(HenleggBehandlingTjeneste.class);
    private BehandlingRevurderingTjeneste revurderingTjenesteMock = mock(BehandlingRevurderingTjeneste.class);
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjenesteMock = mock(BehandlingskontrollAsynkTjeneste.class);
    private BehandlendeEnhetTjeneste behandlendeEnhetTjenesteMock = mock(BehandlendeEnhetTjeneste.class);
    private BehandlingsTjenesteProvider behandlingsTjenesteProvider = new BehandlingsTjenesteProvider(behandlingTjenesteMock, gjenopptaBehandlingTjenesteMock,
        henleggBehandlingTjenesteMock, revurderingTjenesteMock, behandlendeEnhetTjenesteMock);
    private BehandlingManglerKravgrunnlagFristenEndretEventPubliserer fristenEndretEventPubliserer = mock(BehandlingManglerKravgrunnlagFristenEndretEventPubliserer.class);

    private BehandlingRestTjeneste behandlingRestTjeneste = new BehandlingRestTjeneste(behandlingsTjenesteProvider, behandlingDtoTjenesteMock,
        behandlingsprosessTjeneste, behandlingskontrollAsynkTjenesteMock,fristenEndretEventPubliserer);

    private static SaksnummerDto saksnummerDto = new SaksnummerDto(GYLDIG_SAKSNR);
    private static FpsakUuidDto fpsakUuidDto = new FpsakUuidDto(EKSTERN_BEHANDLING_UUID);
    private static BehandlingReferanse behandlingReferanse = new BehandlingReferanse(1l);
    private static UuidDto uuidDto = new UuidDto(UUID.randomUUID());

    @Test
    public void test_opprett_behandling_skal_feile_med_ugyldig_saksnummer() throws URISyntaxException {
        OpprettBehandlingDto dto = opprettBehandlingDto(UGYLDIG_SAKSNR, EKSTERN_BEHANDLING_UUID, FP_YTELSE_TYPE);

        expectedException.expect(IllegalArgumentException.class); // ved rest-kall vil jax validering slå inn og resultere i en FeltFeil
        expectedException.expectMessage("Ugyldig saksnummer");

        behandlingRestTjeneste.opprettBehandling(dto);
    }

    @Test
    public void test_skal_opprette_ny_behandling() throws URISyntaxException {
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(mockBehandling());

        behandlingRestTjeneste.opprettBehandling(opprettBehandlingDto(GYLDIG_SAKSNR, EKSTERN_BEHANDLING_UUID, FP_YTELSE_TYPE));

        verify(behandlingTjenesteMock).opprettBehandlingManuell(any(Saksnummer.class), any(UUID.class), any(FagsakYtelseType.class), any(BehandlingType.class));
    }

    @Test
    public void test_skal_opprette_ny_behandling_for_revurdering() throws URISyntaxException {
        when(behandlingskontrollAsynkTjenesteMock.asynkProsesserBehandling(any(Behandling.class))).thenReturn("1");
        when(revurderingTjenesteMock.opprettRevurdering(any(Long.class), any(BehandlingÅrsakType.class)))
            .thenReturn(mockBehandling());

        OpprettBehandlingDto opprettBehandlingDto = opprettBehandlingDto(GYLDIG_SAKSNR, EKSTERN_BEHANDLING_UUID, FP_YTELSE_TYPE);
        opprettBehandlingDto.setBehandlingType(BehandlingType.REVURDERING_TILBAKEKREVING);
        opprettBehandlingDto.setBehandlingArsakType(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        opprettBehandlingDto.setBehandlingId(1l);

        Response response = behandlingRestTjeneste.opprettBehandling(opprettBehandlingDto);

        verify(revurderingTjenesteMock, atLeastOnce()).opprettRevurdering(any(Long.class), any(BehandlingÅrsakType.class));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_ACCEPTED);
    }

    @Test
    public void test_skal_kalle_på_henlegg_behandling() {
        long versjon = 2L;
        BehandlingResultatType årsak = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        String begrunnelse = "begrunnelse";
        String fritekst = "fritekst";

        behandlingRestTjeneste.henleggBehandling(opprettHenleggBehandlingDto(1234l, versjon, årsak, begrunnelse,fritekst));

        verify(henleggBehandlingTjenesteMock).henleggBehandlingManuelt(1234l, årsak, begrunnelse, fritekst);
    }

    @Test
    public void test_kan_behandling_opprettes_med_tilbakekreving() {
        when(behandlingTjenesteMock.kanOppretteBehandling(any(Saksnummer.class), any(UUID.class))).thenReturn(true);

        Response response = behandlingRestTjeneste.kanOpprettesBehandling(saksnummerDto, fpsakUuidDto);
        assertThat(response.getEntity()).isNotNull();
        boolean result = (boolean) response.getEntity();
        assertThat(result).isTrue();
    }

    @Test
    public void test_kan_behandling_opprettes_med_revurdering() {
        when(behandlingTjenesteMock.kanOppretteBehandling(any(Saksnummer.class), any(UUID.class))).thenReturn(false);
        when(revurderingTjenesteMock.hentEksternBehandling(anyLong())).thenReturn(opprettEksternBehandling());
        when(revurderingTjenesteMock.kanOppretteRevurdering(any(UUID.class))).thenReturn(true);

        Response response = behandlingRestTjeneste.kanOpprettesRevurdering(behandlingReferanse);
        assertThat(response.getEntity()).isNotNull();
        boolean result = (boolean) response.getEntity();
        assertThat(result).isTrue();
    }

    @Test
    public void skal_ha_åpen_tilbakekreving_hvis_tilbakekreving_ikke_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        when(behandlingTjenesteMock.hentBehandlinger(any(Saksnummer.class))).thenReturn(Lists.newArrayList(behandling));
        Response response = behandlingRestTjeneste.harÅpenTilbakekrevingBehandling(saksnummerDto);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getEntity()).isEqualTo(true);
    }

    @Test
    public void skal_ikke_ha_åpen_tilbakekreving_hvis_tilbakekreving_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        behandling.avsluttBehandling();
        when(behandlingTjenesteMock.hentBehandlinger(any(Saksnummer.class))).thenReturn(Lists.newArrayList(behandling));

        Response response = behandlingRestTjeneste.harÅpenTilbakekrevingBehandling(saksnummerDto);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getEntity()).isEqualTo(false);
    }

    @Test
    public void skal_ikke_returnere_vedtak_info_hvis_tilbakekreving_ikke_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
        var e = assertThrows(TekniskException.class, () -> behandlingRestTjeneste.hentTilbakekrevingsVedtakInfo(uuidDto));
        assertThat(e.getMessage()).contains("FPT-763492");
    }

    @Test
    public void skal_ikke_returnere_vedtak_info_hvis_vedtak_info_ikke_finnes() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        behandling.avsluttBehandling();
        when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
        var e = assertThrows(TekniskException.class, () -> behandlingRestTjeneste.hentTilbakekrevingsVedtakInfo(uuidDto));
        assertThat(e.getMessage()).contains("FPT-763492");
    }

    @Test
    public void skal_returnere_vedtak_info_hvis_tilbakekreving_er_avsluttet_og_vedtak_info_finnes() {
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
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        KlageTilbakekrevingDto klageTilbakekrevingDto = (KlageTilbakekrevingDto) response.getEntity();
        assertThat(klageTilbakekrevingDto.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(klageTilbakekrevingDto.getBehandlingId()).isNotNull();
        assertThat(klageTilbakekrevingDto.getVedtakDato()).isEqualTo(vedtakDato);
    }


    private OpprettBehandlingDto opprettBehandlingDto(String saksnr, String eksternUuid, FagsakYtelseType ytelseType) {
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
        return Behandling.nyBehandlingFor(
            Fagsak.opprettNy(new Saksnummer(GYLDIG_SAKSNR), NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.nb)),
            BehandlingType.REVURDERING_TILBAKEKREVING).build();
    }

    private Optional<EksternBehandling> opprettEksternBehandling() {
        return Optional.of(new EksternBehandling(mockBehandling(), Henvisning.fraEksternBehandlingId(1l), UUID.fromString(EKSTERN_BEHANDLING_UUID)));
    }

}
