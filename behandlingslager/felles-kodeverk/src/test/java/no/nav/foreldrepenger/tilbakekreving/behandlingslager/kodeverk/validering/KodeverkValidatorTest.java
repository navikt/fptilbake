package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.validering;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;

public class KodeverkValidatorTest {

    private static final String KEY_ARKIV_FILTYPE = "ARKIV_FILTYPE";
    private static final String KODEVERK_KODE_FEIL_MELDING = "kodeverk kode feilet validering";
    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testSkalPasserePåGyldigInput() {
        TestKodeliste kl = new TestKodeliste(KEY_ARKIV_FILTYPE, "PDF");
        Set<ConstraintViolation<TestKodeliste>> violations = validator.validate(kl);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testSkalPasserePåGyldigInputForliste() {
        TestListeAvKodeliste kl = new TestListeAvKodeliste(KEY_ARKIV_FILTYPE, "PDF");
        Set<ConstraintViolation<TestListeAvKodeliste>> violations = validator.validate(kl);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testSkalFeilePåTomInputForListe() {
        TestListeAvKodeliste kl = new TestListeAvKodeliste("", "");
        Set<ConstraintViolation<TestListeAvKodeliste>> violations = validator.validate(kl);
        assertEquals(1, violations.size());
        assertEquals(KODEVERK_KODE_FEIL_MELDING, violations.iterator().next().getMessage());
    }


    @Test
    public void testSkalFeilePåTomInput() {
        TestKodeliste kl = new TestKodeliste("", "");
        Set<ConstraintViolation<TestKodeliste>> violations = validator.validate(kl);
        assertEquals(1, violations.size());
        assertEquals(KODEVERK_KODE_FEIL_MELDING, violations.iterator().next().getMessage());
    }

    @Test
    public void testSkalFeilePåUgyldigeTegnIKode() {
        TestKodeliste kl = new TestKodeliste(KEY_ARKIV_FILTYPE, "P[^$");
        Set<ConstraintViolation<TestKodeliste>> violations = validator.validate(kl);
        assertEquals(1, violations.size());
        assertEquals(KODEVERK_KODE_FEIL_MELDING, violations.iterator().next().getMessage());
    }

    @Test
    public void testSkalFeilePåUgyldigeTegnIKodeverk() {
        TestKodeliste kl = new TestKodeliste("#¤#2aS", "PDF");
        Set<ConstraintViolation<TestKodeliste>> violations = validator.validate(kl);
        assertEquals(1, violations.size());
    }

    @Test
    public void testSkalFeilePåUgyldigeTegnINavn() {
        TestKodeverkTabell kt = new TestKodeverkTabell("PDF");
        Set<ConstraintViolation<TestKodeverkTabell>> violations = validator.validate(kt);
        assertEquals(1, violations.size());
    }

    @Test
    public void testSkalFeilePåUgyldigKodeLengde() {
        TestKodeliste kl = new TestKodeliste("",
                "asdfghjklqwertyuiasdfgdsfasjjjfhsjhkjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" +
                        "jjjjjjjjjjjjjasdfghjklqwertyuiasdfgdsfasjjjfhsjhkjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj"
        );
        Set<ConstraintViolation<TestKodeliste>> violations = validator.validate(kl);
        assertEquals(1, violations.size());
        assertEquals(KODEVERK_KODE_FEIL_MELDING, violations.iterator().next().getMessage());
    }

    static class KodeverkL extends Kodeliste {

        public KodeverkL(String kodeverk, String kode) {
            super(kode, kodeverk);
        }

    }

    static class KodeverkT extends KodeverkTabell {
        public KodeverkT(String kode) {
            super(kode);
        }
    }

    static class TestKodeliste {
        @ValidKodeverk
        private KodeverkL k;

        public TestKodeliste(String kodeverk, String kode) {
            k = new KodeverkL(kodeverk, kode);
        }

        public KodeverkL getK() {
            return k;
        }

        public void setK(KodeverkL k) {
            this.k = k;
        }
    }

    static class TestListeAvKodeliste {

        @Valid
        private List<@ValidKodeverk KodeverkL> k;

        public TestListeAvKodeliste() {
            k = null;
        }

        public TestListeAvKodeliste(String kodeverk, String kode) {
            k = new ArrayList<>();
            k.add(new KodeverkL(kodeverk, kode));
        }

        public List<KodeverkL> getK() {
            return k;
        }

        public void leggTilK(KodeverkL k) {
            if (this.k == null) {
                this.k = new ArrayList<>();
            }
            this.k.add(k);
        }
    }

    static class TestKodeverkTabell {
        @ValidKodeverk
        private KodeverkT t;

        public TestKodeverkTabell(String kode) {
            t = new KodeverkT(kode);
        }

        public KodeverkT getT() {
            return t;
        }

        public void setT(KodeverkT t) {
            this.t = t;
        }
    }
}
