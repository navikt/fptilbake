package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/** Referanse til en behandling. */
@JsonInclude(Include.NON_NULL)
public class BehandlingIdDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    public BehandlingIdDto() {
        behandlingId = null; // NOSONAR
    }

    public BehandlingIdDto(String behandlingId) {
        this.behandlingId = Long.valueOf(behandlingId);
    }

    public BehandlingIdDto(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' +
            "behandlingId=" + behandlingId +
            '>';
    }
}
