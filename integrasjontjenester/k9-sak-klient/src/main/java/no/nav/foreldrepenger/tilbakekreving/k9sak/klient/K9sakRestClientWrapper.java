package no.nav.foreldrepenger.tilbakekreving.k9sak.klient;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

import java.util.Optional;

/**
 * Enkapsulerer RestClient brukt i K9sakKlient for å sikre at alle requests til k9-sak har X-Json-Serializer-Option header satt, slik at deserialisering
 * av Kodeverdi i respons frå k9-sak er kode strings, og kan deserialiseres med default fptilbake Kodeverdi definisjoner (bruker kun @JsonValue på kode).
 * <p>
 * Kan fjernast igjen viss standard serialisering frå k9-sak blir endra til å serialisere Kodeverdi som kode strings.
 */
public class K9sakRestClientWrapper {
    private RestClient wrapped;

    public K9sakRestClientWrapper(final RestClient wrapped) {
        this.wrapped = wrapped;
    }

    private RestRequest addJsonSerializerOptionHeader(final RestRequest req) {
        return req.header("X-Json-Serializer-Option", "kodeverdi-string");
    }

    public <T> T send(RestRequest request, Class<T> clazz) {
        return this.wrapped.send(addJsonSerializerOptionHeader(request), clazz);
    }

    public <T> Optional<T> sendReturnOptional(RestRequest request, Class<T> clazz) {
        return this.wrapped.sendReturnOptional(addJsonSerializerOptionHeader(request), clazz);
    }
}
