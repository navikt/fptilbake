package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;

@CdiDbAwareTest
class StatistikkRepositoryTest {

    @BeforeAll
    static void setup() {
        System.setProperty("app.name", "k9-tilbake");
    }

    @AfterAll
    static void teardown() {
        System.clearProperty("app.name");
    }

    @Inject
    private StatistikkRepository statistikkRepository;

    @Test
    void skal_kjøre_uten_feil() {
        statistikkRepository.hentAlle();
    }

    @Test
    void skal_maskere_orgnr_fnr_aktørid() {
        Assertions.assertThat(
            StatistikkRepository.masker(" orgnr1=123123123 orgnr2=432123123 fnr=12312312312 fnr2 12312312321 aktørid 1231231231231")).isEqualTo(
            " orgnr1=MASKERT9 orgnr2=MASKERT9 fnr=MASKERT11 fnr2 MASKERT11 aktørid MASKERT13"
        );
    }

    @Test
    void skal_ikke_maskere() {
        Assertions.assertThat(StatistikkRepository.masker("kode=1313 linje 411 behandlingid 12412111")).isEqualTo(
            "kode=1313 linje 411 behandlingid 12412111"
        );
    }
}

