package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface BehandlingFeil extends DeklarerteFeil {

    BehandlingFeil FEILFACTORY = FeilFactory.create(BehandlingFeil.class);

    @TekniskFeil(feilkode = "FPT-473718", feilmelding = "Behandling har ikke aksjonspunkt for definisjon [%s].", logLevel = LogLevel.ERROR)
    Feil aksjonspunktIkkeFunnet(String kode);

    @TekniskFeil(feilkode = "FPT-473718", feilmelding = "Behandling %s kan ha bare en behandlingsårsak", logLevel = LogLevel.WARN)
    Feil merEnnEnBehandlingsÅrsakFinnes(Long behandlingId);

}
