package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class Vilkårsendring {

    private Periode periode;
    private String begrunnelseVilkår;
    private String begrunnelseAktsomhet;
    private String begrunnelseSærligGrunner;
    private List<Historikkendring> endringer;

    Vilkårsendring(Periode periode, String begrunnelseVilkår, String begrunnelseAktsomhet, String begrunnelseSærligGrunner, List<Historikkendring> endringer) {
        this.periode = periode;
        this.begrunnelseVilkår = begrunnelseVilkår;
        this.begrunnelseAktsomhet = begrunnelseAktsomhet;
        this.begrunnelseSærligGrunner = begrunnelseSærligGrunner;
        this.endringer = endringer;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public LocalDate getTom() {
        return periode.getTom();
    }

    public List<Historikkendring> getEndringer() {
        return endringer;
    }

    public String getBegrunnelseVilkår() {
        return begrunnelseVilkår;
    }

    public String getBegrunnelseAktsomhet() {
        return begrunnelseAktsomhet;
    }

    public String getBegrunnelseSærligGrunner() {
        return begrunnelseSærligGrunner;
    }
}
