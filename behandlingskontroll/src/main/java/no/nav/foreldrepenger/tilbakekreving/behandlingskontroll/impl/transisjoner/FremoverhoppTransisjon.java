package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


class FremoverhoppTransisjon implements StegTransisjon {

    private final String id;
    private final BehandlingStegType målsteg;

    public FremoverhoppTransisjon(String id, BehandlingStegType målsteg) {
        this.id = id;
        this.målsteg = målsteg;
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        var funnetMålsteg = nåværendeSteg.getBehandlingModell().hvertStegEtter(nåværendeSteg.getBehandlingStegType())
                .filter(s -> s.getBehandlingStegType().equals(målsteg))
                .findFirst();
        if (funnetMålsteg.isPresent()) {
            return funnetMålsteg.get();
        }
        throw new IllegalArgumentException("Finnes ikke noe steg av type " + målsteg + " etter " + nåværendeSteg);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<BehandlingStegType> getMålstegHvisHopp() {
        return Optional.of(målsteg);
    }

    @Override
    public BehandlingStegResultat getRetningForHopp() {
        return BehandlingStegResultat.FREMOVERFØRT;
    }

    @Override
    public String toString() {
        return "FremoverhoppTransisjon{" +
                "id='" + id + '\'' +
                '}';
    }
}
