package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

class TestAksjonspunktDefinisjon extends AksjonspunktDefinisjon {
    static final AksjonspunktDefinisjon AP_1 = new TestAksjonspunktDefinisjon("AP_1");
    static final AksjonspunktDefinisjon AP_2 = new TestAksjonspunktDefinisjon("AP_2");
    static final AksjonspunktDefinisjon AP_3 = new TestAksjonspunktDefinisjon("AP_3");
    static final AksjonspunktDefinisjon AP_4 = new TestAksjonspunktDefinisjon("AP_4");
    static final AksjonspunktDefinisjon AP_5 = new TestAksjonspunktDefinisjon("AP_5");
    static final AksjonspunktDefinisjon AP_6 = new TestAksjonspunktDefinisjon("AP_6");
    static final AksjonspunktDefinisjon AP_7 = new TestAksjonspunktDefinisjon("AP_7");
    static final AksjonspunktDefinisjon AP_8 = new TestAksjonspunktDefinisjon("AP_8");

    public TestAksjonspunktDefinisjon(String kode) {
        super(kode);
    }
}
