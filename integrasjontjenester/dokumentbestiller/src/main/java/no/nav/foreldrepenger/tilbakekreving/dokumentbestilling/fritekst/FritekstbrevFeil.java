package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;

public interface FritekstbrevFeil extends DeklarerteFeil {
    FritekstbrevFeil FACTORY = FeilFactory.create(FritekstbrevFeil.class);

    @FunksjonellFeil(feilkode = "FPT-110801", feilmelding = "Varselbrev er ikke sendt. Kan ikke forhåndsvise/sende henleggelsesbrev for behandlingId=%s.", løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil kanIkkeSendeEllerForhåndsviseHenleggelsesBrev(Long behandlingId);

    @FunksjonellFeil(feilkode = "FPT-110802",
        feilmelding = "Kan ikke forhåndsvise/sende henleggelsesbrev uten fritekst for Tilbakekreving Revurdering med behandlingId=%s.",
        løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil kanIkkeSendeEllerForhåndsviseRevurderingHenleggelsesBrev(Long behandlingId);
}
