package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class VarselresponsRepository {

    private EntityManager entityManager;

    public VarselresponsRepository() {
    }

    @Inject
    public VarselresponsRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

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
                throw new TekniskException("FPT-754532", String.format("Det finnes ingen behandlinger med id [ %s ]", varselrespons.getBehandlingId()));
            }
            throw new TekniskException("FPT-523523", "Det oppstod en databasefeil ved lagring av responsen", e);
        }
    }

    public Optional<Varselrespons> hentRespons(Long behandlingId) {
        TypedQuery<Varselrespons> query = entityManager.createQuery("from Varselrespons where behandlingId = :behandlingId", Varselrespons.class);
        query.setParameter("behandlingId", behandlingId);
        List<Varselrespons> resultat = query.getResultList();

        if (resultat.size() > 1) {
            throw new TekniskException("FPT-352363", String.format("Fant flere ( %s ) responser enn forventet (1). behandlingId [ %s ]", resultat.size(), behandlingId));
        }
        return !resultat.isEmpty() ? Optional.of(resultat.get(0)) : Optional.empty();
    }

}
