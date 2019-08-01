package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class UnderÅrsakDto {

    @Size(max = 256)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String underÅrsak;

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String underÅrsakKode;

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String kodeverk;

    UnderÅrsakDto(){
        // For CDI
    }

    public UnderÅrsakDto(String underÅrsak, String underÅrsakKode, String kodeverk) {
        this.underÅrsak = underÅrsak;
        this.underÅrsakKode = underÅrsakKode;
        this.kodeverk = kodeverk;
    }

    public String getUnderÅrsak() {
        return underÅrsak;
    }

    public void setUnderÅrsak(String underÅrsak) {
        this.underÅrsak = underÅrsak;
    }

    public String getUnderÅrsakKode() {
        return underÅrsakKode;
    }

    public void setUnderÅrsakKode(String underÅrsakKode) {
        this.underÅrsakKode = underÅrsakKode;
    }

    public String getKodeverk() {
        return kodeverk;
    }

    public void setKodeverk(String kodeverk) {
        this.kodeverk = kodeverk;
    }
}
