package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class BestillBrevDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String brevmalkode;

    @NotNull
    @Size(min = 1, max = 3000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getBrevmalkode() {
        return brevmalkode;
    }

    public void setBrevmalkode(String brevmalkode) {
        this.brevmalkode = brevmalkode;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, behandlingId);
    }
}
