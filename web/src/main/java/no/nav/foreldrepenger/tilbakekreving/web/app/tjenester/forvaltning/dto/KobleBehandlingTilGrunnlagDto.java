package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class KobleBehandlingTilGrunnlagDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long mottattXmlId;

    public KobleBehandlingTilGrunnlagDto() {
        // for REST
    }

    public KobleBehandlingTilGrunnlagDto(Long behandlingId, Long mottattXmlId) {
        this.behandlingId = behandlingId;
        this.mottattXmlId = mottattXmlId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getMottattXmlId() {
        return mottattXmlId;
    }

    public void setMottattXmlId(Long mottattXmlId) {
        this.mottattXmlId = mottattXmlId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, getBehandlingId());
    }
}
