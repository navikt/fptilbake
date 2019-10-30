package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BrevdataRepositoryImpl implements BrevdataRepository {

    private static final String KEY_BEHANDLING_ID = "behandlingId";
    private EntityManager entityManager;

    private BrevdataRepositoryImpl() {
    }

    @Inject
    public BrevdataRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public void lagreVarselbrevData(VarselbrevSporing varselbrevSporing) {
        entityManager.persist(varselbrevSporing);
        //TODO unngå flush, bare nyttig for tester
        entityManager.flush();
    }

    @Override
    public void lagreVedtaksbrevData(VedtaksbrevSporing vedtaksbrevSporing) {
        entityManager.persist(vedtaksbrevSporing);
        //TODO unngå flush, bare nyttig for tester
        entityManager.flush();
    }

    @Override
    public void lagreVedtakPerioderOgTekster(List<VedtaksbrevPeriode> vedtaksbrevPerioder) {
        for (VedtaksbrevPeriode vedtaksbrevPeriode : vedtaksbrevPerioder) {
            entityManager.persist(vedtaksbrevPeriode);
        }
        //TODO unngå flush, bare nyttig for tester
        entityManager.flush();
    }

    @Override
    public void lagreVedtaksbrevOppsummering(VedtaksbrevOppsummering vedtaksbrevOppsummering) {
        entityManager.persist(vedtaksbrevOppsummering);
        //TODO unngå flush, bare nyttig for tester
        entityManager.flush();
    }

    @Override
    public List<VarselbrevSporing> hentVarselbrevData(Long behandlingId) {
        TypedQuery<VarselbrevSporing> query = entityManager.createQuery("from VarselbrevSporing where behandling_id = :behandlingId", VarselbrevSporing.class);
        query.setParameter(KEY_BEHANDLING_ID, behandlingId);
        return query.getResultList();
    }

    @Override
    public List<VedtaksbrevSporing> hentVedtaksbrevData(Long behandlingId) {
        TypedQuery<VedtaksbrevSporing> query = entityManager.createQuery("from VedtaksbrevSporing where behandling_id = :behandlingId", VedtaksbrevSporing.class);
        query.setParameter(KEY_BEHANDLING_ID, behandlingId);
        return query.getResultList();
    }

    @Override
    public Optional<VedtaksbrevOppsummering> hentVedtaksbrevOppsummering(Long behandlingId) {
        TypedQuery<VedtaksbrevOppsummering> query = entityManager.createQuery("from VedtaksbrevOppsummering where behandling_id = :behandlingId", VedtaksbrevOppsummering.class);
        query.setParameter(KEY_BEHANDLING_ID, behandlingId);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    @Override
    public List<VedtaksbrevPeriode> hentVedtaksbrevPerioderMedTekst(Long behandlingId) {
        TypedQuery<VedtaksbrevPeriode> query = entityManager.createQuery("from VedtaksbrevPeriode where behandling_id = :behandlingId", VedtaksbrevPeriode.class);
        query.setParameter(KEY_BEHANDLING_ID, behandlingId);
        return query.getResultList();
    }

    @Override
    public void slettOppsummering(Long behandlingId) {
        //FIXME unngå fysisk slett av data.. bør kun gjøre logisk  sletting
        Optional<VedtaksbrevOppsummering> vedtaksbrevOppsummeringOpt = hentVedtaksbrevOppsummering(behandlingId);
        vedtaksbrevOppsummeringOpt.ifPresent(oppsummering -> entityManager.remove(oppsummering));
    }

    @Override
    public void slettPerioderMedFritekster(Long behandlingId) {
        //FIXME unngå fysisk slett av data.. bør kun gjøre logisk  sletting
        List<VedtaksbrevPeriode> vedtaksbrevPerioder = hentVedtaksbrevPerioderMedTekst(behandlingId);
        for (VedtaksbrevPeriode periode : vedtaksbrevPerioder) {
            entityManager.remove(periode);
        }
    }

    @Override
    public boolean harVarselBrevSendtForBehandlingId(Long behandlingId) {
        return !hentVarselbrevData(behandlingId).isEmpty();
    }
}
