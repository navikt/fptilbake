package no.nav.foreldrepenger.tilbakekreving.organisasjon;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonRestKlient;

@ApplicationScoped
public class VirksomhetTjeneste {

    private static final Logger log = LoggerFactory.getLogger(VirksomhetTjeneste.class);
    private OrganisasjonRestKlient organisasjonConsumer;

    VirksomhetTjeneste() {
        // for CDI
    }

    @Inject
    public VirksomhetTjeneste(OrganisasjonRestKlient organisasjonConsumer) {
        this.organisasjonConsumer = organisasjonConsumer;
    }

    public Virksomhet hentOrganisasjon(String orgNummer) {
        Objects.requireNonNull(orgNummer, "orgNummer");
        var response = organisasjonConsumer.hentOrganisasjon(orgNummer);
        return new Virksomhet.Builder()
            .medOrgnr(orgNummer)
            .medNavn(response.getNavn())
            .build();
    }

    public boolean validerOrganisasjon(String orgNummer) {
        try {
            return hentOrganisasjon(orgNummer) != null;
        } catch (Exception e) {
            log.warn("Kan ikke hente organisasjon for orgNummer {}", orgNummer, e);
            return false;
        }
    }

}
