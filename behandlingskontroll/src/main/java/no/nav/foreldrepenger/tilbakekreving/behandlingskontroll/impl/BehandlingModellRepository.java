package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

@ApplicationScoped
public class BehandlingModellRepository implements AutoCloseable {

    private final ConcurrentMap<Object, BehandlingModell> cachedModell = new ConcurrentHashMap<>();

    @Inject
    public BehandlingModellRepository() {
    }

    /**
     * Finn modell for angitt behandling type.
     * <p>
     * Når modellen ikke lenger er i bruk må {@link BehandlingModellImpl#close()}
     * kalles slik at den ikke fortsetter å holde på referanser til objekter. (DETTE
     * KAN DROPPES OM VI FÅR CACHET MODELLENE!)
     */
    public BehandlingModell getModell(BehandlingType behandlingType) {
        var key = cacheKey(behandlingType);
        cachedModell.computeIfAbsent(key, (kode) -> byggModell(behandlingType));
        return cachedModell.get(key);
    }

    protected Object cacheKey(BehandlingType behandlingType) {
        // lager en key av flere sammensatte elementer.
        return List.of(behandlingType);
    }

    protected BehandlingModell byggModell(BehandlingType behandlingType) {
        return BehandlingTypeRef.Lookup.find(BehandlingModell.class, behandlingType)
                .orElseThrow(() -> new IllegalStateException("Har ikke BehandlingModell for BehandlingType:" + behandlingType));
    }

    @Override
    public void close() throws Exception {
        cachedModell.clear();
    }

}
