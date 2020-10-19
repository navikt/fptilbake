package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.vedtak.feil.FeilFactory;

public class TpsTjenesteTest {

    private static Map<AktørId, PersonIdent> FNR_VED_AKTØR_ID = new HashMap<>();
    private static Map<PersonIdent, AktørId> AKTØR_ID_VED_FNR = new HashMap<>();

    private static final AktørId AKTØR_ID = new AktørId("1");
    private static final AktørId ENDRET_AKTØR_ID = new AktørId("2");
    private static final AktørId AKTØR_ID_SOM_TRIGGER_EXCEPTION = new AktørId("10");
    private static final PersonIdent FNR = new PersonIdent("12345678901");
    private static final PersonIdent ENDRET_FNR = new PersonIdent("02345678901");
    private static final LocalDate FØDSELSDATO = LocalDate.of(1992, Month.OCTOBER, 13);

    private static final String NAVN = "Anne-Berit Hjartdal";

    private TpsTjeneste tpsTjeneste;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Before
    public void oppsett() {
        FNR_VED_AKTØR_ID.put(AKTØR_ID, FNR);
        FNR_VED_AKTØR_ID.put(ENDRET_AKTØR_ID, ENDRET_FNR);
        AKTØR_ID_VED_FNR.put(FNR, AKTØR_ID);
        AKTØR_ID_VED_FNR.put(ENDRET_FNR, ENDRET_AKTØR_ID);

        tpsTjeneste = new TpsTjeneste(new TpsAdapterMock());
    }

    @Test
    public void skal_ikke_hente_bruker_for_ukjent_aktør() {
        Optional<Personinfo> funnetBruker = tpsTjeneste.hentBrukerForAktør(new AktørId("666"));
        assertThat(funnetBruker).isNotPresent();
    }

    @Test
    public void skal_kaste_feil_ved_tjenesteexception_dersom_aktør_ikke_er_cachet() {
        expectedException.expect(TpsException.class);

        tpsTjeneste.hentBrukerForAktør(AKTØR_ID_SOM_TRIGGER_EXCEPTION);
    }

    private class TpsAdapterMock implements TpsAdapter {
        private static final String ADR1 = "Adresselinje1";
        private static final String ADR2 = "Adresselinje2";
        private static final String ADR3 = "Adresselinje3";
        private static final String POSTNR = "1234";
        private static final String POSTSTED = "Oslo";
        private static final String LAND = "Norge";

        @Override
        public Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId) {
            if (aktørId == AKTØR_ID_SOM_TRIGGER_EXCEPTION) {
                throw new TpsException(FeilFactory.create(TpsFeilmeldinger.class)
                        .tpsUtilgjengeligSikkerhetsbegrensning(new HentPersonSikkerhetsbegrensning("String", null)));
            }
            return Optional.ofNullable(FNR_VED_AKTØR_ID.get(aktørId));
        }

        @Override
        public Personinfo hentKjerneinformasjon(PersonIdent fnr, AktørId aktørId) {
            if (!AKTØR_ID_VED_FNR.containsKey(fnr)) {
                return null;
            }
            return new Personinfo.Builder()
                    .medAktørId(aktørId)
                    .medPersonIdent(fnr)
                    .medNavn(NAVN)
                    .medFødselsdato(FØDSELSDATO)
                    .medNavBrukerKjønn(KVINNE)
                    .build();
        }

        @Override
        public Adresseinfo hentAdresseinformasjon(PersonIdent fnr) {
            return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, fnr, NAVN)
                    .medAdresselinje1(ADR1)
                    .medAdresselinje2(ADR2)
                    .medAdresselinje3(ADR3)
                    .medPostNr(POSTNR)
                    .medPoststed(POSTSTED)
                    .medLand(LAND)
                    .build();
        }

        @Override
        public Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent) {
            return Optional.ofNullable(new AktørId(personIdent.getIdent()));
        }

    }
}
