package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface TpsOversetterFeilmeldinger extends DeklarerteFeil {

    TpsOversetterFeilmeldinger FACTORY = FeilFactory.create(TpsOversetterFeilmeldinger.class);

    @TekniskFeil(feilkode = "FPT-112305", feilmelding = "Bruker %s er ikke Person, kan ikke hente ut adresse", logLevel = LogLevel.WARN)
    Feil ukjentBrukerTypeFinnerIkkeAdresse(String aktørId);

    @TekniskFeil(feilkode = "FPT-200210", feilmelding = "Fant ikke informasjon om land i adresseinfo, bruker Norge videre", logLevel = LogLevel.WARN)
    Feil manglerLandBrukerNorge(VLException cause);

    @TekniskFeil(feilkode = "FPT-222317", feilmelding = "Bruker %s: Vedtaksløsningen gjenkjenner ikke adressetype %s fra TPS", logLevel = LogLevel.WARN)
    Feil ikkeGjenkjentAdresseType(String ident, String value);

}
