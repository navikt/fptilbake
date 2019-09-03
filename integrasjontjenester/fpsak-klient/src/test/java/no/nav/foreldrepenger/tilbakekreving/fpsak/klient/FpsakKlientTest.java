package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonadresseDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class FpsakKlientTest {

    private static final Long BEHANDLING_ID = 123456L;
    private static final Long FAGSAK_ID = 1234L;
    private static final String SAKSNUMMER = "1256436";
    private static final String VARSELTEKST = "varseltekst";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private static final String BASE_URI = "http://fpsak";
    private static final URI BEHANDLING_URI = URI.create(BASE_URI + "/fpsak/api/behandling/backend-root?uuid=" + BEHANDLING_UUID);
    private static final URI BEHANDLING_ALLE_URI = URI.create(BASE_URI + "/fpsak/api/behandlinger/alle?saksnummer=" + SAKSNUMMER);
    private static final URI PERSONOPPLYSNING_URI = URI.create(BASE_URI + "/fpsak/api/behandling/person/personopplysninger?uuid=" + BEHANDLING_UUID);
    private static final URI VARSELTEKST_URI = URI.create(BASE_URI + "/fpsak/api/behandling/tilbakekreving/varseltekst?uuid=" + BEHANDLING_UUID);
    private static final URI TILBAKEKREVING_VALG_URI = URI.create(BASE_URI + "/fpsak/api/behandling/tilbakekreving/valg?uuid=" + BEHANDLING_UUID);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OidcRestClient oidcRestClientMock = mock(OidcRestClient.class);

    private FpsakKlient klient = new FpsakKlient(oidcRestClientMock);

    @Test
    public void skal_hente_DokumentinfoDto() {
        EksternBehandlingsinfoDto returnDto = dokumentinfoDto();

        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, EksternBehandlingsinfoDto.class)).thenReturn(Optional.of(returnDto));
        when(oidcRestClientMock.getReturnsOptional(PERSONOPPLYSNING_URI, PersonopplysningDto.class)).thenReturn(Optional.of(personopplysningDto()));
        when(oidcRestClientMock.getReturnsOptional(VARSELTEKST_URI, String.class)).thenReturn(Optional.of(VARSELTEKST));

        Optional<EksternBehandlingsinfoDto> dokumentinfoDto = klient.hentBehandlingsinfo(BEHANDLING_UUID);

        assertThat(dokumentinfoDto).hasValue(returnDto);
        assertThat(dokumentinfoDto.get().getPersonopplysningDto()).isNotNull();
        assertThat(dokumentinfoDto.get().getVarseltekst()).isEqualTo(VARSELTEKST);
    }

    @Test
    public void skal_returnere_tom_optional_når_dokumentinfo_ikke_finnes() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.empty());

        Optional<EksternBehandlingsinfoDto> resultat = klient.hentBehandlingsinfo(BEHANDLING_UUID);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_returnere_hvis_finnes_behandling_i_fpsak() {
        EksternBehandlingsinfoDto eksternBehandlingInfo = dokumentinfoDto();

        JsonNode jsonNode = new ObjectMapper().convertValue(Lists.newArrayList(eksternBehandlingInfo), JsonNode.class);
        when(oidcRestClientMock.get(BEHANDLING_ALLE_URI, JsonNode.class)).thenReturn(jsonNode);

        boolean erFinnesIFpsak = klient.finnesBehandlingIFpsak(SAKSNUMMER, BEHANDLING_ID);
        assertThat(erFinnesIFpsak).isTrue();
    }

    @Test
    public void skal_returnere_tom_hvis_finnes_ikke_behandling_i_fpsak() {
        JsonNode jsonNode = new ObjectMapper().convertValue(Lists.newArrayList(), JsonNode.class);
        when(oidcRestClientMock.get(BEHANDLING_ALLE_URI, JsonNode.class)).thenReturn(jsonNode);

        boolean erFinnesIFpsak = klient.finnesBehandlingIFpsak(SAKSNUMMER, BEHANDLING_ID);
        assertThat(erFinnesIFpsak).isFalse();
    }

    @Test
    public void skal_returnere_tilbakekreving_valg() {
        TilbakekrevingValgDto tilbakekrevingValgDto = new TilbakekrevingValgDto(VidereBehandling.TILBAKEKREV_I_INFOTRYGD);
        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, EksternBehandlingsinfoDto.class)).thenReturn(Optional.of(dokumentinfoDto()));
        when(oidcRestClientMock.getReturnsOptional(TILBAKEKREVING_VALG_URI, TilbakekrevingValgDto.class)).thenReturn(Optional.of(tilbakekrevingValgDto));

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID.toString());
        assertThat(valgDto).isPresent();
        assertThat(valgDto.get().getVidereBehandling()).isEqualByComparingTo(VidereBehandling.TILBAKEKREV_I_INFOTRYGD);
    }

    @Test
    public void skal_returnere_tom_tilbakekreving_valg() {
        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, EksternBehandlingsinfoDto.class)).thenReturn(Optional.of(dokumentinfoDto()));
        when(oidcRestClientMock.getReturnsOptional(TILBAKEKREVING_VALG_URI, TilbakekrevingValgDto.class)).thenReturn(Optional.empty());

        Optional<TilbakekrevingValgDto> valgDto = klient.hentTilbakekrevingValg(BEHANDLING_UUID.toString());
        assertThat(valgDto).isEmpty();
    }


    private EksternBehandlingsinfoDto dokumentinfoDto() {
        EksternBehandlingsinfoDto dto = new EksternBehandlingsinfoDto();
        dto.setFagsaktype(new KodeDto("kv", "kode", "navn"));
        dto.setPersonopplysningDto(personopplysningDto());
        dto.setBehandlendeEnhetId("4214");
        dto.setBehandlendeEnhetNavn("enhetnavn");
        dto.setAnsvarligSaksbehandler("saksbehandler");
        dto.setId(BEHANDLING_ID);
        dto.setLinks(resourcelinks());
        dto.setSaksnummer(SAKSNUMMER);
        dto.setFagsakId(FAGSAK_ID);
        dto.setVarseltekst(VARSELTEKST);
        return dto;
    }

    private PersonopplysningDto personopplysningDto() {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setFødselsnummer("fnr");
        dto.setNavn("navn navn");
        dto.setAktoerId("aktørId");
        dto.setHarVerge(false);
        dto.setAdresser(adresser());
        return dto;
    }

    private List<PersonadresseDto> adresser() {
        PersonadresseDto dto = new PersonadresseDto();
        dto.setAdresselinje1("adrl1");
        dto.setAdresselinje2("adrl2");
        dto.setAdresselinje3("adrl3");
        dto.setAdresseType(new KodeDto("adresser", "kode", "navn"));
        dto.setPostnummer("0001");
        dto.setPoststed("sted");
        return Collections.singletonList(dto);
    }

    private List<BehandlingResourceLinkDto> resourcelinks() {
        BehandlingResourceLinkDto personOpplysningerRessursLink = new BehandlingResourceLinkDto();
        personOpplysningerRessursLink.setHref("/fpsak/api/behandling/person/personopplysninger?uuid=" + BEHANDLING_UUID.toString());
        personOpplysningerRessursLink.setRel("soeker-personopplysninger");

        BehandlingResourceLinkDto tilbakekrevingvalgRessursLink = new BehandlingResourceLinkDto();
        tilbakekrevingvalgRessursLink.setHref("/fpsak/api/behandling/tilbakekreving/valg?uuid=" + BEHANDLING_UUID.toString());
        tilbakekrevingvalgRessursLink.setRel("tilbakekreving-valg");

        BehandlingResourceLinkDto varselTekstRessursLink = new BehandlingResourceLinkDto();
        varselTekstRessursLink.setHref("/fpsak/api/behandling/tilbakekreving/varseltekst?uuid=" + BEHANDLING_UUID.toString());
        varselTekstRessursLink.setRel("tilbakekrevingsvarsel-fritekst");
        return Lists.newArrayList(personOpplysningerRessursLink, tilbakekrevingvalgRessursLink, varselTekstRessursLink);
    }

}
