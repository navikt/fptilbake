package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;

class RevurderingFremoverhoppTransisjon extends FremoverhoppTransisjon {


    RevurderingFremoverhoppTransisjon(BehandlingStegType målsteg) {
        super("revurdering-fremoverhopp-til-" + målsteg.getKode(), målsteg);
    }

}
