package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

class DummySteg implements BehandlingSteg {

    private List<AksjonspunktResultat> aksjonspunkter;
    private boolean tilbakefør;
    protected AtomicReference<BehandleStegResultat> sisteUtførStegResultat = new AtomicReference<>();

    public DummySteg() {
        aksjonspunkter = Collections.emptyList();
    }

    public DummySteg(AksjonspunktResultat... aksjonspunkt) {
        aksjonspunkter = Arrays.asList(aksjonspunkt);
    }

    public DummySteg(boolean tilbakefør, AksjonspunktResultat... aksjonspunkt) {
        this.aksjonspunkter = Arrays.asList(aksjonspunkt);
        this.tilbakefør = tilbakefør;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        if (tilbakefør) {
            BehandleStegResultat tilbakeført = BehandleStegResultat
                    .tilbakeførtMedAksjonspunkter(aksjonspunkter.stream()
                            .map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList()));
            sisteUtførStegResultat.set(tilbakeført);
            return tilbakeført;
        }
        BehandleStegResultat utførtMedAksjonspunkter = BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunkter);
        sisteUtførStegResultat.set(utførtMedAksjonspunkter);
        return utførtMedAksjonspunkter;
    }

    public boolean isUtførStegKalt() {
        return sisteUtførStegResultat.get() != null;
    }

    public BehandleStegResultat sisteStegResultat() {
        return sisteUtførStegResultat.get();
    }

    public static BehandlingModellImpl.BiFunction<BehandlingStegType, BehandlingType, BehandlingSteg> map(List<TestStegKonfig> input) {

        Map<List<?>, BehandlingSteg> resolver = new HashMap<>();

        for (TestStegKonfig konfig : input) {
            List<?> key = Arrays.asList(konfig.getBehandlingStegType(), konfig.getBehandlingType());
            resolver.put(key, konfig.getSteg());
        }

        return (t, u) -> resolver.get(Arrays.asList(t, u));
    }

}
