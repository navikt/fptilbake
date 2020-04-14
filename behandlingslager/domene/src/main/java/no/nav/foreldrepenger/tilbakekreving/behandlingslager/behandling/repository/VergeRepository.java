package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VergeRepository {

    private EntityManager entityManager;

    VergeRepository() {
        // for CDI proxy
    }

    @Inject
    public VergeRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public long lagreVergeInformasjon(VergeEntitet vergeEntitet) {
        vergeEntitet.getVergeOrganisasjon().ifPresent(vergeOrganisasjon -> entityManager.persist(vergeOrganisasjon));
        entityManager.persist(vergeEntitet);
        return vergeEntitet.getId();
    }

    public VergeEntitet hentVergeInformasjon(Long vergeId) {
        return entityManager.find(VergeEntitet.class, vergeId);
    }
}
