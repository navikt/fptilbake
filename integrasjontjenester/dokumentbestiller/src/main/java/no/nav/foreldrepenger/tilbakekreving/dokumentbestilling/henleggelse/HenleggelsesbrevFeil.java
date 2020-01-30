package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;

public interface HenleggelsesbrevFeil extends DeklarerteFeil {
    HenleggelsesbrevFeil FACTORY = FeilFactory.create(HenleggelsesbrevFeil.class);

    @FunksjonellFeil(feilkode = "FPT-110801", feilmelding = "Varselbrev er ikke sendt. Kan ikke forhåndsvise/sende henleggelsesbrev for behandlingId=%s.", løsningsforslag = "", logLevel = LogLevel.WARN)
    Feil kanIkkeSendeEllerForhåndsviseHenleggelsesBrev(Long behandlingId);
}
