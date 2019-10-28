package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VarselbrevSporingRepository {

    private EntityManager entityManager;

    VarselbrevSporingRepository() {
        //for CDI proxy
    }

    @Inject
    public VarselbrevSporingRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    public void lagreVarselbrevData(VarselbrevSporing varselbrevSporing) {
        entityManager.persist(varselbrevSporing);
    }

    public boolean harVarselBrevSendtForBehandlingId(Long behandlingId) {
        return !hentVarselbrevData(behandlingId).isEmpty();
    }

    public List<VarselbrevSporing> hentVarselbrevData(Long behandlingId) {
        TypedQuery<VarselbrevSporing> query = entityManager.createQuery("from VarselbrevSporing where behandling_id = :behandlingId", VarselbrevSporing.class);
        query.setParameter("behandlingId", behandlingId);
        return query.getResultList();
    }

    public Optional<VarselbrevSporing> hentSistSendtVarselbrev(Long behandlingId) {
        return hentVarselbrevData(behandlingId)
            .stream()
            .max(Comparator.comparing(VarselbrevSporing::getOpprettetTidspunkt));
    }

}
