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
public class BrevSporingRepository {

    private EntityManager entityManager;

    BrevSporingRepository() {
        //for CDI proxy
    }

    @Inject
    public BrevSporingRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    public void lagre(BrevSporing brevSporing) {
        entityManager.persist(brevSporing);
    }

    public boolean harVarselBrevSendtForBehandlingId(long behandlingId) {
        return !hentBrevData(behandlingId, BrevType.VARSEL_BREV).isEmpty();
    }

    public Optional<BrevSporing> hentSistSendtVarselbrev(long behandlingId) {
        return hentBrevData(behandlingId, BrevType.VARSEL_BREV)
            .stream()
            .max(Comparator.comparing(BrevSporing::getOpprettetTidspunkt));
    }

    public List<BrevSporing> hentBrevData(Long behandlingId, BrevType brevType) {
        TypedQuery<BrevSporing> query = entityManager.createQuery("from BrevSporing where behandling_id = :behandlingId and brevType=:brevType", BrevSporing.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("brevType", brevType);
        return query.getResultList();
    }

}
