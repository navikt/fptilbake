package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;

@JsonTypeName(AvklartFaktaFeilutbetalingDto.AKSJONSPUNKT_KODE)
public class AvklartFaktaFeilutbetalingDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "7003";

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

    public List<FaktaFeilutbetalingDto> getFeilutbetalingFakta() {
        return feilutbetalingFakta;
    }

    public void setFeilutbetalingFakta(List<FaktaFeilutbetalingDto> feilutbetalingFakta) {
        this.feilutbetalingFakta = feilutbetalingFakta;
    }
}
