package no.nav.foreldrepenger.tilbakekreving.k9sak.klient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
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
import no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto.K9sakBehandlingInfoDto;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class K9sakKlientTest {

    private static final String SAKSNUMMER = "1256436";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();
    private static final Henvisning HENVISNING = K9HenvisningKonverterer.uuidTilHenvisning(BEHANDLING_UUID);

    private static final String BASE_URI = "http://k9-sak";
    private static final URI BEHANDLING_URI = URI.create(BASE_URI + "/k9/sak/api/behandling/backend-root?behandlingUuid=" + BEHANDLING_UUID);
    private static final URI BEHANDLING_ALLE_URI = URI.create(BASE_URI + "/k9/sak/api/behandlinger/alle?saksnummer=" + SAKSNUMMER);
    private static final URI PERSONOPPLYSNING_URI = URI.create(BASE_URI + "/k9/sak/api/behandling/person/personopplysninger?behandlingUuid=" + BEHANDLING_UUID);
    private static final URI TILBAKEKREVING_VALG_URI = URI.create(BASE_URI + "/k9/sak/api/behandling/tilbakekreving/valg?behandlingUuid=" + BEHANDLING_UUID);

    private final OidcRestClient oidcRestClientMock = mock(OidcRestClient.class);

    private final K9sakKlient klient = new K9sakKlient(oidcRestClientMock);

    @Test
    public void skal_hente_behandlingInfoDto() {
        K9sakBehandlingInfoDto returnDto = k9sakBehandlingInfoDto();

        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, K9sakBehandlingInfoDto.class)).thenReturn(Optional.of(returnDto));
        when(oidcRestClientMock.getReturnsOptional(PERSONOPPLYSNING_URI, PersonopplysningDto.class)).thenReturn(Optional.of(personopplysningDto()));

        SamletEksternBehandlingInfo dokumentinfoDto = klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER);

        assertThat(dokumentinfoDto.getGrunninformasjon()).isEqualTo(returnDto);
        assertThat(dokumentinfoDto.getPersonopplysninger()).isNotNull();
    }

    @Test
    public void skal_kaste_exception_når_k9sak_behandling_ikke_finnes() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.empty());

        Assertions.assertThrows(IntegrasjonException.class, () -> klient.hentBehandlingsinfo(BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER));
    }

    @Test
    public void skal_returnere_hvis_finnes_behandling_i_k9sak() {
        K9sakBehandlingInfoDto eksternBehandlingInfo = k9sakBehandlingInfoDto();
        K9sakKlient.ListeAvK9sakBehandlingInfoDto liste = new K9sakKlient.ListeAvK9sakBehandlingInfoDto();
        liste.add(eksternBehandlingInfo);
        when(oidcRestClientMock.get(BEHANDLING_ALLE_URI, K9sakKlient.ListeAvK9sakBehandlingInfoDto.class)).thenReturn(liste);

        boolean erFinnesIK9sak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIK9sak).isTrue();
    }

    @Test
    public void skal_returnere_tom_hvis_finnes_ikke_behandling_i_k9sak() {
        when(oidcRestClientMock.get(BEHANDLING_ALLE_URI, K9sakKlient.ListeAvK9sakBehandlingInfoDto.class)).thenReturn(new K9sakKlient.ListeAvK9sakBehandlingInfoDto());

        boolean erFinnesIK9sak = klient.finnesBehandlingIFagsystem(SAKSNUMMER, HENVISNING);
        assertThat(erFinnesIK9sak).isFalse();
    }

    @Test
    public void skal_returnere_tilbakekreving_valg() {
        TilbakekrevingValgDto tilbakekrevingValgDto = new TilbakekrevingValgDto(VidereBehandling.TILBAKEKR_OPPRETT);
        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, K9sakBehandlingInfoDto.class)).thenReturn(Optional.of(k9sakBehandlingInfoDto()));
        when(oidcRestClientMock.getReturnsOptional(TILBAKEKREVING_VALG_URI, TilbakekrevingValgDto.class)).thenReturn(Optional.of(tilbakekrevingValgDto));

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isPresent();
        assertThat(valgDto.get().getVidereBehandling()).isEqualByComparingTo(VidereBehandling.TILBAKEKR_OPPRETT);
    }

    @Test
    public void skal_returnere_tom_tilbakekreving_valg() {
        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, K9sakBehandlingInfoDto.class)).thenReturn(Optional.of(k9sakBehandlingInfoDto()));
        when(oidcRestClientMock.getReturnsOptional(TILBAKEKREVING_VALG_URI, TilbakekrevingValgDto.class)).thenReturn(Optional.empty());

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID);
        assertThat(valgDto).isEmpty();
    }

    private K9sakBehandlingInfoDto k9sakBehandlingInfoDto() {
        K9sakBehandlingInfoDto dto = new K9sakBehandlingInfoDto();
        dto.setBehandlendeEnhetId("4214");
        dto.setBehandlendeEnhetNavn("enhetnavn");
        dto.setUuid(BEHANDLING_UUID);
        dto.setLinks(resourcelinks());
        return dto;
    }

    private PersonopplysningDto personopplysningDto() {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setAktoerId("aktørId");
        return dto;
    }

    private List<BehandlingResourceLinkDto> resourcelinks() {
        BehandlingResourceLinkDto personOpplysningerRessursLink = new BehandlingResourceLinkDto();
        personOpplysningerRessursLink.setHref("/k9/sak/api/behandling/person/personopplysninger?behandlingUuid=" + BEHANDLING_UUID.toString());
        personOpplysningerRessursLink.setRel("soeker-personopplysninger");

        BehandlingResourceLinkDto tilbakekrevingvalgRessursLink = new BehandlingResourceLinkDto();
        tilbakekrevingvalgRessursLink.setHref("/k9/sak/api/behandling/tilbakekreving/valg?behandlingUuid=" + BEHANDLING_UUID.toString());
        tilbakekrevingvalgRessursLink.setRel("tilbakekrevingvalg");

        BehandlingResourceLinkDto varselTekstRessursLink = new BehandlingResourceLinkDto();
        varselTekstRessursLink.setHref("/k9/sak/api/behandling/tilbakekreving/varseltekst?behandlingUuid=" + BEHANDLING_UUID.toString());
        varselTekstRessursLink.setRel("tilbakekrevingsvarsel-fritekst");
        return List.of(personOpplysningerRessursLink, tilbakekrevingvalgRessursLink, varselTekstRessursLink);
    }
}
