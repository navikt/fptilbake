package no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurdervilkår;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertFeilutbetalingPeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatInfoDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling.AutomatiskSaksbehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;


@BehandlingStegRef(kode = "VTILBSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class VurderTilbakekrevingStegImpl implements VurderTilbakekrevingSteg {

    private static final Logger logger = LoggerFactory.getLogger(VurderTilbakekrevingStegImpl.class);

    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepository behandlingRepository;
    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;

    VurderTilbakekrevingStegImpl() {
        // for CDI
    }

    @Inject
    public VurderTilbakekrevingStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                        VilkårsvurderingTjeneste vilkårsvurderingTjeneste) {
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
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
        logger.info("utfører vilkår steg automatisk for behandling={}", behandlingId);
        List<DetaljertFeilutbetalingPeriodeDto> feilutbetaltePerioder = vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(behandlingId);
        List<VilkårsvurderingPerioderDto> vilkårsvurdertePerioder = feilutbetaltePerioder.stream().filter(periode -> !periode.isForeldet())
            .map(periode -> lagVilkårsvurderingPeriode(periode.tilPeriode(), AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE))
            .collect(Collectors.toList());
        vilkårsvurderingTjeneste.lagreVilkårsvurdering(behandlingId, vilkårsvurdertePerioder);
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
