package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class BestillBrevDto implements AbacDto {

    @NotNull
    @Valid
    private BehandlingReferanse behandlingId;

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String brevmalkode;

    @NotNull
    @Size(min = 1, max = 3000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    public BehandlingReferanse getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(BehandlingReferanse behandlingId) {
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
        return behandlingId.abacAttributter();
    }
}
