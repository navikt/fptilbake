package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.foreldrepenger.tilbakekreving.organisasjon.OrganisasjonIkkeFunnetException;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.OrganisasjonUgyldigInputException;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface EksternDataForBrevFeil extends DeklarerteFeil {

    EksternDataForBrevFeil FACTORY = FeilFactory.create(EksternDataForBrevFeil.class);

    @TekniskFeil(feilkode = "FPT-089912", feilmelding = "Fant ikke person med aktørId %s i tps", logLevel = LogLevel.WARN)
    Feil fantIkkePersoniTPS(String aktørId);

    @IntegrasjonFeil(feilkode = "FPT-254132", feilmelding = "Fant ikke organisasjon", exceptionClass = OrganisasjonIkkeFunnetException.class, logLevel = LogLevel.WARN)
    Feil organisasjonIkkeFunnet(HentOrganisasjonOrganisasjonIkkeFunnet årsak);

    @IntegrasjonFeil(feilkode= "FPT-934726", feilmelding = "Input organisasjon nummer er ugyldig", exceptionClass = OrganisasjonUgyldigInputException.class, logLevel = LogLevel.WARN)
    Feil ugyldigInput(HentOrganisasjonUgyldigInput årsak);
}
