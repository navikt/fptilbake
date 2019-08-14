package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.OpprettBehandlingDto;

public class BehandlingRestTjenesteTest {

    public static final String GYLDIG_AKTØR_ID = "12345678901";
    public static final String UGYLDIG_AKTØR_ID = "%&#123124";
    public static final String GYLDIG_SAKSNR = "123456";
    public static final String UGYLDIG_SAKSNR = "(#2141##";
    public static final long EKSTERN_BEHANDLING_ID = 123456L;
    public static final String YTELSE_TYPE = FagsakYtelseType.FORELDREPENGER.getKode();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjenesteMock = mock(GjenopptaBehandlingTjeneste.class);
    private BehandlingDtoTjeneste behandlingDtoTjenesteMock = mock(BehandlingDtoTjeneste.class);
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste = mock(BehandlingsprosessApplikasjonTjeneste.class);
    private HenleggBehandlingTjeneste henleggBehandlingTjenesteMock = mock(HenleggBehandlingTjeneste.class);
    private BehandlingRevurderingTjeneste revurderingTjenesteMock = mock(BehandlingRevurderingTjeneste.class);
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjenesteMock = mock(BehandlingskontrollAsynkTjeneste.class);

    private BehandlingRestTjeneste behandlingRestTjeneste = new BehandlingRestTjeneste(behandlingTjenesteMock, gjenopptaBehandlingTjenesteMock,
        behandlingDtoTjenesteMock, behandlingsprosessTjeneste, henleggBehandlingTjenesteMock, revurderingTjenesteMock, behandlingskontrollAsynkTjenesteMock);

    @Test
    public void test_opprett_behandling_skal_feile_med_ugyldig_aktørId() throws URISyntaxException {
        OpprettBehandlingDto dto = opprettBehandlingDto(UGYLDIG_AKTØR_ID, GYLDIG_SAKSNR, EKSTERN_BEHANDLING_ID, YTELSE_TYPE);

        expectedException.expect(IllegalArgumentException.class); // ved rest-kall vil jax validering slå inn og resultere i en FeltFeil
        expectedException.expectMessage("Ugyldig aktørId");

        behandlingRestTjeneste.opprettBehandling(dto);
    }

    @Test
    public void test_opprett_behandling_skal_feile_med_ugyldig_saksnummer() throws URISyntaxException {
        OpprettBehandlingDto dto = opprettBehandlingDto(GYLDIG_AKTØR_ID, UGYLDIG_SAKSNR, EKSTERN_BEHANDLING_ID, YTELSE_TYPE);

        expectedException.expect(IllegalArgumentException.class); // ved rest-kall vil jax validering slå inn og resultere i en FeltFeil
        expectedException.expectMessage("Ugyldig saksnummer");

        behandlingRestTjeneste.opprettBehandling(dto);
    }

    @Test
    public void test_skal_opprette_ny_behandling() throws URISyntaxException {
        behandlingRestTjeneste.opprettBehandling(opprettBehandlingDto(GYLDIG_AKTØR_ID, GYLDIG_SAKSNR, EKSTERN_BEHANDLING_ID, YTELSE_TYPE));

        verify(behandlingTjenesteMock).opprettBehandlingManuell(any(Saksnummer.class), anyLong(), any(AktørId.class),anyString(), any(BehandlingType.class));
    }

    @Test
    public void test_skal_opprette_ny_behandling_for_revurdering() throws URISyntaxException {
        when(behandlingskontrollAsynkTjenesteMock.asynkProsesserBehandling(any(Behandling.class))).thenReturn("1");
        when(revurderingTjenesteMock.opprettRevurdering(any(Saksnummer.class), anyLong(), anyString()))
            .thenReturn(mockBehandling());

        OpprettBehandlingDto opprettBehandlingDto = opprettBehandlingDto(GYLDIG_AKTØR_ID, GYLDIG_SAKSNR, EKSTERN_BEHANDLING_ID, YTELSE_TYPE);
        opprettBehandlingDto.setBehandlingType(BehandlingType.REVURDERING_TILBAKEKREVING.getKode());
        opprettBehandlingDto.setBehandlingArsakType(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR.getKode());

        Response response = behandlingRestTjeneste.opprettBehandling(opprettBehandlingDto);

        verify(revurderingTjenesteMock, atLeastOnce()).opprettRevurdering(any(Saksnummer.class), anyLong(), anyString());
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_ACCEPTED);
    }

    @Test
    public void test_skal_kalle_på_henlegg_behandling() {
        long versjon = 2L;
        BehandlingResultatType årsak = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        String begrunnelse = "begrunnelse";

        behandlingRestTjeneste.henleggBehandling(opprettHenleggBehandlingDto(EKSTERN_BEHANDLING_ID, versjon, årsak, begrunnelse));

        verify(henleggBehandlingTjenesteMock).henleggBehandling(EKSTERN_BEHANDLING_ID, årsak, begrunnelse);
    }

    private OpprettBehandlingDto opprettBehandlingDto(String aktørId, String saksnr, long eksternBehandlingId, String ytelseType) {
        OpprettBehandlingDto dto = new OpprettBehandlingDto();
        dto.setAktørId(aktørId);
        dto.setSaksnummer(saksnr);
        dto.setEksternBehandlingId(eksternBehandlingId);
        dto.setBehandlingType(BehandlingType.TILBAKEKREVING.getKode());
        dto.setFagsakYtelseType(ytelseType);
        return dto;
    }

    private HenleggBehandlingDto opprettHenleggBehandlingDto(long behandlingId, long versjon, BehandlingResultatType årsak, String begrunnelse) {
        HenleggBehandlingDto dto = new HenleggBehandlingDto();
        dto.setBegrunnelse(begrunnelse);
        dto.setBehandlingId(behandlingId);
        dto.setBehandlingVersjon(versjon);
        dto.setÅrsakKode(årsak.getKode());
        return dto;
    }

    private Behandling mockBehandling() {
        return Behandling.nyBehandlingFor(
            Fagsak.opprettNy(1l, new Saksnummer(GYLDIG_SAKSNR), NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.nb)),
            BehandlingType.REVURDERING_TILBAKEKREVING).build();
    }

}
