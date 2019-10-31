package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(AvklartFaktaFeilutbetalingDto.AKSJONSPUNKT_KODE)
public class AvklartFaktaFeilutbetalingDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "7003";

    @JsonProperty("begrunnelse")
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @Valid
    @Size(min = 1)
    private List<FaktaFeilutbetalingDto> feilutbetalingFakta;

    public AvklartFaktaFeilutbetalingDto() {
        super();
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public List<FaktaFeilutbetalingDto> getFeilutbetalingFakta() {
        return feilutbetalingFakta;
    }

    public void setFeilutbetalingFakta(List<FaktaFeilutbetalingDto> feilutbetalingFakta) {
        this.feilutbetalingFakta = feilutbetalingFakta;
    }
}
