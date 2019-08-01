package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface VarselresponsRepositoryFeil extends DeklarerteFeil {

    VarselresponsRepositoryFeil FACTORY = FeilFactory.create(VarselresponsRepositoryFeil.class);

    @TekniskFeil(feilkode = "FPT-352363", feilmelding = "Fant flere ( %s ) responser enn forventet (1). behandlingId [ %s ]", logLevel = LogLevel.WARN)
    Feil flereResponserEnnForventet(int antall, Long behandlingId);

    @TekniskFeil(feilkode = "FPT-754532", feilmelding = "Det finnes ingen behandlinger med id [ %s ]", logLevel = LogLevel.WARN)
    Feil fantIngenBehandlingMedId(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-523523", feilmelding = "Det oppstod en databasefeil ved lagring av responsen", logLevel = LogLevel.WARN)
    Feil constraintFeilVedLagring(Exception e);

}
