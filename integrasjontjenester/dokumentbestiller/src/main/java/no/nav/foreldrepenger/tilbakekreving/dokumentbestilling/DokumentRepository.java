package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class DokumentRepository  {

    private EntityManager entityManager;

    DokumentRepository() {
        // for CDI proxy
    }

    @Inject
    public DokumentRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

   
    public List<DokumentMalType> hentAlleDokumentMalTyper() {
        return entityManager.createQuery("SELECT d FROM DokumentMalType d", DokumentMalType.class) //$NON-NLS-1$
            .setHint(QueryHints.HINT_READONLY, "true") //$NON-NLS-1$
            .getResultList();
    }

   
    public DokumentMalType hentDokumentMalType(String kode) {
        TypedQuery<DokumentMalType> query = entityManager
            .createQuery("from DokumentMalType d where d.kode = :kode", DokumentMalType.class)
            .setParameter("kode", kode);
        return HibernateVerktøy.hentEksaktResultat(query);
    }

}
