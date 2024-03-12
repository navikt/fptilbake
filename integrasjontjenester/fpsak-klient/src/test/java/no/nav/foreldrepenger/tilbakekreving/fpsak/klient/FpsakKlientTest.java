package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakBehandlingInfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering.FpoppdragRestKlient;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;

class FpsakKlientTest {

    private static final Long BEHANDLING_ID = 123456L;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(BEHANDLING_ID);
    private static final String SAKSNUMMER = "1256436";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private static final String BASE_URI = "http://fpsak";

    private RestClient restClientMock = mock(RestClient.class);
    private FpoppdragRestKlient fpoppdragRestKlient = mock(FpoppdragRestKlient.class);

    private FpsakKlient klient = new FpsakKlient(restClientMock, fpoppdragRestKlient);

    @Test
    void skal_hente_DokumentinfoDto() {
        FpsakBehandlingInfoDto returnDto = dokumentinfoDto();

        when(restClientMock.sendReturnOptional(any(), eq(FpsakBehandlingInfoDto.class))).thenReturn(Optional.of(returnDto));
        when(restClientMock.sendReturnOptional(any(), eq(PersonopplysningDto.class))).thenReturn(Optional.of(personopplysningDto()));

        SamletEksternBehandlingInfo dokumentinfoDto = klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER);

        assertThat(dokumentinfoDto.getGrunninformasjon()).isEqualTo(returnDto);
        assertThat(dokumentinfoDto.getPersonopplysninger()).isNotNull();
    }

    @Test
    void skal_kaste_exception_når_fpsak_behandling_ikke_finnes() {
        when(restClientMock.sendReturnOptional(any(), any())).thenReturn(Optional.empty());

        Assertions.assertThrows(IntegrasjonException.class, () -> klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER));
    }

    @Test
    void skal_returnere_hvis_finnes_behandling_i_fpsak() {
        FpsakBehandlingInfoDto eksternBehandlingInfo = dokumentinfoDto();
        FpsakKlient.ListeAvFpsakBehandlingInfoDto liste = new FpsakKlient.ListeAvFpsakBehandlingInfoDto();
        liste.add(eksternBehandlingInfo);
        when(restClientMock.send(any(), eq(FpsakKlient.ListeAvFpsakBehandlingInfoDto.class))).thenReturn(liste);

        boolean erFinnesIFpsak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIFpsak).isTrue();
    }

    @Test
    void skal_returnere_tom_hvis_finnes_ikke_behandling_i_fpsak() {
        when(restClientMock.send(any(), eq(FpsakKlient.ListeAvFpsakBehandlingInfoDto.class))).thenReturn(new FpsakKlient.ListeAvFpsakBehandlingInfoDto());

        boolean erFinnesIFpsak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIFpsak).isFalse();
    }

    @Test
    void skal_returnere_tilbakekreving_valg() {
        TilbakekrevingValgDto tilbakekrevingValgDto = new TilbakekrevingValgDto(VidereBehandling.TILBAKEKR_OPPRETT);
        when(restClientMock.sendReturnOptional(any(), eq(FpsakBehandlingInfoDto.class))).thenReturn(Optional.of(dokumentinfoDto()));
        when(restClientMock.sendReturnOptional(any(), eq(TilbakekrevingValgDto.class))).thenReturn(Optional.of(tilbakekrevingValgDto));

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isPresent();
        assertThat(valgDto.get().getVidereBehandling()).isEqualByComparingTo(VidereBehandling.TILBAKEKR_OPPRETT);
    }

    @Test
    void skal_returnere_tom_tilbakekreving_valg() {
        when(restClientMock.sendReturnOptional(any(), eq(FpsakBehandlingInfoDto.class))).thenReturn(Optional.of(dokumentinfoDto()));
        when(restClientMock.sendReturnOptional(any(), eq(TilbakekrevingValgDto.class))).thenReturn(Optional.empty());

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isEmpty();
    }

    private FpsakBehandlingInfoDto dokumentinfoDto() {
        FpsakBehandlingInfoDto dto = new FpsakBehandlingInfoDto();
        dto.setBehandlendeEnhetId("4214");
        dto.setBehandlendeEnhetNavn("enhetnavn");
        dto.setId(BEHANDLING_ID);
        dto.setLinks(resourcelinks());
        return dto;
    }

    private PersonopplysningDto personopplysningDto() {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setAktoerId("aktørId");
        dto.setAntallBarn(1);
        return dto;
    }

    private List<BehandlingResourceLinkDto> resourcelinks() {
        BehandlingResourceLinkDto personOpplysningerRessursLink = new BehandlingResourceLinkDto();
        personOpplysningerRessursLink.setHref("/fpsak/api/behandling/person/personopplysninger?uuid=" + BEHANDLING_UUID.toString());
        personOpplysningerRessursLink.setRel("personopplysninger-tilbake");

        BehandlingResourceLinkDto tilbakekrevingvalgRessursLink = new BehandlingResourceLinkDto();
        tilbakekrevingvalgRessursLink.setHref("/fpsak/api/behandling/tilbakekreving/valg?uuid=" + BEHANDLING_UUID.toString());
        tilbakekrevingvalgRessursLink.setRel("tilbakekreving-valg");

        BehandlingResourceLinkDto varselTekstRessursLink = new BehandlingResourceLinkDto();
        varselTekstRessursLink.setHref("/fpsak/api/behandling/tilbakekreving/varseltekst?uuid=" + BEHANDLING_UUID.toString());
        varselTekstRessursLink.setRel("tilbakekrevingsvarsel-fritekst");
        return List.of(personOpplysningerRessursLink, tilbakekrevingvalgRessursLink, varselTekstRessursLink);
    }

}
