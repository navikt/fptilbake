package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9DataKeys;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PipAktørId;


public class K9AppRessursData {

    private final Set<String> aktørIdSet = new LinkedHashSet<>();
    private final Set<String> fødselsnumre = new LinkedHashSet<>();
    private final Map<K9DataKeys, K9RessursData> resources = new HashMap<>();

    public Set<String> getAktørIdSet() {
        return aktørIdSet;
    }

    public Set<String> getFødselsnumre() {
        return fødselsnumre;
    }

    public Map<K9DataKeys, K9RessursData> getResources() {
        return resources;
    }

    public K9RessursData getResource(K9DataKeys key) {
        return resources.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AppRessursData{}";
    }

    public static class Builder {
        private final K9AppRessursData pdpRequest;

        public Builder() {
            pdpRequest = new K9AppRessursData();
        }

        public Builder leggTilAktørId(String aktørId) {
            pdpRequest.aktørIdSet.add(aktørId);
            return this;
        }

        public Builder leggTilAktørIdSet(Collection<String> aktørId) {
            pdpRequest.aktørIdSet.addAll(aktørId);
            return this;
        }

        public Builder leggTilAbacAktørIdSet(Collection<K9PipAktørId> aktørId) {
            pdpRequest.aktørIdSet.addAll(aktørId.stream().map(K9PipAktørId::getVerdi).collect(Collectors.toSet()));
            return this;
        }

        public Builder leggTilFødselsnummer(String fnr) {
            pdpRequest.fødselsnumre.add(fnr);
            return this;
        }

        public Builder leggTilFødselsnumre(Collection<String> fnr) {
            pdpRequest.fødselsnumre.addAll(fnr);
            return this;
        }

        public Builder leggTilRessurs(K9DataKeys key, String value) {
            if (value == null) {
                removeKeyIfPresent(key);
                return this;
            }
            pdpRequest.resources.put(key, new K9RessursData(key, value));
            return this;
        }

        public Builder leggTilRessurs(K9DataKeys key, K9RessursDataValue value) {
            if (value == null) {
                removeKeyIfPresent(key);
                return this;
            }
            pdpRequest.resources.put(key, new K9RessursData(key, value.getVerdi()));
            return this;
        }

        public K9AppRessursData build() {
            return pdpRequest;
        }

        private void removeKeyIfPresent(K9DataKeys key) {
            if (pdpRequest.resources.get(key) != null) {
                pdpRequest.resources.remove(key);
            }
        }
    }
}
