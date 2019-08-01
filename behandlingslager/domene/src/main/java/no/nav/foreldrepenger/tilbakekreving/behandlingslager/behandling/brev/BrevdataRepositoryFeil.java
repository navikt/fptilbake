package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BrevdataRepositoryFeil extends DeklarerteFeil {

    BrevdataRepositoryFeil FACTORY = FeilFactory.create(BrevdataRepositoryFeil.class);

    @TekniskFeil(feilkode = "FPT-475717", feilmelding = "Det finnes ingen behandlinger med id [ %s ]", logLevel = LogLevel.WARN)
    Feil fantIngenBehandlingMedId(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-367434", feilmelding = "Det oppstod en databasefeil ved lagring av brevdataen", logLevel = LogLevel.WARN)
    Feil constraintFeilVedLagring(Exception e);

}
