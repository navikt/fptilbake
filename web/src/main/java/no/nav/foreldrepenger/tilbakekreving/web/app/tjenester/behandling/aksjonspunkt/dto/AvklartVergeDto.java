package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VergeDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(AvklartVergeDto.AKSJONSPUNKT_KODE)
public class AvklartVergeDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5030";

    @JsonProperty("begrunnelse")
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @NotNull
    @Valid
    @JsonProperty("vergeFakta")
    private VergeDto vergeFakta;

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public VergeDto getVergeFakta() {
        return vergeFakta;
    }

    public void setVergeFakta(VergeDto vergeFakta) {
        this.vergeFakta = vergeFakta;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = AbacDataAttributter.opprett();
        String fnr = this.getVergeFakta().getFnr();
        if (fnr != null) {
            abacDataAttributter.leggTil(AppAbacAttributtType.FNR, fnr);
        }
        return abacDataAttributter;
    }

}
