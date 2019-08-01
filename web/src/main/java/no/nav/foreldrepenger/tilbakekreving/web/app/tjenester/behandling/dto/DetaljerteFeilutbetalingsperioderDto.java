package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.math.BigDecimal;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;

public class DetaljerteFeilutbetalingsperioderDto {

    private List<DetaljertFeilutbetalingPeriodeDto> perioder;
    private BigDecimal rettsgebyr;

    public List<DetaljertFeilutbetalingPeriodeDto> getPerioder() {
        return perioder;
    }

    public void setPerioder(List<DetaljertFeilutbetalingPeriodeDto> perioder) {
        this.perioder = perioder;
    }

    public BigDecimal getRettsgebyr() {
        return rettsgebyr;
    }

    public void setRettsgebyr(BigDecimal rettsgebyr) {
        this.rettsgebyr = rettsgebyr;
    }
}
