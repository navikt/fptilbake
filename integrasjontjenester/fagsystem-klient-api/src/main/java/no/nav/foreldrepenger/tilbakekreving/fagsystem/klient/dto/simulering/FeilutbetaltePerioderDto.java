package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering;

import java.util.List;

public class FeilutbetaltePerioderDto {
    private Long sumFeilutbetaling;
    private List<PeriodeDto> perioder;

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public void setSumFeilutbetaling(Long sumFeilutbetaling) {
        this.sumFeilutbetaling = sumFeilutbetaling;
    }

    public List<PeriodeDto> getPerioder() {
        return perioder;
    }

    public void setPerioder(List<PeriodeDto> perioder) {
        this.perioder = perioder;
    }
}
