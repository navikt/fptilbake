package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class HenleggBehandlingDto implements AbacDto {

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Valid
    private UUID behandlingUuid;

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String årsakKode;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @Size(max = 2000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingVersjon;

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

    public String getÅrsakKode() {
        return årsakKode;
    }

    public void setÅrsakKode(String årsakKode) {
        this.årsakKode = årsakKode;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
    }

    public void setBehandlingVersjon(Long behandlingVersjon) {
        this.behandlingVersjon = behandlingVersjon;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        var attributter = AbacDataAttributter.opprett();
        if (behandlingId != null) {
            attributter.leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, behandlingId);
        }
        if (behandlingUuid != null) {
            attributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingUuid);
        }
        return attributter;
    }

}
