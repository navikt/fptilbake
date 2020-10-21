package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;

class Historikkendring<T> {

    private HistorikkEndretFeltType felt;
    private T forrigeVerdi;
    private T nyVerdi;

    public Historikkendring(HistorikkEndretFeltType felt, T forrigeVerdi, T nyVerdi) {
        this.felt = felt;
        this.forrigeVerdi = forrigeVerdi;
        this.nyVerdi = nyVerdi;
    }

    public HistorikkEndretFeltType getFelt() {
        return felt;
    }

    public T getForrigeVerdi() {
        return forrigeVerdi;
    }

    public T getNyVerdi() {
        return nyVerdi;
    }



}
