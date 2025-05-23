package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class ByttBehandlendeEnhetDto implements AbacDto {
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Valid
    private UUID behandlingUuid;

    @Size(min = 1, max = 256)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String enhetNavn;

    @Size(max = 10)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String enhetId;

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

    public String getEnhetId() {
        return enhetId;
    }

    public void setEnhetId(String enhetId) {
        this.enhetId = enhetId;
    }

    public String getEnhetNavn() {
        return enhetNavn;
    }

    public void setEnhetNavn(String enhetNavn) {
        this.enhetNavn = enhetNavn;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
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
