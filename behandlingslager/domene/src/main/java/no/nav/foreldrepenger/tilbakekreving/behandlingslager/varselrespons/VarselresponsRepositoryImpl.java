package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons.VarselresponsRepositoryFeil.FACTORY;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VarselresponsRepositoryImpl implements VarselresponsRepository {

    private EntityManager entityManager;

    public VarselresponsRepositoryImpl() {}

    @Inject
    public VarselresponsRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public void lagre(Varselrespons varselrespons) {
        Varselrespons persister = varselrespons;
        Optional<Varselrespons> eksisterende = hentRespons(varselrespons.getBehandlingId());
        if (eksisterende.isPresent()) {
            persister = eksisterende.get();
            persister.setAkseptertFaktagrunnlag(varselrespons.getAkseptertFaktagrunnlag());
        }
        try {
            entityManager.persist(persister);
            entityManager.flush();
        } catch (PersistenceException e) {
            if (e.getCause().getCause().getMessage().contains("ORA-02291")) {
                throw VarselresponsRepositoryFeil.FACTORY.fantIngenBehandlingMedId(varselrespons.getBehandlingId()).toException();
            }
            throw VarselresponsRepositoryFeil.FACTORY.constraintFeilVedLagring(e).toException();
        }
    }

    @Override
    public Optional<Varselrespons> hentRespons(Long behandlingId) {
        TypedQuery<Varselrespons> query = entityManager.createQuery("from Varselrespons where behandling_id = :behandlingId", Varselrespons.class);
        query.setParameter("behandlingId", behandlingId);
        List<Varselrespons> resultat = query.getResultList();

        if (resultat.size() > 1) {
            throw FACTORY.flereResponserEnnForventet(resultat.size(), behandlingId).toException();
        }
        return !resultat.isEmpty() ? Optional.of(resultat.get(0)) : Optional.empty();
    }

}
