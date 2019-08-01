package no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimuleringResultatDto {

    private Long sumFeilutbetaling;
    private Long sumInntrekk;

    public SimuleringResultatDto() {
    }

    public SimuleringResultatDto(Long sumFeilutbetaling, Long sumInntrekk) {
        this.sumFeilutbetaling = sumFeilutbetaling;
        this.sumInntrekk = sumInntrekk;
    }

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public Long getSumInntrekk() {
        return sumInntrekk;
    }

}
