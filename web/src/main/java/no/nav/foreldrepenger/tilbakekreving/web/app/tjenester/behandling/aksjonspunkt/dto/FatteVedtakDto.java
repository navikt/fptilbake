package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(FatteVedtakDto.AKSJONSPUNKT_KODE)
public class FatteVedtakDto extends BekreftetAksjonspunktDto {
    static final String AKSJONSPUNKT_KODE = "5005";

    @Valid
    @Size(max = 10)
    private Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos;

    FatteVedtakDto() {
        // For Jackson
    }

    public FatteVedtakDto(Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos) {
        this.aksjonspunktGodkjenningDtos = aksjonspunktGodkjenningDtos;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Collection<AksjonspunktGodkjenningDto> getAksjonspunktGodkjenningDtos() {
        return aksjonspunktGodkjenningDtos;
    }
}
