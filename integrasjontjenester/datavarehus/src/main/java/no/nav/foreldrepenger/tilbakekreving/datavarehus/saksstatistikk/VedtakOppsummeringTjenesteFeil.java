package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface VedtakOppsummeringTjenesteFeil extends DeklarerteFeil {
    VedtakOppsummeringTjenesteFeil FACTORY = FeilFactory.create(VedtakOppsummeringTjenesteFeil.class);

    @TekniskFeil(feilkode = "FPT-131275", feilmelding = "Fant ikke vedtak for behandling med behandlingId=%s.Kan ikke sende data til DVH", logLevel = LogLevel.WARN)
    Feil fantIkkeBehandlingVedtak(long behandlingId);
}
