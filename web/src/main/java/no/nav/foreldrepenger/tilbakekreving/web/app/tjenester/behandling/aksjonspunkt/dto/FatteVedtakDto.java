package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;

@JsonTypeName(AksjonspunktKodeDefinisjon.FATTE_VEDTAK)
public class FatteVedtakDto extends BekreftetAksjonspunktDto {

    @Valid
    @Size(max = 10)
    private Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos;

    FatteVedtakDto() {
        // For Jackson
    }

    public FatteVedtakDto(Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtos) {
        this.aksjonspunktGodkjenningDtos = aksjonspunktGodkjenningDtos;
    }

    public Collection<AksjonspunktGodkjenningDto> getAksjonspunktGodkjenningDtos() {
        return aksjonspunktGodkjenningDtos;
    }
}
