package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonadresseDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.VarseltekstDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class FpsakKlientTest {

    private static final Long BEHANDLING_ID = 123456L;
    private static final Long FAGSAK_ID = 1234L;
    private static final String SAKSNUMMER = "1256436";
    private static final String VARSELTEKST = "varseltekst";

    private static final URI BEHANDLING_URI = URI.create("http://fpsak/fpsak/api/behandlinger?behandlingId=" + BEHANDLING_ID);
    private static final URI FAGSAK_TYPE_URI = URI.create("http://fpsak/fpsak/api/fagsak?saksnummer=" + SAKSNUMMER);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OidcRestClient oidcRestClientMock = mock(OidcRestClient.class);

    private FpsakKlient klient = new FpsakKlient(oidcRestClientMock);

    @Test
    public void test_skal_hente_fagsakId() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.of(dokumentinfoDto()));

        Long result = klient.hentFagsakId(BEHANDLING_ID);

        assertThat(result).isEqualTo(FAGSAK_ID);
    }

    @Test
    public void skal_kaste_exception_når_behandling_ikke_finnes() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.empty());

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-532523");

        klient.hentFagsakId(BEHANDLING_ID);
    }

    @Test
    public void skal_hente_DokumentinfoDto() {
        EksternBehandlingsinfoDto returnDto = dokumentinfoDto();

        when(oidcRestClientMock.getReturnsOptional(BEHANDLING_URI, EksternBehandlingsinfoDto.class)).thenReturn(Optional.of(returnDto));
        when(oidcRestClientMock.get(FAGSAK_TYPE_URI, FagsakDto.class)).thenReturn(fagsakDto());

        Optional<EksternBehandlingsinfoDto> dokumentinfoDto = klient.hentBehandlingsinfo(BEHANDLING_ID, SAKSNUMMER);

        assertThat(dokumentinfoDto).hasValue(returnDto);
    }

    @Test
    public void skal_returnere_tom_optional_når_dokumentinfo_ikke_finnes() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.empty());

        Optional<EksternBehandlingsinfoDto> resultat = klient.hentBehandlingsinfo(BEHANDLING_ID, SAKSNUMMER);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_hente_varseltekst() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.of(new VarseltekstDto(VARSELTEKST)));

        Optional<String> resultat = klient.hentVarseltekst(BEHANDLING_ID);

        assertThat(resultat).hasValue(VARSELTEKST);
    }

    @Test
    public void skal_returnere_tom_optional_når_varseltekst_ikke_finnes() {
        when(oidcRestClientMock.getReturnsOptional(any(), any())).thenReturn(Optional.empty());

        Optional<String> resultat = klient.hentVarseltekst(BEHANDLING_ID);

        assertThat(resultat).isEmpty();
    }

    private EksternBehandlingsinfoDto dokumentinfoDto() {
        EksternBehandlingsinfoDto dto = new EksternBehandlingsinfoDto();
        dto.setFagsaktype(new KodeDto("kv", "kode", "navn"));
        dto.setPersonopplysningDto(personopplysningDto());
        dto.setBehandlendeEnhetId("4214");
        dto.setBehandlendeEnhetNavn("enhetnavn");
        dto.setSprakkode(Språkkode.nb);
        dto.setAnsvarligSaksbehandler("saksbehandler");
        dto.setId(BEHANDLING_ID);
        dto.setLinks(resourcelinks());
        dto.setSaksnummer(SAKSNUMMER);
        dto.setFagsakId(FAGSAK_ID);
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
        return Collections.emptyList();
    }

    private FagsakDto fagsakDto() {
        FagsakDto dto = new FagsakDto();
        dto.setSakstype(new KodeDto("fagsak", "kode", "navn"));
        return dto;
    }
}
