package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BehandlingVedtakRepository {

    private static final Logger logger = LoggerFactory.getLogger(BehandlingVedtakRepository.class);

    private EntityManager entityManager;

    BehandlingVedtakRepository() {
        // for hibernate
    }

    @Inject
    public BehandlingVedtakRepository( EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(BehandlingVedtak behandlingVedtak) {
        if (behandlingVedtak.getId() != null) {
            logger.warn("BehandlingVedtak ble oppdatert for behandling={}, dette skal normalt aldri skje. Eneste kjente situasjon hvor det kan skje er " +
                    "at tilbakeføring til tidligere steg var nødvendig fordi kravgrunnlag ble endret etter vedtaket ble fattet.",
                behandlingVedtak.getBehandlingsresultat().getId());
        }
        entityManager.persist(behandlingVedtak);
        entityManager.flush();
    }

    public Optional<BehandlingVedtak> hentBehandlingvedtakForBehandlingId(Long behandlingId) {
        TypedQuery<BehandlingVedtak> query = entityManager.createQuery(
                "FROM BehandlingVedtak vedtak WHERE vedtak.behandlingsresultat.behandling.id = :behandlingId", //$NON-NLS-1$
                BehandlingVedtak.class);

        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    public List<BehandlingVedtak> hentAlleBehandlingVedtak(){
        TypedQuery<BehandlingVedtak> query = entityManager.createQuery("select vedtak FROM BehandlingVedtak vedtak join fetch vedtak.behandlingsresultat", BehandlingVedtak.class); //$NON-NLS-1$
        return query.getResultList();
    }
}
