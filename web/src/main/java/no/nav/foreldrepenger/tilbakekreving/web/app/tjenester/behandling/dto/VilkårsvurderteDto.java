package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;

public class VilkårsvurderteDto {

    private List<VilkårsvurderingPerioderDto> vilkarsVurdertePerioder;

    public List<VilkårsvurderingPerioderDto> getVilkarsVurdertePerioder() {
        return vilkarsVurdertePerioder;
    }

    public void setVilkarsVurdertePerioder(List<VilkårsvurderingPerioderDto> vilkarsVurdertePerioder) {
        this.vilkarsVurdertePerioder = vilkarsVurdertePerioder;
    }
}
