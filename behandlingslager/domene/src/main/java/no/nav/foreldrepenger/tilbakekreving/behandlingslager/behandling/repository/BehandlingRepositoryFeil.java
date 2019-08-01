package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BehandlingRepositoryFeil extends DeklarerteFeil {
    BehandlingRepositoryFeil FACTORY = FeilFactory.create(BehandlingRepositoryFeil.class);

    @TekniskFeil(feilkode = "FPT-131239", feilmelding = "Fant ikke entitet for låsing [%s], id=%s.", logLevel = LogLevel.ERROR)
    Feil fantIkkeEntitetForLåsing(String entityClassName, long id);

    @TekniskFeil(feilkode = "FPT-131240", feilmelding = "Fant ikke BehandlingVedtak, behandlingId=%s.", logLevel = LogLevel.ERROR)
    Feil fantIkkeBehandlingVedtak(long behandlingId);

}
