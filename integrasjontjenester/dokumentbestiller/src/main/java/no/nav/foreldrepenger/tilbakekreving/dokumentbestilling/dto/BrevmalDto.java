package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

public class BrevmalDto {
    private String kode;
    private String navn;
    private boolean tilgjengelig;

    public BrevmalDto(String kode, String navn, boolean tilgjengelig) {
        this.kode = kode;
        this.navn = navn;
        this.tilgjengelig = tilgjengelig;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public boolean getTilgjengelig() {
        return tilgjengelig;
    }

    public void setTilgjengelig(boolean tilgjengelig) {
        this.tilgjengelig = tilgjengelig;
    }

}
