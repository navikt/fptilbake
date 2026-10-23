package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;


import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

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

    public Collection<Totrinnsvurdering> hentTotrinnsvurderinger(Behandling behandling) {
        return getAktiveTotrinnaksjonspunktvurderinger(behandling);
    }

    public void slettGammelTotrinnData(Long behandlingId) {
        List<Totrinnsvurdering> totrinnsvurderinger = (List<Totrinnsvurdering>) getAktiveTotrinnaksjonspunktvurderinger(behandlingId);
        if (!totrinnsvurderinger.isEmpty()) {
            totrinnsvurderinger.forEach(totrinnsvurdering -> {
                totrinnsvurdering.disable();
                entityManager.persist(totrinnsvurdering);
            });
        }
        entityManager.flush();
    }

    protected Collection<Totrinnsvurdering> getAktiveTotrinnaksjonspunktvurderinger(Behandling behandling) {
        return getAktiveTotrinnaksjonspunktvurderinger(behandling.getId());
    }

    protected Collection<Totrinnsvurdering> getAktiveTotrinnaksjonspunktvurderinger(Long behandlingId) {
        TypedQuery<Totrinnsvurdering> query = entityManager.createQuery(
                "FROM Totrinnsvurdering tav WHERE tav.behandling.id = :behandling_id AND tav.aktiv = true",
                Totrinnsvurdering.class);

        query.setParameter("behandling_id", behandlingId);
        return query.getResultList();
    }

}
