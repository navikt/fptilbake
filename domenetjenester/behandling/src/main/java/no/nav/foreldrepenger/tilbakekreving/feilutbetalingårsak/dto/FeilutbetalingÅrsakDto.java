package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class FeilutbetalingÅrsakDto {

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String årsakKode;

    @Size(max = 256)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String årsak;

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String kodeverk;

    @Size(min=0)
    @Valid
    private List<UnderÅrsakDto> underÅrsaker = new ArrayList<>();

    public String getÅrsakKode() {
        return årsakKode;
    }

    public void setÅrsakKode(String årsakKode) {
        this.årsakKode = årsakKode;
    }

    public String getÅrsak() {
        return årsak;
    }

    public void setÅrsak(String årsak) {
        this.årsak = årsak;
    }

    public String getKodeverk() {
        return kodeverk;
    }

    public void setKodeverk(String kodeverk) {
        this.kodeverk = kodeverk;
    }

    public List<UnderÅrsakDto> getUnderÅrsaker() {
        return underÅrsaker;
    }

    public void leggTilUnderÅrsaker(UnderÅrsakDto underÅrsak) {
        underÅrsaker.add(underÅrsak);
    }
}
