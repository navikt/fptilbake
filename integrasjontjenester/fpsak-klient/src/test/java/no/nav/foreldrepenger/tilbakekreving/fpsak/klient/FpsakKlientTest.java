package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakTilbakeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering.FpoppdragRestKlient;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;

class FpsakKlientTest {

    private static final Long BEHANDLING_ID = 123456L;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(BEHANDLING_ID);
    private static final String SAKSNUMMER = "1256436";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private final RestClient restClientMock = mock(RestClient.class);
    private final FpoppdragRestKlient fpoppdragRestKlient = mock(FpoppdragRestKlient.class);

    private FpsakKlient klient = new FpsakKlient(restClientMock, fpoppdragRestKlient);

    @Test
    void skal_hente_DokumentinfoDto() {
        var returnDto = dokumentinfoDto(null);

        when(restClientMock.sendReturnOptional(any(), eq(FpsakTilbakeDto.class))).thenReturn(Optional.of(returnDto));

        SamletEksternBehandlingInfo dokumentinfoDto = klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER);

        assertThat(dokumentinfoDto.getGrunninformasjon().getHenvisning().toLong()).isEqualTo(BEHANDLING_ID);
        assertThat(dokumentinfoDto.getGrunninformasjon().getUuid()).isEqualTo(BEHANDLING_UUID);
        assertThat(dokumentinfoDto.getGrunninformasjon().getBehandlendeEnhetId()).isEqualTo("4214");
        assertThat(dokumentinfoDto.getPersonopplysninger()).isNotNull();
    }

    @Test
    void skal_kaste_exception_når_fpsak_behandling_ikke_finnes() {
        when(restClientMock.sendReturnOptional(any(), any())).thenReturn(Optional.empty());

        Assertions.assertThrows(IntegrasjonException.class, () -> klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER));
    }

    @Test
    void skal_returnere_hvis_finnes_behandling_i_fpsak() {
        var eksternBehandlingInfo = dokumentinfoDto(null);
        when(restClientMock.sendReturnOptional(any(), eq(FpsakTilbakeDto.class))).thenReturn(Optional.of(eksternBehandlingInfo));

        boolean erFinnesIFpsak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIFpsak).isTrue();
    }

    @Test
    void skal_returnere_tom_hvis_finnes_ikke_behandling_i_fpsak() {
        when(restClientMock.sendReturnOptional(any(), eq(FpsakTilbakeDto.class))).thenReturn(Optional.empty());

        boolean erFinnesIFpsak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIFpsak).isFalse();
    }

    @Test
    void skal_returnere_tilbakekreving_valg() {
        var dto = dokumentinfoDto(new FpsakTilbakeDto.FeilutbetalingDto(FpsakTilbakeDto.FeilutbetalingValg.OPPRETT, null));
        when(restClientMock.sendReturnOptional(any(), eq(FpsakTilbakeDto.class))).thenReturn(Optional.of(dto));

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isPresent();
        assertThat(valgDto.get().videreBehandling()).isEqualByComparingTo(VidereBehandling.TILBAKEKR_OPPRETT);
    }

    @Test
    void skal_returnere_tom_tilbakekreving_valg() {
        when(restClientMock.sendReturnOptional(any(), eq(FpsakTilbakeDto.class))).thenReturn(Optional.of(dokumentinfoDto(null)));

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isEmpty();
    }

    private FpsakTilbakeDto dokumentinfoDto(FpsakTilbakeDto.FeilutbetalingDto feilutbetalingDto) {
        var b = new FpsakTilbakeDto.BehandlingDto(BEHANDLING_UUID, new FpsakTilbakeDto.HenvisningDto(BEHANDLING_ID),
            "4214", "enhetnavn",
            FpsakTilbakeDto.Språkkode.NB, null);
        var f = new FpsakTilbakeDto.FagsakDto("1234567890123", SAKSNUMMER, FpsakTilbakeDto.YtelseType.FORELDREPENGER);
        var fam = new FpsakTilbakeDto.FamilieHendelseDto(FpsakTilbakeDto.FamilieHendelseType.FØDSEL, 1);
        return new FpsakTilbakeDto(b, f, fam, feilutbetalingDto, false, null);
    }


}
