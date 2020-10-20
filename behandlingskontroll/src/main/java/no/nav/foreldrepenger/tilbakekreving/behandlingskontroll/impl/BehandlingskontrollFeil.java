package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BehandlingskontrollFeil extends DeklarerteFeil {

    BehandlingskontrollFeil FACTORY = FeilFactory.create(BehandlingskontrollFeil.class);

    @TekniskFeil(feilkode = "FPT-143308", feilmelding = "BehandlingId %s er allerede avsluttet, kan ikke henlegges", logLevel = ERROR)
    Feil kanIkkeHenleggeAvsluttetBehandling(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-154409", feilmelding = "BehandlingId %s er satt på vent, må aktiveres før den kan henlegges", logLevel = ERROR)
    Feil kanIkkeHenleggeBehandlingPåVent(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-105126", feilmelding = "BehandlingId %s har flere enn et aksjonspunkt, hvor aksjonspunktet fører til tilbakehopp ved gjenopptakelse. Kan ikke gjenopptas.", logLevel = ERROR)
    Feil kanIkkeGjenopptaBehandlingFantFlereAksjonspunkterSomMedførerTilbakehopp(Long behandlingId);

    @TekniskFeil(feilkode = "FPT-105127", feilmelding = "Utilfredsstilt avhengighet ved oppslag av behandlingssteg: %s, behandlingType %s.", logLevel = WARN)
    Feil utilfredsstiltAvhengighetVedOppslag(BehandlingStegType behandlingStegType, BehandlingType behandlingType);

    @TekniskFeil(feilkode = "FPT-105128", feilmelding = "Ambivalent avhengighet ved oppslag av behandlingssteg: %s, behandlingType %s.", logLevel = WARN)
    Feil ambivalentAvhengighetVedOppslag(BehandlingStegType behandlingStegType, BehandlingType behandlingType);

}
