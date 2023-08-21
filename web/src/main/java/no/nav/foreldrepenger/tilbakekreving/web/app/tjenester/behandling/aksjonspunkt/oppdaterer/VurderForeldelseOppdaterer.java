package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.VurderForeldelseDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderForeldelseDto.class, adapter = AksjonspunktOppdaterer.class)
public class VurderForeldelseOppdaterer implements AksjonspunktOppdaterer<VurderForeldelseDto> {

    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;

    @Inject
    public VurderForeldelseOppdaterer(VurdertForeldelseTjeneste vurdertForeldelseTjeneste) {
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
    }

    @Override
    public void oppdater(VurderForeldelseDto dto, Behandling behandling) {
        vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(behandling.getId(), dto.getForeldelsePerioder());
    }
}
