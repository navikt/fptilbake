package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FeilutbetalingPerioderDto {

    @Min(value = 0)
    @Max(value = Long.MAX_VALUE)
    private Long behandlingId;

    @Valid
    private UUID behandlingUuid;

    @NotNull
    @Size(min = 1, max = 100)
    @Valid
    private List<ForeldelsePeriodeMedBeløpDto> perioder;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    public List<ForeldelsePeriodeMedBeløpDto> getPerioder() {
        return perioder;
    }

    public void setPerioder(List<ForeldelsePeriodeMedBeløpDto> perioder) {
        this.perioder = perioder;
    }

}
