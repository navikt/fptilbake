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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseAggregate;


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

        // hvis alle periode er foreldet, prosess skal hoppe til vedtak
        Optional<VurdertForeldelseAggregate> foreldelseAggregate = behandlingRepositoryProvider.getVurdertForeldelseRepository()
                .finnVurdertForeldelseForBehandling(behandlingId);
        if (foreldelseAggregate.isPresent()) {
            boolean erAllePeriodeForeldet = foreldelseAggregate.get().getVurdertForeldelse().getVurdertForeldelsePerioder().stream()
                    .allMatch(foreldelsePeriodeDto -> ForeldelseVurderingType.FORELDET.equals(foreldelsePeriodeDto.getForeldelseVurderingType()));
            if (erAllePeriodeForeldet) {
                // fjerner gamle vilkårsvurdering hvis det finnes
                håndteresGammelVilkårsvurdering(behandlingId);

                return BehandleStegResultat.fremoverførtMedAksjonspunkter(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK,
                        Collections.singletonList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK));
            }
        }
        // ellers forsatt med Vilkårsvudering
        return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING));
    }

    private void håndteresGammelVilkårsvurdering(Long behandlingId) {

        VilkårsvurderingRepository vilkårsvurderingRepository = behandlingRepositoryProvider.getVilkårsvurderingRepository();
        Optional<VilkårVurderingAggregateEntitet> aggregateEntitet = vilkårsvurderingRepository.finnVilkårsvurderingForBehandlingId(behandlingId);
        if (aggregateEntitet.isPresent()) {
            aggregateEntitet.get().disable();
            behandlingRepositoryProvider.getVilkårsvurderingRepository().lagre(aggregateEntitet.get());
        }
    }
}
