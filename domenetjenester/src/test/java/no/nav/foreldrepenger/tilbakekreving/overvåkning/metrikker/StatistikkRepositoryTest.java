package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;

@CdiDbAwareTest
class StatistikkRepositoryTest {

    @BeforeAll
    static void beforeAll() {
        System.setProperty("app.name", "k9-tilbake");
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty("app.name");
    }

    @Inject
    private EntityManager entityManager;

    @Inject
    private StatistikkRepository statistikkRepository;

    @Test
    void skal_kjøre_uten_feil() {
        statistikkRepository.hentAlle();
    }
}