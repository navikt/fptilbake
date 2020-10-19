package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

public enum VedtaksbrevType {
    ORDINÆR,
    FRITEKST_VEDTAK_OG_UTEN_PERIODE, // start på vedtak er berre fritekst, og ingen periodeavsnitt
    FRITEKST // mulig fremtidig fullstendig fritekstbrev
}
