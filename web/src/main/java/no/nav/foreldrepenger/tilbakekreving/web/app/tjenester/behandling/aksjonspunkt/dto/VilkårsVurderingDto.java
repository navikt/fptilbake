package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;

@JsonTypeName(AksjonspunktKodeDefinisjon.VURDER_TILBAKEKREVING)
public class VilkårsVurderingDto extends BekreftetAksjonspunktDto {

    @Size(min = 1)
    @Valid
    private List<VilkårsvurderingPerioderDto> vilkarsVurdertePerioder;

    public List<VilkårsvurderingPerioderDto> getVilkarsVurdertePerioder() {
        return vilkarsVurdertePerioder;
    }

    public void setVilkarsVurdertePerioder(List<VilkårsvurderingPerioderDto> vilkarsVurdertePerioder) {
        this.vilkarsVurdertePerioder = vilkarsVurdertePerioder;
    }
}
