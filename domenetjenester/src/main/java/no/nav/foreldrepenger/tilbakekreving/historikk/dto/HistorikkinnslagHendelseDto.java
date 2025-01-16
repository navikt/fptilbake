package no.nav.foreldrepenger.tilbakekreving.historikk.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldFelt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

public class HistorikkinnslagHendelseDto {
    private HistorikkinnslagType navn;
    private String verdi;

    public HistorikkinnslagType getNavn() {
        return navn;
    }

    public void setNavn(HistorikkinnslagType navn) {
        this.navn = navn;
    }

    public String getVerdi() {
        return verdi;
    }

    public void setVerdi(String verdi) {
        this.verdi = verdi;
    }

    public static HistorikkinnslagHendelseDto mapFra(HistorikkinnslagOldFelt hendelse) {
        HistorikkinnslagHendelseDto dto = new HistorikkinnslagHendelseDto();
        dto.navn = HistorikkinnslagType.fraKode(hendelse.getNavn());
        dto.verdi = hendelse.getTilVerdi();
        return dto;
    }
}
