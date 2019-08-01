package no.nav.foreldrepenger.tilbakekreving.pip.fpinfo.intern;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class FpsakPipKlient {

    private SystemUserOidcRestClient restClient;
    private URI endpoint;

    public FpsakPipKlient() {
        // CDI
    }

    @Inject
    public FpsakPipKlient(SystemUserOidcRestClient restClient, @KonfigVerdi(value = "fpsak_pip_aktoer_for_sak.url") String urlPip) {
        this.restClient = restClient;
        this.endpoint = URI.create(urlPip);
    }

    public Set<AktørId> hentAktørIdForSak(String saksnummer) {
        Objects.requireNonNull(saksnummer);
        if (saksnummer.isEmpty()) {
            // dersom saksnummer er null, tom eller ikke finnes, skal det feile her
            throw new IllegalArgumentException("tomt saksnummer");
        }
        URI uri = request(saksnummer);

        Set<String> aktørIder = restClient.get(uri, HashSet.class);

        return aktørIder.stream()
            .map(AktørId::new)
            .collect(Collectors.toSet());
    }

    private URI request(String saksnummer) {
        try {
            return new URIBuilder(endpoint)
                .addParameter("saksnummer", saksnummer)
                .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
