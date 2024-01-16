package no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatInfoDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@ApplicationScoped
public class AutomatiskVurdertVilkårTjeneste {

    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    AutomatiskVurdertVilkårTjeneste() {
        // for CDI
    }

    @Inject
    public AutomatiskVurdertVilkårTjeneste(VilkårsvurderingTjeneste vilkårsvurderingTjeneste,
                                           BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void automatiskVurdertVilkår(Behandling behandling, String begrunnelse) {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        long behandlingId = behandling.getId();
        var feilutbetaltePerioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(behandlingId);
        var vilkårsvurdertePerioder = feilutbetaltePerioder.stream().filter(periode -> !periode.isForeldet())
                .map(periode -> lagVilkårsvurderingPeriode(periode.tilPeriode(), begrunnelse))
                .toList();
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(behandlingId, vilkårsvurdertePerioder);
        //Aksjonpunkt oppretter ikke automatisk for automatisk saksbehandling. Det opprettes manuelt for å vise vilkår data i frontend.
        lagUtførtAksjonspunkt(kontekst, behandling);
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

    private void lagUtførtAksjonspunkt(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        behandlingskontrollTjeneste.lagreAksjonspunktOpprettetUtførtUtenEvent(kontekst, behandling.getAktivtBehandlingSteg(), AksjonspunktDefinisjon.VURDER_TILBAKEKREVING);
    }
}
