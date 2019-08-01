package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

public class TestBehandlingType extends BehandlingType {
    public static final BehandlingType TEST = new TestBehandlingType("BT-TEST");
    public static final BehandlingType TEST2 = new TestBehandlingType("BT-TEST2");

    protected TestBehandlingType(String kode) {
        super(kode);
    }
}
