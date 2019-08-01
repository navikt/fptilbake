package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface AksjonspunktApplikasjonFeil extends DeklarerteFeil {
    AksjonspunktApplikasjonFeil FACTORY = FeilFactory.create(AksjonspunktApplikasjonFeil.class);

    @TekniskFeil(feilkode = "FPT-770743",
            feilmelding = "Finner ikke håndtering for aksjonspunkt med kode: %s", logLevel = WARN)
    Feil kanIkkeFinneAksjonspunktUtleder(String aksjonspunktKode);
}
