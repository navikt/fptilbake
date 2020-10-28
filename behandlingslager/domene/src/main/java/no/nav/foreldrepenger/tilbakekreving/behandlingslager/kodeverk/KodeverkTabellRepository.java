package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

@ApplicationScoped
public class KodeverkTabellRepository {

    private EntityManager entityManager;

    KodeverkTabellRepository() {
        // CDI
    }

    @Inject
    public KodeverkTabellRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public BehandlingStegType finnBehandlingStegType(String kode) {
        TypedQuery<BehandlingStegType> query = entityManager.createQuery("from BehandlingStegType where kode=:kode", BehandlingStegType.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    public AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode) {
        TypedQuery<AksjonspunktDefinisjon> query = entityManager.createQuery("from AksjonspunktDef where kode=:kode", AksjonspunktDefinisjon.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }
}
