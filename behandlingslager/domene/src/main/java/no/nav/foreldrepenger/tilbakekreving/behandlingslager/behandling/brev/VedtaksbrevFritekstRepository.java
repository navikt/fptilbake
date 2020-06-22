package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class VedtaksbrevFritekstRepository {

    private EntityManager entityManager;

    VedtaksbrevFritekstRepository() {
        //for CDI proxy
    }

    @Inject
    public VedtaksbrevFritekstRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    public void lagreVedtakPerioderOgTekster(List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder) {
        for (VedtaksbrevFritekstPeriode vedtaksbrevFritekstPeriode : vedtaksbrevFritekstPerioder) {
            entityManager.persist(vedtaksbrevFritekstPeriode);
        }
    }

    public void lagreVedtaksbrevOppsummering(VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering) {
        entityManager.persist(vedtaksbrevFritekstOppsummering);
    }

    public Optional<VedtaksbrevFritekstOppsummering> hentVedtaksbrevOppsummering(Long behandlingId) {
        TypedQuery<VedtaksbrevFritekstOppsummering> query = entityManager.createQuery("from VedtaksbrevFritekstOppsummering where behandling_id = :behandlingId", VedtaksbrevFritekstOppsummering.class);
        query.setParameter("behandlingId", behandlingId);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<VedtaksbrevFritekstPeriode> hentVedtaksbrevPerioderMedTekst(Long behandlingId) {
        TypedQuery<VedtaksbrevFritekstPeriode> query = entityManager.createQuery("from VedtaksbrevFritekstPeriode where behandling_id = :behandlingId", VedtaksbrevFritekstPeriode.class);
        query.setParameter("behandlingId", behandlingId);
        return query.getResultList();
    }

    public void slettOppsummering(Long behandlingId) {
        //FIXME unngå fysisk slett av data.. bør kun gjøre logisk  sletting
        Optional<VedtaksbrevFritekstOppsummering> vedtaksbrevOppsummeringOpt = hentVedtaksbrevOppsummering(behandlingId);
        vedtaksbrevOppsummeringOpt.ifPresent(oppsummering -> entityManager.remove(oppsummering));
    }

    public void slettPerioderMedFritekster(Long behandlingId) {
        //FIXME unngå fysisk slett av data.. bør kun gjøre logisk  sletting
        List<VedtaksbrevFritekstPeriode> vedtaksbrevFritekstPerioder = hentVedtaksbrevPerioderMedTekst(behandlingId);
        for (VedtaksbrevFritekstPeriode periode : vedtaksbrevFritekstPerioder) {
            entityManager.remove(periode);
        }
    }

}
