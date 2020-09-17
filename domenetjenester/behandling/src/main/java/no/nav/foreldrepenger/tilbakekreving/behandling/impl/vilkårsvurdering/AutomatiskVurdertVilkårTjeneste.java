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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@ApplicationScoped
public class AutomatiskVurdertVilkårTjeneste {

    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;

    AutomatiskVurdertVilkårTjeneste() {
        // for CDI
    }

    @Inject
    public AutomatiskVurdertVilkårTjeneste(VilkårsvurderingTjeneste vilkårsvurderingTjeneste) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
    }

    public void automatiskVurdertVilkår(Behandling behandling, String begrunnelse) {
        long behandlingId = behandling.getId();
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetaltePerioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(behandlingId);
        List<VilkårsvurderingPerioderDto> vilkårsvurdertePerioder = feilutbetaltePerioder.stream().filter(periode -> !periode.isForeldet())
            .map(periode -> lagVilkårsvurderingPeriode(periode.tilPeriode(), begrunnelse))
            .collect(Collectors.toList());
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(behandlingId, vilkårsvurdertePerioder);
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
}
