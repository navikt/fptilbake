package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.GeografiKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.SpråkKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class TpsAdapterImplTest {

    private TpsAdapterImpl tpsAdapterImpl;

    private AktørConsumerMedCache aktørConsumerMock = Mockito.mock(AktørConsumerMedCache.class);
    private PersonConsumer personProxyServiceMock = Mockito.mock(PersonConsumer.class);

    TpsTjeneste tpsTjeneste = Mockito.mock(TpsTjeneste.class);
    private final AktørId aktørId = new AktørId("1337");
    private final PersonIdent fnr = new PersonIdent("11112222333");

    @Before
    public void setup() {
        TpsAdresseOversetter tpsAdresseOversetter = new TpsAdresseOversetter(lagMockNavBrukerKodeverkRepository(), null);

        TpsOversetter tpsOversetter = new TpsOversetter(
            lagMockNavBrukerKodeverkRepository(), mock(GeografiKodeverkRepository.class), lagMockSpråkKodeverkRepository(), tpsAdresseOversetter);
        tpsAdapterImpl = new TpsAdapterImpl(aktørConsumerMock, personProxyServiceMock, tpsOversetter);
    }

    private NavBrukerKodeverkRepository lagMockNavBrukerKodeverkRepository() {
        NavBrukerKodeverkRepository mockNavBrukerKodeverkRepository = mock(NavBrukerKodeverkRepository.class);
        when(mockNavBrukerKodeverkRepository.finnBrukerKjønn(any(String.class))).thenReturn(NavBrukerKjønn.KVINNE);
        return mockNavBrukerKodeverkRepository;
    }

    private SpråkKodeverkRepository lagMockSpråkKodeverkRepository() {
        SpråkKodeverkRepository språkRepo = Mockito.mock(SpråkKodeverkRepository.class);
        when(språkRepo.finnSpråkMedKodeverkEiersKode("NN")).thenReturn(Optional.of(Språkkode.nn));
        when(språkRepo.finnSpråkMedKodeverkEiersKode("NB")).thenReturn(Optional.of(Språkkode.nb));
        return språkRepo;
    }

    @Test
    public void test_hentIdentForAktørId_normal() throws Exception {
        Mockito.when(aktørConsumerMock.hentPersonIdentForAktørId("1")).thenReturn(Optional.of("1337"));
        Optional<PersonIdent> optIdent = tpsAdapterImpl.hentIdentForAktørId(new AktørId("1"));
        assertThat(optIdent.get()).isEqualTo(new PersonIdent("1337"));
    }

    @Test
    public void test_hentIdentForAktørId_ikkeFunnet() throws Exception {
        Mockito.when(aktørConsumerMock.hentPersonIdentForAktørId("1")).thenReturn(Optional.empty());
        Optional<PersonIdent> optIdent = tpsAdapterImpl.hentIdentForAktørId(new AktørId("1"));
        assertThat(optIdent).isNotPresent();
    }

    @Test
    public void test_hentKjerneinformasjon_normal() throws Exception {
        AktørId aktørId = new AktørId("1337");
        PersonIdent fnr = new PersonIdent("11112222333");
        String navn = "John Doe";
        LocalDate fødselsdato = LocalDate.of(1343, 12, 12);
        NavBrukerKjønn kjønn = NavBrukerKjønn.KVINNE;

        HentPersonResponse response = new HentPersonResponse();
        Bruker person = new Bruker();
        response.setPerson(person);
        Mockito.when(personProxyServiceMock.hentPersonResponse(Mockito.any())).thenReturn(response);

        TpsOversetter tpsOversetterMock = Mockito.mock(TpsOversetter.class);
        Personinfo personinfo0 = new Personinfo.Builder()
            .medPersonIdent(fnr)
            .medNavn(navn)
            .medFødselsdato(fødselsdato)
            .medNavBrukerKjønn(kjønn)
            .medAktørId(aktørId)
            .medForetrukketSpråk(Språkkode.nb)
            .build();

        Mockito.when(tpsOversetterMock.tilBrukerInfo(Mockito.any(AktørId.class), eq(person))).thenReturn(personinfo0);
        tpsAdapterImpl = new TpsAdapterImpl(aktørConsumerMock, personProxyServiceMock, tpsOversetterMock);

        Personinfo personinfo = tpsAdapterImpl.hentKjerneinformasjon(fnr, aktørId);
        assertNotNull(personinfo);
        assertThat(personinfo.getAktørId()).isEqualTo(aktørId);
        assertThat(personinfo.getPersonIdent()).isEqualTo(fnr);
        assertThat(personinfo.getNavn()).isEqualTo(navn);
        assertThat(personinfo.getFødselsdato()).isEqualTo(fødselsdato);
    }

    @Test(expected = TekniskException.class)
    public void skal_få_exception_når_tjenesten_ikke_kan_finne_personen() throws Exception {
        Mockito.when(personProxyServiceMock.hentPersonResponse(Mockito.any()))
            .thenThrow(new HentPersonPersonIkkeFunnet(null, null));

        tpsAdapterImpl.hentKjerneinformasjon(fnr, aktørId);
    }

    @Test(expected = ManglerTilgangException.class)
    public void skal_få_exception_når_tjenesten_ikke_kan_aksesseres_pga_manglende_tilgang() throws Exception {
        when(personProxyServiceMock.hentPersonResponse(any(HentPersonRequest.class)))
            .thenThrow(new HentPersonSikkerhetsbegrensning(null, null));

        tpsAdapterImpl.hentKjerneinformasjon(fnr, aktørId);
    }

    @Test
    public void test_hentAdresseinformasjon_normal() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        HentPersonResponse response = new HentPersonResponse();
        Bruker person = new Bruker();
        response.setPerson(person);

        ArgumentCaptor<HentPersonRequest> captor = ArgumentCaptor.forClass(HentPersonRequest.class);
        when(personProxyServiceMock.hentPersonResponse(captor.capture())).thenReturn(response);

        final String addresse = "Veien 17";

        TpsOversetter tpsOversetterMock = Mockito.mock(TpsOversetter.class);
        Adresseinfo.Builder builder = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, new PersonIdent("11112222333"), "Tjoms");
        Adresseinfo adresseinfoExpected = builder.medAdresselinje1(addresse).build();

        when(tpsOversetterMock.tilAdresseinfo(eq(person))).thenReturn(adresseinfoExpected);
        tpsAdapterImpl = new TpsAdapterImpl(aktørConsumerMock, personProxyServiceMock, tpsOversetterMock);

        Adresseinfo adresseinfoActual = tpsAdapterImpl.hentAdresseinformasjon(fnr);

        assertThat(adresseinfoActual).isNotNull();
        assertThat(adresseinfoActual).isEqualTo(adresseinfoExpected);
        assertThat(adresseinfoActual.getAdresselinje1()).isEqualTo(adresseinfoExpected.getAdresselinje1());
    }

    @Test(expected = TekniskException.class)
    public void test_hentAdresseinformasjon_personIkkeFunnet() throws Exception {
        when(personProxyServiceMock.hentPersonResponse(any())).thenThrow(new HentPersonPersonIkkeFunnet(null, null));

        tpsAdapterImpl.hentAdresseinformasjon(fnr);
    }

    @Test(expected = ManglerTilgangException.class)
    public void test_hentAdresseinformasjon_manglende_tilgang() throws Exception {
        when(personProxyServiceMock.hentPersonResponse(any())).thenThrow(new HentPersonSikkerhetsbegrensning(null, null));

        tpsAdapterImpl.hentAdresseinformasjon(fnr);
    }
}
