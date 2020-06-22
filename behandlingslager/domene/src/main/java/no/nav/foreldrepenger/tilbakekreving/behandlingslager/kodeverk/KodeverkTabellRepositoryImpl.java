package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;

@ApplicationScoped
public class KodeverkTabellRepositoryImpl implements KodeverkTabellRepository {

    private EntityManager entityManager;

    KodeverkTabellRepositoryImpl() {
        // CDI
    }

    @Inject
    public KodeverkTabellRepositoryImpl( EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public BehandlingStegType finnBehandlingStegType(String kode) {
        TypedQuery<BehandlingStegType> query = entityManager.createQuery("from BehandlingStegType where kode=:kode", BehandlingStegType.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    @Override
    public Venteårsak finnVenteårsak(String kode) {
        TypedQuery<Venteårsak> query = entityManager.createQuery("from Venteårsak where kode=:kode", Venteårsak.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }

    @Override
    public Set<VurderÅrsak> finnVurderÅrsaker(Collection<String> koder) {
        if (koder.isEmpty()){
            return Collections.emptySet();
        }
        TypedQuery<VurderÅrsak> query = entityManager.createQuery("from VurderÅrsak where kode in (:kode)", VurderÅrsak.class);
        query.setParameter("kode", koder);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return new HashSet<>(query.getResultList());
    }

    @Override
    public AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode) {
        TypedQuery<AksjonspunktDefinisjon> query = entityManager.createQuery("from AksjonspunktDef where kode=:kode", AksjonspunktDefinisjon.class);
        query.setParameter("kode", kode);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getSingleResult();
    }
}
