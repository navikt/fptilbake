package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatInfoDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@ApplicationScoped
public class AutomatiskVurdertVilkårTjeneste {

    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;

    AutomatiskVurdertVilkårTjeneste() {
        // for CDI
    }

    @Inject
    public AutomatiskVurdertVilkårTjeneste(VilkårsvurderingTjeneste vilkårsvurderingTjeneste,
                                           AksjonspunktRepository aksjonspunktRepository) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    public void automatiskVurdertVilkår(Behandling behandling, String begrunnelse) {
        long behandlingId = behandling.getId();
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetaltePerioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(behandlingId);
        List<VilkårsvurderingPerioderDto> vilkårsvurdertePerioder = feilutbetaltePerioder.stream().filter(periode -> !periode.isForeldet())
            .map(periode -> lagVilkårsvurderingPeriode(periode.tilPeriode(), begrunnelse))
            .collect(Collectors.toList());
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(behandlingId, vilkårsvurdertePerioder);
        //Aksjonpunkt oppretter ikke automatisk for automatisk saksbehandling. Det opprettes manuelt for å vise vilkår data i frontend.
        lagUtførtAksjonspunkt(behandling);
    }

    private VilkårsvurderingPerioderDto lagVilkårsvurderingPeriode(Periode periode, String begrunnelse) {
        VilkårsvurderingPerioderDto vilkårsvurderingPeriode = new VilkårsvurderingPerioderDto();
        vilkårsvurderingPeriode.setPeriode(periode);
        vilkårsvurderingPeriode.setBegrunnelse(begrunnelse);
        vilkårsvurderingPeriode.setVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT);
        VilkårResultatAktsomhetDto vilkårResultatAktsomhetDto = new VilkårResultatAktsomhetDto();
        vilkårResultatAktsomhetDto.setTilbakekrevSelvOmBeloepErUnder4Rettsgebyr(false);
        VilkårResultatInfoDto vilkårResultatInfoDto = new VilkårResultatAnnetDto(begrunnelse, Aktsomhet.SIMPEL_UAKTSOM, vilkårResultatAktsomhetDto);
        vilkårsvurderingPeriode.setVilkarResultatInfo(vilkårResultatInfoDto);

        return vilkårsvurderingPeriode;
    }

    private void lagUtførtAksjonspunkt(Behandling behandling){
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_TILBAKEKREVING);
        aksjonspunktRepository.setTilUtført(aksjonspunkt);
    }
}
