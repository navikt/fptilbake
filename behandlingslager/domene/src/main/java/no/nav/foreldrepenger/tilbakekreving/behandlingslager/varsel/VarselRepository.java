package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VarselRepository {

    private EntityManager entityManager;

    VarselRepository() {
        // for CDI
    }

    @Inject
    public VarselRepository(@VLPersistenceUnit  EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, String varselTekst, Long varselBeløp) {
        Optional<VarselEntitet> forrigeVarsel = finnVarsel(behandlingId);
        if (forrigeVarsel.isPresent()) {
            forrigeVarsel.get().disable();
            entityManager.persist(forrigeVarsel);
        }
        VarselEntitet varselEntitet = VarselEntitet.builder().medBehandlingId(behandlingId)
            .medVarselTekst(varselTekst)
            .medVarselBeløp(varselBeløp)
            .medAktiv(true).build();
        entityManager.persist(varselEntitet);
        entityManager.flush();
    }

    public Optional<VarselEntitet> finnVarsel(Long behandlingId) {
        TypedQuery<VarselEntitet> query = entityManager.createQuery("from VarselEntitet vars where vars.behandlingId=:behandlingId " +
            "and vars.aktiv=:aktiv", VarselEntitet.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    public VarselEntitet finnEksaktVarsel(Long behandlingId) {
        TypedQuery<VarselEntitet> query = entityManager.createQuery("from VarselEntitet vars where vars.behandlingId=:behandlingId " +
            "and vars.aktiv=:aktiv", VarselEntitet.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentEksaktResultat(query);
    }

    public void lagreVarseltBeløp(Long behandlingId, Long varseltBeløp) {
        VarselEntitet varselEntitet = finnEksaktVarsel(behandlingId);
        varselEntitet.setVarselBeløp(varseltBeløp);

        entityManager.persist(varselEntitet);
        entityManager.flush();
    }
}
