package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.PoststedKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoenn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoennstyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkelnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@CdiDbAwareTest
public class TpsOversetterTest {

    private static final String GATEADRESSE1 = "Gaten 13 B";
    private static final String POSTNUMMER = "1234";
    private static final String USTRUKTURERT_GATEADRESSE1 = "Ustrukturert adresselinje 1";

    @Mock
    private Bruker bruker;
    @Mock
    private MidlertidigPostadresseNorge midlertidigPostadresseNorge;
    @Mock
    private MidlertidigPostadresseUtland midlertidigPostadresseUtland;
    @Mock
    private Matrikkeladresse matrikkeladresse;
    @Mock
    private Gateadresse gateadresse;
    @Mock
    private Postadresse postadresse;
    @Mock
    private UstrukturertAdresse ustrukturertAdresse;
    @Mock
    private Bostedsadresse bostedsadresse;
    @Mock
    private PostboksadresseNorsk postboksAdresse;

    private TpsOversetter tpsOversetter;
    private TpsAdresseOversetter tpsAdresseOversetter;

    @BeforeEach
    public void oppsett(EntityManager entityManager) {

        Landkoder landkodeNorge = new Landkoder();
        landkodeNorge.setValue("NOR");
        Statsborgerskap statsborgerskap = new Statsborgerskap();
        statsborgerskap.setLand(landkodeNorge);

        NorskIdent ident = new NorskIdent();
        ident.setIdent("123");
        PersonIdent pi = new PersonIdent();
        pi.setIdent(ident);

        lenient().when(bruker.getAktoer()).thenReturn(pi);
        lenient().when(bruker.getStatsborgerskap()).thenReturn(statsborgerskap);
        var poststedKodeverkRepository = new PoststedKodeverkRepository(entityManager);
        tpsAdresseOversetter = new TpsAdresseOversetter(poststedKodeverkRepository);
        tpsOversetter = new TpsOversetter(tpsAdresseOversetter);
        Matrikkelnummer matrikkelnummer = new Matrikkelnummer();
        matrikkelnummer.setBruksnummer("bnr");
        matrikkelnummer.setFestenummer("fnr");
        matrikkelnummer.setGaardsnummer("gnr");
        matrikkelnummer.setSeksjonsnummer("snr");
        matrikkelnummer.setUndernummer("unr");
        lenient().when(matrikkeladresse.getMatrikkelnummer()).thenReturn(matrikkelnummer);
        Postnummer poststed = new Postnummer();
        poststed.setKodeRef(POSTNUMMER);
        poststed.setValue(POSTNUMMER);
        lenient().when(matrikkeladresse.getPoststed()).thenReturn(poststed);

        lenient().when(postboksAdresse.getLandkode()).thenReturn(landkodeNorge);
        lenient().when(postboksAdresse.getPostboksnummer()).thenReturn("47");
        lenient().when(postboksAdresse.getPoststed()).thenReturn(poststed);

        lenient().when(gateadresse.getGatenavn()).thenReturn("Gaten");
        lenient().when(gateadresse.getHusnummer()).thenReturn(13);
        lenient().when(gateadresse.getHusbokstav()).thenReturn("B");
        lenient().when(gateadresse.getPoststed()).thenReturn(poststed);

        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        LocalDate fom = LocalDate.now().minusDays(1);
        LocalDate tom = LocalDate.now().plusDays(1);
        try {
            DatatypeFactory factory = DatatypeFactory.newInstance();
            gyldighetsperiode.setFom(factory.newXMLGregorianCalendar(GregorianCalendar.from(fom.atStartOfDay(ZoneId.systemDefault()))));
            gyldighetsperiode.setTom(factory.newXMLGregorianCalendar(GregorianCalendar.from(tom.atStartOfDay(ZoneId.systemDefault()))));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Ugyldig format", e);
        }
        lenient().when(midlertidigPostadresseNorge.getPostleveringsPeriode()).thenReturn(gyldighetsperiode);
        Personnavn personnavn = new Personnavn();
        personnavn.setSammensattNavn("Ole Olsen");
        lenient().when(bruker.getPersonnavn()).thenReturn(personnavn);

        UstrukturertAdresse adresse = new UstrukturertAdresse();
        adresse.setAdresselinje1("Test utlandsadresse");
        lenient().when(midlertidigPostadresseUtland.getUstrukturertAdresse()).thenReturn(adresse);

        lenient().when(ustrukturertAdresse.getAdresselinje1()).thenReturn(USTRUKTURERT_GATEADRESSE1);
        lenient().when(postadresse.getUstrukturertAdresse()).thenReturn(ustrukturertAdresse);
        leggPåAndrePåkrevdeFelter();
    }

    @Test
    public void testPostnummerAdresse() {
        initMockBostedsadresseMedPostboksAdresseForBruker();

        Adresseinfo adresseinfo = tpsOversetter.tilAdresseinfo(bruker);

        assertThat(adresseinfo).isNotNull();
        assertThat(adresseinfo.getAdresselinje1()).isEqualTo("Postboks 47");
        assertThat(adresseinfo.getAdresselinje2()).isNull();
        assertThat(adresseinfo.getAdresselinje3()).isNull();
        assertThat(adresseinfo.getAdresselinje4()).isNull();
        assertThat(adresseinfo.getLand()).isEqualTo("NOR");
        assertThat(adresseinfo.getPostNr()).isEqualTo("1234");
        assertThat(adresseinfo.getPoststed()).isEqualTo("UKJENT");
    }

    @Test
    public void testPostnummerAdresseEksistererIkke() {
        when(bruker.getGjeldendePostadressetype()).thenReturn(new Postadressetyper());
        assertThrows(VLException.class, () -> tpsOversetter.tilAdresseinfo(bruker));
    }

    @Test
    public void testUtlandsadresse() {
        when(bruker.getMidlertidigPostadresse()).thenReturn(midlertidigPostadresseUtland);
        String utlandsadresse = tpsAdresseOversetter.finnUtlandsadresseFor(bruker);
        assertThat(utlandsadresse).isEqualTo("Test utlandsadresse");
    }

    @Test
    public void skal_ikke_feile_når_bruker_ikke_har_utlandsadresse() {
        String utlandsadresse = tpsAdresseOversetter.finnUtlandsadresseFor(bruker);
        assertThat(utlandsadresse).isNull();
    }

    @Test
    public void testPostnummerAdresseMedPostboksanlegg() {
        initMockBostedsadresseMedPostboksAdresseForBruker();
        when(postboksAdresse.getPostboksanlegg()).thenReturn("Etterstad");

        Adresseinfo adresseinfo = tpsOversetter.tilAdresseinfo(bruker);

        assertThat(adresseinfo).isNotNull();
        assertThat(adresseinfo.getAdresselinje1()).isEqualTo("Postboks 47 Etterstad");
        assertThat(adresseinfo.getAdresselinje2()).isNull();
        assertThat(adresseinfo.getAdresselinje3()).isNull();
        assertThat(adresseinfo.getAdresselinje4()).isNull();
        assertThat(adresseinfo.getLand()).isEqualTo("NOR");
        assertThat(adresseinfo.getPostNr()).isEqualTo("1234");
        assertThat(adresseinfo.getPoststed()).isEqualTo("UKJENT");
    }

    @Test
    public void testPostnummerAdresseMedTilleggsadresse() {
        initMockBostedsadresseMedPostboksAdresseForBruker();
        when(postboksAdresse.getTilleggsadresse()).thenReturn("Tilleggsadresse");

        Adresseinfo adresseinfo = tpsOversetter.tilAdresseinfo(bruker);

        assertThat(adresseinfo).isNotNull();
        assertThat(adresseinfo.getAdresselinje1()).isEqualTo("Tilleggsadresse");
        assertThat(adresseinfo.getAdresselinje2()).isEqualTo("Postboks 47");
        assertThat(adresseinfo.getAdresselinje3()).isNull();
        assertThat(adresseinfo.getAdresselinje4()).isNull();
        assertThat(adresseinfo.getLand()).isEqualTo("NOR");
        assertThat(adresseinfo.getPostNr()).isEqualTo("1234");
        assertThat(adresseinfo.getPoststed()).isEqualTo("UKJENT");
    }

    @Test
    public void testMidlertidigMatrikkelAdresseNorge() {
        when(bruker.getGjeldendePostadressetype()).thenReturn(tilPostadressetyper("MIDLERTIDIG_POSTADRESSE_NORGE"));
        when(midlertidigPostadresseNorge.getStrukturertAdresse()).thenReturn(matrikkeladresse);
        when(bruker.getMidlertidigPostadresse()).thenReturn(midlertidigPostadresseNorge);
        Adresseinfo adresseinfo = tpsOversetter.tilAdresseinfo(bruker);
        assertThat(adresseinfo).isNotNull();
        assertThat(adresseinfo.getPostNr()).isEqualTo(POSTNUMMER);
    }

    @Test
    public void testMidlertidigGateAdresseNorge() {
        when(bruker.getGjeldendePostadressetype()).thenReturn(tilPostadressetyper("MIDLERTIDIG_POSTADRESSE_NORGE"));
        when(midlertidigPostadresseNorge.getStrukturertAdresse()).thenReturn(gateadresse);
        when(bruker.getMidlertidigPostadresse()).thenReturn(midlertidigPostadresseNorge);

        Adresseinfo adresseinfo = tpsOversetter.tilAdresseinfo(bruker);

        assertThat(adresseinfo).isNotNull();
        assertThat(adresseinfo.getAdresselinje1()).isEqualTo(GATEADRESSE1);
        assertThat(adresseinfo.getPostNr()).isEqualTo(POSTNUMMER);
    }

    @Test
    public void testUstrukturertAdresse() {
        when(bruker.getGjeldendePostadressetype()).thenReturn(tilPostadressetyper("POSTADRESSE"));
        when(bruker.getPostadresse()).thenReturn(postadresse);

        Adresseinfo adresseinfo = tpsOversetter.tilAdresseinfo(bruker);

        assertThat(adresseinfo).isNotNull();
        assertThat(adresseinfo.getAdresselinje1()).isEqualTo(USTRUKTURERT_GATEADRESSE1);
    }

    private void initMockBostedsadresseMedPostboksAdresseForBruker() {
        lenient().when(bruker.getGjeldendePostadressetype()).thenReturn(tilPostadressetyper("BOSTEDSADRESSE"));
        lenient().when(bruker.getBostedsadresse()).thenReturn(bostedsadresse);
        lenient().when(bostedsadresse.getStrukturertAdresse()).thenReturn(postboksAdresse);
    }

    private void leggPåAndrePåkrevdeFelter() {
        Kjoenn kjønn = new Kjoenn();
        Kjoennstyper kjønnstype = new Kjoennstyper();
        kjønnstype.setValue("K");
        kjønn.setKjoenn(kjønnstype);
        lenient().when(bruker.getKjoenn()).thenReturn(kjønn);

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder land = new Landkoder();
        land.setValue("NOR");
        statsborgerskap.setLand(land);
        lenient().when(bruker.getStatsborgerskap()).thenReturn(statsborgerskap);

        initMockBostedsadresseMedPostboksAdresseForBruker();

        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(DateUtil.convertToXMLGregorianCalendar(LocalDate.now()));
        lenient().when(bruker.getFoedselsdato()).thenReturn(foedselsdato);
    }

    private Postadressetyper tilPostadressetyper(String type) {
        Postadressetyper postadresseType = new Postadressetyper();
        postadresseType.setValue(type);
        return postadresseType;
    }

}
