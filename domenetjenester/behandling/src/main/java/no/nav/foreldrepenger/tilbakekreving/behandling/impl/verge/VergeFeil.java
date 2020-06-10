package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface VergeFeil extends DeklarerteFeil {
    VergeFeil FACTORY = FeilFactory.create(VergeFeil.class);

    @TekniskFeil(feilkode = "FPT-763493", feilmelding = "Behandlingen er allerede avsluttet eller sett på vent, kan ikke opprette verge for behandling %s", logLevel = LogLevel.WARN)
    Feil kanIkkeOppretteVerge(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-763494", feilmelding = "Behandlingen er allerede avsluttet eller sett på vent, kan ikke fjerne verge for behandling %s", logLevel = LogLevel.WARN)
    Feil kanIkkeFjerneVerge(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-185321", feilmelding = "Behandling %s har allerede aksjonspunkt 5030 for verge/fullmektig", logLevel = LogLevel.WARN)
    Feil harAlleredeAksjonspunktForVerge(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-185322", feilmelding = "Verge funksjonalitet er ikke skrudd på i produksjon", logLevel = LogLevel.WARN)
    Feil harVergeSkruddAvIProd();
}
