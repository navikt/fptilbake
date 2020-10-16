package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.GeografiKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.PoststedKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.SpråkKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
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
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personstatus;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personstatuser;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Spraak;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class TpsOversetterTest {

    private static final String POSTNUMMER = "1234";

    private static final String GATEADRESSE1 = "Gaten 13 B";

    private static final String USTRUKTURERT_GATEADRESSE1 = "Ustrukturert adresselinje 1";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private NavBrukerKodeverkRepository brukerKodeverkRepository = new NavBrukerKodeverkRepository(new KodeverkRepositoryImpl(repoRule.getEntityManager()));
    private GeografiKodeverkRepository bgKodeverkRepository = new GeografiKodeverkRepository(repoRule.getEntityManager());
    private SpråkKodeverkRepository språkKodeverkRepository = new SpråkKodeverkRepository(new KodeverkRepositoryImpl(repoRule.getEntityManager()));
    private PoststedKodeverkRepository poststedKodeverkRepository = new PoststedKodeverkRepository(repoRule.getEntityManager());

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

    @Before
    public void oppsett() {

        Landkoder landkodeNorge = new Landkoder();
        landkodeNorge.setValue("NOR");
        Statsborgerskap statsborgerskap = new Statsborgerskap();
        statsborgerskap.setLand(landkodeNorge);

        NorskIdent ident = new NorskIdent();
        ident.setIdent("123");
        PersonIdent pi = new PersonIdent();
        pi.setIdent(ident);

        when(bruker.getAktoer()).thenReturn(pi);
        when(bruker.getStatsborgerskap()).thenReturn(statsborgerskap);
        tpsAdresseOversetter = new TpsAdresseOversetter(brukerKodeverkRepository, poststedKodeverkRepository);
        tpsOversetter = new TpsOversetter(brukerKodeverkRepository, bgKodeverkRepository, språkKodeverkRepository, tpsAdresseOversetter);
        Matrikkelnummer matrikkelnummer = new Matrikkelnummer();
        matrikkelnummer.setBruksnummer("bnr");
        matrikkelnummer.setFestenummer("fnr");
        matrikkelnummer.setGaardsnummer("gnr");
        matrikkelnummer.setSeksjonsnummer("snr");
        matrikkelnummer.setUndernummer("unr");
        when(matrikkeladresse.getMatrikkelnummer()).thenReturn(matrikkelnummer);
        Postnummer poststed = new Postnummer();
        poststed.setKodeRef(POSTNUMMER);
        poststed.setValue(POSTNUMMER);
        when(matrikkeladresse.getPoststed()).thenReturn(poststed);

        when(postboksAdresse.getLandkode()).thenReturn(landkodeNorge);
        when(postboksAdresse.getPostboksnummer()).thenReturn("47");
        when(postboksAdresse.getPoststed()).thenReturn(poststed);

        when(gateadresse.getGatenavn()).thenReturn("Gaten");
        when(gateadresse.getHusnummer()).thenReturn(13);
        when(gateadresse.getHusbokstav()).thenReturn("B");
        when(gateadresse.getPoststed()).thenReturn(poststed);

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
        when(midlertidigPostadresseNorge.getPostleveringsPeriode()).thenReturn(gyldighetsperiode);
        Personnavn personnavn = new Personnavn();
        personnavn.setSammensattNavn("Ole Olsen");
        when(bruker.getPersonnavn()).thenReturn(personnavn);

        UstrukturertAdresse adresse = new UstrukturertAdresse();
        adresse.setAdresselinje1("Test utlandsadresse");
        when(midlertidigPostadresseUtland.getUstrukturertAdresse()).thenReturn(adresse);

        when(ustrukturertAdresse.getAdresselinje1()).thenReturn(USTRUKTURERT_GATEADRESSE1);
        when(postadresse.getUstrukturertAdresse()).thenReturn(ustrukturertAdresse);
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

    @Test(expected = VLException.class)
    public void testPostnummerAdresseEksistererIkke() {
        when(bruker.getGjeldendePostadressetype()).thenReturn(new Postadressetyper());
        tpsOversetter.tilAdresseinfo(bruker);
    }

    @Test
    public void testUtlandsadresse() {
        when(bruker.getMidlertidigPostadresse()).thenReturn(midlertidigPostadresseUtland);
        String utlandsadresse = tpsAdresseOversetter.finnUtlandsadresseFor(bruker);
        assertThat(utlandsadresse).isEqualTo("Test utlandsadresse");
    }

    @Test
    public void skal_ikke_feile_når_bruker_ikke_har_utlandsadresse() {
        when(bruker.getMidlertidigPostadresse()).thenReturn(null);
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
        when(postboksAdresse.getTilleggsadresseType()).thenReturn("TilleggsadresseType");

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

    @Test
    public void skal_ha_med_foretrukket_språk_når_finnes() throws Exception {
        tpsOversetter = new TpsOversetter(brukerKodeverkRepository, bgKodeverkRepository, språkKodeverkRepository, tpsAdresseOversetter);

        Spraak språk = new Spraak();
        språk.setValue("NN");
        when(bruker.getMaalform()).thenReturn(språk);
        Personinfo personinfo = tpsOversetter.tilBrukerInfo(new AktørId("123"), bruker);
        assertThat(personinfo.getForetrukketSpråk()).isEqualTo(Språkkode.nn);
    }

    @Test
    public void skal_default_til_bokmål_om_foretrukket_språk_ikke_er_satt() throws Exception {

        tpsOversetter = new TpsOversetter(brukerKodeverkRepository, bgKodeverkRepository, språkKodeverkRepository, tpsAdresseOversetter);

        when(bruker.getMaalform()).thenReturn(null);
        Personinfo personinfo = tpsOversetter.tilBrukerInfo(new AktørId("123"), bruker);
        assertThat(personinfo.getForetrukketSpråk()).isEqualTo(Språkkode.nb);
    }

    @Test
    public void skal_defaulte_til_bokmål_om_foretrukket_språk_ikke_er_støttet() throws Exception {

        tpsOversetter = new TpsOversetter(brukerKodeverkRepository, bgKodeverkRepository, språkKodeverkRepository, tpsAdresseOversetter);

        Spraak språk = new Spraak();
        språk.setValue("SVORSK");
        when(bruker.getMaalform()).thenReturn(språk);
        Personinfo personinfo = tpsOversetter.tilBrukerInfo(new AktørId("123"), bruker);
        assertThat(personinfo.getForetrukketSpråk()).isEqualTo(Språkkode.nb);
    }

    @Test
    public void skal_defaulte_til_bokmål_om_foretrukket_språk_er_NO() throws Exception {
        GeografiKodeverkRepository grunnlagRepo = Mockito.mock(GeografiKodeverkRepository.class);

        tpsOversetter = new TpsOversetter(brukerKodeverkRepository, bgKodeverkRepository, språkKodeverkRepository, tpsAdresseOversetter);

        Spraak språk = new Spraak();
        språk.setValue("NO");
        when(bruker.getMaalform()).thenReturn(språk);
        Personinfo personinfo = tpsOversetter.tilBrukerInfo(new AktørId("123"), bruker);
        assertThat(personinfo.getForetrukketSpråk()).isEqualTo(Språkkode.nb);
    }

    @Test
    public void skal_oversette_statsborgerskap() {
        // Arrange
        GeografiKodeverkRepository grunnlagRepo = Mockito.mock(GeografiKodeverkRepository.class);
        no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder norge = no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder.NOR;
        when(grunnlagRepo.finnLandkode(norge.getKode())).thenReturn(norge);
        tpsOversetter = new TpsOversetter(brukerKodeverkRepository, grunnlagRepo, språkKodeverkRepository, tpsAdresseOversetter);

        // Act
        Personinfo personinfo = tpsOversetter.tilBrukerInfo(new AktørId("123"), bruker);

        // Assert
        assertThat(personinfo.getLandkode()).isEqualTo(norge);
        assertThat(personinfo.getStatsborgerskap().getLandkode()).isEqualTo(norge.getKode());
    }

    private void initMockBrukerPersonstatus(PersonstatusType personstatusType) {
        Personstatus tpsPersonstatus = new Personstatus();
        Personstatuser tpsPersonstatuser = new Personstatuser();
        tpsPersonstatuser.setValue(personstatusType.getKode());
        tpsPersonstatus.setPersonstatus(tpsPersonstatuser);
        when(bruker.getPersonstatus()).thenReturn(tpsPersonstatus);
    }

    private void initMockBostedsadresseMedPostboksAdresseForBruker() {
        when(bruker.getGjeldendePostadressetype()).thenReturn(tilPostadressetyper("BOSTEDSADRESSE"));
        when(bruker.getBostedsadresse()).thenReturn(bostedsadresse);
        when(bostedsadresse.getStrukturertAdresse()).thenReturn(postboksAdresse);
    }

    private void leggPåAndrePåkrevdeFelter() {
        Kjoenn kjønn = new Kjoenn();
        Kjoennstyper kjønnstype = new Kjoennstyper();
        kjønnstype.setValue("K");
        kjønn.setKjoenn(kjønnstype);
        when(bruker.getKjoenn()).thenReturn(kjønn);

        initMockBrukerPersonstatus(PersonstatusType.BOSA);

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder land = new Landkoder();
        land.setValue("NOR");
        statsborgerskap.setLand(land);
        when(bruker.getStatsborgerskap()).thenReturn(statsborgerskap);

        initMockBostedsadresseMedPostboksAdresseForBruker();

        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(DateUtil.convertToXMLGregorianCalendar(LocalDate.now()));
        when(bruker.getFoedselsdato()).thenReturn(foedselsdato);
    }

    private Postadressetyper tilPostadressetyper(String type) {
        Postadressetyper postadresseType = new Postadressetyper();
        postadresseType.setValue(type);
        return postadresseType;
    }

}
