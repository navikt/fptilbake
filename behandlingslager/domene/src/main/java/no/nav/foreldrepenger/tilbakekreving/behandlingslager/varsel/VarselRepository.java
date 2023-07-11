package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class VarselRepository {

    private EntityManager entityManager;

    VarselRepository() {
        // for CDI
    }

    @Inject
    public VarselRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, String varselTekst, Long varselBeløp) {
        Optional<VarselInfo> forrigeVarsel = finnVarsel(behandlingId);
        if (forrigeVarsel.isPresent()) {
            VarselInfo forrigeVarselInfo = forrigeVarsel.get();
            forrigeVarselInfo.disable();
            entityManager.persist(forrigeVarselInfo);
        }
        VarselInfo varselInfo = VarselInfo.builder().medBehandlingId(behandlingId)
                .medVarselTekst(varselTekst)
                .medVarselBeløp(varselBeløp).build();
        entityManager.persist(varselInfo);
        entityManager.flush();
    }

    public Optional<VarselInfo> finnVarsel(Long behandlingId) {
        TypedQuery<VarselInfo> query = lagFinnVarselQuery(behandlingId);
        return hentUniktResultat(query);
    }

    public VarselInfo finnEksaktVarsel(Long behandlingId) {
        TypedQuery<VarselInfo> query = lagFinnVarselQuery(behandlingId);
        return hentEksaktResultat(query);
    }

    private TypedQuery<VarselInfo> lagFinnVarselQuery(Long behandlingId) {
        TypedQuery<VarselInfo> query = entityManager.createQuery("from VarselInfo vars where vars.behandlingId=:behandlingId " +
                "and vars.aktiv=:aktiv", VarselInfo.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return query;
    }

    public void lagreVarseltBeløp(Long behandlingId, Long varseltBeløp) {
        VarselInfo varselInfo = finnEksaktVarsel(behandlingId);
        varselInfo.setVarselBeløp(varseltBeløp);

        entityManager.persist(varselInfo);
        entityManager.flush();
    }
}
