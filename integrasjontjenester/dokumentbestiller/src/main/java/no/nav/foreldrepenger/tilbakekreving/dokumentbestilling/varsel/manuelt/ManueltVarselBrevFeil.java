package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ManueltVarselBrevFeil extends DeklarerteFeil {

    ManueltVarselBrevFeil FACTORY = FeilFactory.create(ManueltVarselBrevFeil.class);

    @TekniskFeil(feilkode = "FPT-612900", feilmelding = "Kravgrunnlag finnes ikke for behandling=%s, kan ikke sende varsel", logLevel = LogLevel.WARN)
    Feil kanIkkeSendeVarselForGrunnlagFinnesIkke(Long behandlingId);
}
