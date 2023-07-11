package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;


import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class TotrinnRepository {

    private EntityManager entityManager;

    TotrinnRepository() {
        // CDI
    }

    @Inject
    public TotrinnRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public void lagreTotrinnsvurdering(Totrinnsvurdering totrinnsvurdering) {
        entityManager.persist(totrinnsvurdering);
    }


    public void lagreOgFlush(Behandling behandling, Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger) {
        Objects.requireNonNull(behandling, "behandling");

        Collection<Totrinnsvurdering> aktiveVurderinger = getAktiveTotrinnaksjonspunktvurderinger(behandling);
        if (!aktiveVurderinger.isEmpty()) {
            aktiveVurderinger.forEach(vurdering -> {
                vurdering.setAktiv(false);
                entityManager.persist(vurdering);
            });
        }
        totrinnaksjonspunktvurderinger.forEach(this::lagreTotrinnsvurdering);
        entityManager.flush();
    }

    public void lagreOgFlush(Behandling behandling, Totrinnresultatgrunnlag totrinnresultatgrunnlag) {
        Objects.requireNonNull(behandling, "behandling");

        Optional<Totrinnresultatgrunnlag> aktivtTotrinnresultatgrunnlag = getAktivtTotrinnresultatgrunnlag(behandling);
        if (aktivtTotrinnresultatgrunnlag.isPresent()) {
            Totrinnresultatgrunnlag grunnlag = aktivtTotrinnresultatgrunnlag.get();
            grunnlag.disable();
            entityManager.persist(grunnlag);
        }
        lagreTotrinnsresultatgrunnlag(totrinnresultatgrunnlag);
        entityManager.flush();
    }

    public Collection<Totrinnsvurdering> hentTotrinnsvurderinger(Behandling behandling) {
        return getAktiveTotrinnaksjonspunktvurderinger(behandling);
    }

    public Optional<Totrinnresultatgrunnlag> hentTotrinngrunnlag(Behandling behandling) {
        return getAktivtTotrinnresultatgrunnlag(behandling);
    }

    public void slettGammelTotrinnData(Long behandlingId) {
        List<Totrinnsvurdering> totrinnsvurderinger = (List<Totrinnsvurdering>) getAktiveTotrinnaksjonspunktvurderinger(behandlingId);
        if (!totrinnsvurderinger.isEmpty()) {
            totrinnsvurderinger.forEach(totrinnsvurdering -> {
                totrinnsvurdering.disable();
                entityManager.persist(totrinnsvurdering);
            });
        }
        Optional<Totrinnresultatgrunnlag> totrinnresultatgrunnlag = getAktivtTotrinnresultatgrunnlag(behandlingId);
        if (totrinnresultatgrunnlag.isPresent()) {
            Totrinnresultatgrunnlag aktivTotrinnresultatgrunnlag = totrinnresultatgrunnlag.get();
            aktivTotrinnresultatgrunnlag.disable();
            entityManager.persist(aktivTotrinnresultatgrunnlag);
        }
        entityManager.flush();
    }

    protected Collection<Totrinnsvurdering> getAktiveTotrinnaksjonspunktvurderinger(Behandling behandling) {
        return getAktiveTotrinnaksjonspunktvurderinger(behandling.getId());
    }

    protected Optional<Totrinnresultatgrunnlag> getAktivtTotrinnresultatgrunnlag(Behandling behandling) {
        return getAktivtTotrinnresultatgrunnlag(behandling.getId());
    }

    protected Optional<Totrinnresultatgrunnlag> getAktivtTotrinnresultatgrunnlag(Long behandlingId) {
        TypedQuery<Totrinnresultatgrunnlag> query = entityManager.createQuery(
                "SELECT trg FROM Totrinnresultatgrunnlag trg WHERE trg.behandling.id = :behandling_id AND trg.aktiv = 'J'", //$NON-NLS-1$
                Totrinnresultatgrunnlag.class);

        query.setParameter("behandling_id", behandlingId); //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);
    }

    protected Collection<Totrinnsvurdering> getAktiveTotrinnaksjonspunktvurderinger(Long behandlingId) {
        TypedQuery<Totrinnsvurdering> query = entityManager.createQuery(
                "SELECT tav FROM Totrinnsvurdering tav WHERE tav.behandling.id = :behandling_id AND tav.aktiv = 'J'", //$NON-NLS-1$
                Totrinnsvurdering.class);

        query.setParameter("behandling_id", behandlingId); //$NON-NLS-1$
        return query.getResultList();
    }

    private void lagreTotrinnsresultatgrunnlag(Totrinnresultatgrunnlag totrinnresultatgrunnlag) {
        entityManager.persist(totrinnresultatgrunnlag);
    }
}
