package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;

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
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class FpsakKlientTest {

    private static final Long BEHANDLING_ID = 123456L;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(BEHANDLING_ID);
    private static final String SAKSNUMMER = "1256436";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private static final String BASE_URI = "http://fpsak";
    private static final URI BEHANDLING_URI = URI.create(BASE_URI + "/fpsak/api/behandling/backend-root?uuid=" + BEHANDLING_UUID);
    private static final URI BEHANDLING_ALLE_URI = URI.create(BASE_URI + "/fpsak/api/behandlinger/alle?saksnummer=" + SAKSNUMMER);
    private static final URI PERSONOPPLYSNING_URI = URI.create(BASE_URI + "/fpsak/api/behandling/person/personopplysninger?uuid=" + BEHANDLING_UUID);
    private static final URI TILBAKEKREVING_VALG_URI = URI.create(BASE_URI + "/fpsak/api/behandling/tilbakekreving/valg?uuid=" + BEHANDLING_UUID);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OidcRestClient oidcRestClientMock = mock(OidcRestClient.class);
    private FpoppdragRestKlient fpoppdragRestKlient = mock(FpoppdragRestKlient.class);

    private FpsakKlient klient = new FpsakKlient(oidcRestClientMock, fpoppdragRestKlient);

    @Test
    public void skal_hente_DokumentinfoDto() {
        FpsakBehandlingInfoDto returnDto = dokumentinfoDto();

        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, FpsakBehandlingInfoDto.class)).thenReturn(Optional.of(returnDto));
        when(oidcRestClientMock.getReturnsOptional(PERSONOPPLYSNING_URI, PersonopplysningDto.class)).thenReturn(Optional.of(personopplysningDto()));

        SamletEksternBehandlingInfo dokumentinfoDto = klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER);

        assertThat(dokumentinfoDto.getGrunninformasjon()).isEqualTo(returnDto);
        assertThat(dokumentinfoDto.getPersonopplysninger()).isNotNull();
    }

    @Test
    public void skal_kaste_exception_når_fpsak_behandling_ikke_finnes() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.empty());

        Assertions.assertThrows(IntegrasjonException.class, () -> klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER));
    }

    @Test
    public void skal_returnere_hvis_finnes_behandling_i_fpsak() {
        FpsakBehandlingInfoDto eksternBehandlingInfo = dokumentinfoDto();
        FpsakKlient.ListeAvFpsakBehandlingInfoDto liste = new FpsakKlient.ListeAvFpsakBehandlingInfoDto();
        liste.add(eksternBehandlingInfo);
        when(oidcRestClientMock.get(BEHANDLING_ALLE_URI, FpsakKlient.ListeAvFpsakBehandlingInfoDto.class)).thenReturn(liste);

        boolean erFinnesIFpsak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIFpsak).isTrue();
    }

    @Test
    public void skal_returnere_tom_hvis_finnes_ikke_behandling_i_fpsak() {
        when(oidcRestClientMock.get(BEHANDLING_ALLE_URI, FpsakKlient.ListeAvFpsakBehandlingInfoDto.class)).thenReturn(new FpsakKlient.ListeAvFpsakBehandlingInfoDto());

        boolean erFinnesIFpsak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIFpsak).isFalse();
    }

    @Test
    public void skal_returnere_tilbakekreving_valg() {
        TilbakekrevingValgDto tilbakekrevingValgDto = new TilbakekrevingValgDto(VidereBehandling.TILBAKEKREV_I_INFOTRYGD);
        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, FpsakBehandlingInfoDto.class)).thenReturn(Optional.of(dokumentinfoDto()));
        when(oidcRestClientMock.getReturnsOptional(TILBAKEKREVING_VALG_URI, TilbakekrevingValgDto.class)).thenReturn(Optional.of(tilbakekrevingValgDto));

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isPresent();
        assertThat(valgDto.get().getVidereBehandling()).isEqualByComparingTo(VidereBehandling.TILBAKEKREV_I_INFOTRYGD);
    }

    @Test
    public void skal_returnere_tom_tilbakekreving_valg() {
        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, FpsakBehandlingInfoDto.class)).thenReturn(Optional.of(dokumentinfoDto()));
        when(oidcRestClientMock.getReturnsOptional(TILBAKEKREVING_VALG_URI, TilbakekrevingValgDto.class)).thenReturn(Optional.empty());

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
        return Arrays.asList(personOpplysningerRessursLink, tilbakekrevingvalgRessursLink, varselTekstRessursLink);
    }

}
