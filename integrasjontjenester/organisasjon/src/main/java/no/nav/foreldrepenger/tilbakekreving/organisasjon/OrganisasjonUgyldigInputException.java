package no.nav.foreldrepenger.tilbakekreving.organisasjon;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class OrganisasjonUgyldigInputException extends IntegrasjonException {
    public OrganisasjonUgyldigInputException(Feil feil) {
        super(feil);
    }
}

