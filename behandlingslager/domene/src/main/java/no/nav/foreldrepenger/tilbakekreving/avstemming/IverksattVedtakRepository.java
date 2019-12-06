package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class IverksattVedtakRepository {

    private EntityManager entityManager;

    IverksattVedtakRepository() {
        // For CDI
    }

    @Inject
    public IverksattVedtakRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(IverksattVedtak iverksattVedtak) {
        entityManager.persist(iverksattVedtak);
    }

    public List<IverksattVedtak> hent(LocalDate iverksattDato) {
        return null;
    }

}
