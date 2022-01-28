package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


class HenleggelseTransisjon implements StegTransisjon {

    @Override
    public String getId() {
        return FellesTransisjoner.HENLAGT.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        var funnetMålsteg = nåværendeSteg.getBehandlingModell().hvertStegEtter(nåværendeSteg.getBehandlingStegType())
                .filter(s -> s.getBehandlingStegType().equals(BehandlingStegType.IVERKSETT_VEDTAK))
                .findFirst();
        if (funnetMålsteg.isPresent()) {
            return funnetMålsteg.get();
        }
        throw new IllegalArgumentException("Finnes ikke noe steg av type " + BehandlingStegType.IVERKSETT_VEDTAK + " etter " + nåværendeSteg);
    }

    @Override
    public Optional<BehandlingStegType> getMålstegHvisHopp() {
        return Optional.of(BehandlingStegType.IVERKSETT_VEDTAK);
    }

    @Override
    public BehandlingStegResultat getRetningForHopp() {
        return BehandlingStegResultat.FREMOVERFØRT;
    }

    @Override
    public String toString() {
        return "Henleggelse{}";
    }
}
