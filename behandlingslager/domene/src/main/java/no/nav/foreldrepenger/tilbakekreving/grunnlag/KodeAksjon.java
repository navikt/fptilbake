package no.nav.foreldrepenger.tilbakekreving.grunnlag;

public enum KodeAksjon {

    FINN_GRUNNLAG_OMGJØRING("3"),
    HENT_GRUNNLAG_OMGJØRING("5"),
    FATTE_VEDTAK("8");

    private String kode;

    KodeAksjon(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
