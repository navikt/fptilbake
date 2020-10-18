package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;

public class TestBehandlingStegType extends BehandlingStegType{
    public static final BehandlingStegType STEG_1 = new TestBehandlingStegType("STEG-1");
    public static final BehandlingStegType STEG_2 = new TestBehandlingStegType("STEG-2");
    public static final BehandlingStegType STEG_3 = new TestBehandlingStegType("STEG-3");
    public static final BehandlingStegType STEG_4 = new TestBehandlingStegType("STEG-4");
    public static final BehandlingStegType STEG_5 = new TestBehandlingStegType("STEG-5");

    public TestBehandlingStegType(String kode) {
        super(kode);
    }
}
