package no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurdervilkår;

import java.util.Collections;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.AutomatiskVurdertVilkårTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling.AutomatiskSaksbehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;


@BehandlingStegRef(BehandlingStegType.VTILBSTEG)
@BehandlingTypeRef
@ApplicationScoped
public class VurderTilbakekrevingSteg implements BehandlingSteg {

    private static final Logger LOG = LoggerFactory.getLogger(VurderTilbakekrevingSteg.class);

    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepository behandlingRepository;
    private AutomatiskVurdertVilkårTjeneste automatiskVurdertVilkårTjeneste;

    VurderTilbakekrevingSteg() {
        // for CDI
    }

    @Inject
    public VurderTilbakekrevingSteg(BehandlingRepositoryProvider repositoryProvider,
                                    AutomatiskVurdertVilkårTjeneste automatiskVurdertVilkårTjeneste) {
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.automatiskVurdertVilkårTjeneste = automatiskVurdertVilkårTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.isAutomatiskSaksbehandlet()) {
            utførStegAutomatisk(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        } else {
            if (allePerioderErForeldet(behandlingId)) {
                deaktiverForrigeVilkårsvurdering(behandlingId);
                return hoppFremoverTilVedtak();
            }
            return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING));
        }
    }

    protected void utførStegAutomatisk(Behandling behandling) {
        long behandlingId = behandling.getId();
        LOG.info("utfører vilkår steg automatisk for behandling={}", behandlingId);
        automatiskVurdertVilkårTjeneste.automatiskVurdertVilkår(behandling, AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE);
    }

    private BehandleStegResultat hoppFremoverTilVedtak() {
        return BehandleStegResultat.fremoverførtMedAksjonspunkter(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK,
                Collections.singletonList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK));
    }

    private boolean allePerioderErForeldet(Long behandlingId) {
        Optional<VurdertForeldelse> vurdertForeldelseOpt = vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId);
        return vurdertForeldelseOpt.map(vurdertForeldelse -> vurdertForeldelse.getVurdertForeldelsePerioder().stream()
                .allMatch(foreldelsePeriodeDto -> ForeldelseVurderingType.FORELDET.equals(foreldelsePeriodeDto.getForeldelseVurderingType()))).orElse(false);
    }

    private void deaktiverForrigeVilkårsvurdering(Long behandlingId) {
        vilkårsvurderingRepository.slettVilkårsvurdering(behandlingId);
    }

}
