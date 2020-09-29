package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

public enum DetaljertBrevType {

    VARSEL(BrevType.VARSEL_BREV),
    KORRIGERT_VARSEL(BrevType.VARSEL_BREV),
    VEDTAK(BrevType.VEDTAK_BREV),
    HENLEGGELSE(BrevType.HENLEGGELSE_BREV),
    INNHENT_DOKUMETASJON(BrevType.INNHENT_DOKUMENTASJONBREV);

    private BrevType brevType;

    DetaljertBrevType(BrevType brevType) {
        this.brevType = brevType;
    }

    public BrevType getBrevType() {
        return brevType;
    }

    public boolean gjelderVarsel() {
        return this == VARSEL || this == KORRIGERT_VARSEL;
    }
}
