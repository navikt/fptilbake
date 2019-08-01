package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.VilkårsVurderingDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VilkårsVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
public class VilkårsvurderingOppdaterer implements AksjonspunktOppdaterer<VilkårsVurderingDto> {

    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;

    @Inject
    public VilkårsvurderingOppdaterer(VilkårsvurderingTjeneste vilkårsvurderingTjeneste) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
    }

    @Override
    public void oppdater(VilkårsVurderingDto dto, Behandling behandling) {
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(behandling.getId(), dto.getVilkarsVurdertePerioder());
    }
}
