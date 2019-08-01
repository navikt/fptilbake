package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class VarselresponsDto implements AbacDto {

    @Valid
    private BehandlingIdDto behandlingId;

    @NotNull
    private Boolean akseptertFaktagrunnlag;

    @NotNull
    @Valid
    private ResponsKanal kildeKanal;

    VarselresponsDto() {}

    public VarselresponsDto(Long behandlingId, ResponsKanal kildeKanal, Boolean akseptertFaktagrunnlag) {
        this.behandlingId = new BehandlingIdDto(behandlingId);
        this.kildeKanal = kildeKanal;
        this.akseptertFaktagrunnlag = akseptertFaktagrunnlag;
    }

    public Long getBehandlingId() {
        return behandlingId.getBehandlingId();
    }

    public Boolean getAkseptertFaktagrunnlag() {
        return akseptertFaktagrunnlag;
    }

    public ResponsKanal getKildeKanal() {
        return kildeKanal;
    }

    @JsonProperty(value = "akseptertFaktagrunnlag")
    public boolean harAkseptertFaktagrunnlag() {
        return akseptertFaktagrunnlag;
    }

    public static VarselresponsDto fraDomene(Varselrespons varselrespons) {
        VarselresponsDto dto = new VarselresponsDto();
        dto.behandlingId = new BehandlingIdDto(varselrespons.getBehandlingId());
        dto.akseptertFaktagrunnlag = varselrespons.getAkseptertFaktagrunnlag();
        return dto;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter attributter = AbacDataAttributter.opprett();
        attributter.leggTilBehandlingsId(behandlingId.getBehandlingId());
        return attributter;
    }
}
