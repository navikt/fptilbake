package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingTypeStegSekvens;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;

@ApplicationScoped
public class BehandlingModellRepository {

    private EntityManager entityManager; // NOSONAR

    private final ConcurrentMap<Object, BehandlingModell> cachedModell = new ConcurrentHashMap<>();

    BehandlingModellRepository() {
        // for CDI proxy
    }

    @Inject
    public BehandlingModellRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public KodeverkRepository getKodeverkRepository() {
        return new KodeverkRepositoryImpl(entityManager);
    }

    public BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon() {
        List<BehandlingStegStatus> list = Lists.newArrayList(BehandlingStegStatus.values());
        return new BehandlingStegKonfigurasjon(list);
    }

    /**
     * Finn modell for angitt behandling type.
     * <p>
     * Når modellen ikke lenger er i bruk må {@link BehandlingModellImpl#close()}
     * kalles slik at den ikke fortsetter å holde på referanser til objekter. (DETTE KAN DROPPES OM VI FÅR CACHET
     * MODELLENE!)
     */
    public BehandlingModell getModell(BehandlingType behandlingType) {
        Object key = cacheKey(behandlingType);
        cachedModell.computeIfAbsent(key, (kode) -> byggModell(behandlingType));
        return cachedModell.get(key);
    }

    private Object cacheKey(BehandlingType behandlingType) {
        // lager en key av flere sammensatte elementer.
        return Arrays.asList(behandlingType);
    }

    protected BehandlingModellImpl byggModell(BehandlingType type) {
        BehandlingModellImpl modell = nyModell(type);

        List<BehandlingTypeStegSekvens> stegSekvens = finnBehandlingStegSekvens(type);
        modell.leggTil(stegSekvens);

        return modell;
    }

    protected BehandlingModellImpl nyModell(BehandlingType type) {
        return new BehandlingModellImpl(type, false);
    }

    private List<BehandlingTypeStegSekvens> finnBehandlingStegSekvens(BehandlingType type) {
        String jpql = "from BehandlingTypeStegSekvens btss where btss.behandlingType=:behandlingType ORDER BY btss.sekvensNr ASC"; //$NON-NLS-1$
        TypedQuery<BehandlingTypeStegSekvens> query = entityManager.createQuery(jpql, BehandlingTypeStegSekvens.class);
        query.setParameter("behandlingType", type); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        return query.getResultList();
    }

}
