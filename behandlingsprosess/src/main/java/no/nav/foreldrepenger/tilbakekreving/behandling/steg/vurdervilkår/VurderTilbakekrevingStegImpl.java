package no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurdervilkår;

import java.util.Collections;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;


@BehandlingStegRef(kode = "VTILBSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class VurderTilbakekrevingStegImpl implements VurderTilbakekrevingSteg {

    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Inject
    public VurderTilbakekrevingStegImpl(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.behandlingRepositoryProvider = behandlingRepositoryProvider;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Long behandlingId = kontekst.getBehandlingId();

        if (allePerioderErForeldet(behandlingId)){
            deaktiverForrigeVilkårsvurdering(behandlingId);
            return hoppFremoverTilVedtak();
        }

        return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING));
    }

    private BehandleStegResultat hoppFremoverTilVedtak() {
        return BehandleStegResultat.fremoverførtMedAksjonspunkter(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK,
            Collections.singletonList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK));
    }

    private boolean allePerioderErForeldet(Long behandlingId) {
        Optional<VurdertForeldelse> vurdertForeldelseOpt = behandlingRepositoryProvider.getVurdertForeldelseRepository().finnVurdertForeldelse(behandlingId);
        if (vurdertForeldelseOpt.isPresent()) {
            return vurdertForeldelseOpt.get().getVurdertForeldelsePerioder().stream()
                .allMatch(foreldelsePeriodeDto -> ForeldelseVurderingType.FORELDET.equals(foreldelsePeriodeDto.getForeldelseVurderingType()));
        }
        return false;
    }

    private void deaktiverForrigeVilkårsvurdering(Long behandlingId) {
        VilkårsvurderingRepository vilkårsvurderingRepository = behandlingRepositoryProvider.getVilkårsvurderingRepository();
        vilkårsvurderingRepository.slettVilkårsvurdering(behandlingId);
    }
}
