package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn;

public class TotrinnskontrollVurderÅrsak {
    private String kode;
    private String navn;

    public TotrinnskontrollVurderÅrsak(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }
}
