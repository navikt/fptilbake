package no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurderforeldelse;

import static java.util.Collections.singletonList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.VurderForeldelseAksjonspunktUtleder;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling.AutomatiskSaksbehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;

@BehandlingStegRef(kode = "VFORELDETSTEG")
@BehandlingTypeRef
@ApplicationScoped
@Transactional
public class VurderForeldelseStegImpl implements VurderForeldelseSteg {

    private static final Logger logger = LoggerFactory.getLogger(VurderForeldelseStegImpl.class);

    private BehandlingRepository behandlingRepository;
    private VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder;
    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;

    VurderForeldelseStegImpl() {
        // For CDI
    }

    @Inject
    public VurderForeldelseStegImpl(BehandlingRepositoryProvider repositoryProvider,
                                    VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder,
                                    VurdertForeldelseTjeneste vurdertForeldelseTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vurderForeldelseAksjonspunktUtleder = vurderForeldelseAksjonspunktUtleder;
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.isAutomatiskSaksbehandlet()) {
            utførStegAutomatisk(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }else {
            Optional<AksjonspunktDefinisjon> aksjonspunktDefinisjon = vurderForeldelseAksjonspunktUtleder.utledAksjonspunkt(kontekst.getBehandlingId());
            return aksjonspunktDefinisjon.map(ap -> BehandleStegResultat.utførtMedAksjonspunkter(singletonList(ap)))
                .orElseGet(BehandleStegResultat::utførtUtenAksjonspunkter);
        }
    }


    protected void utførStegAutomatisk(Behandling behandling) {
        long behandlingId = behandling.getId();
        logger.info("utfører foreldelse steg automatisk for behandling={}", behandlingId);
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = vurdertForeldelseTjeneste.hentFaktaPerioder(behandlingId);
        List<ForeldelsePeriodeDto> foreldelsePerioder = feilutbetalingPerioderDto.getPerioder().stream()
            .filter(periode -> vurderForeldelseAksjonspunktUtleder.erForeldet(LocalDate.now(), periode.getFom()))
            .map(periode -> new ForeldelsePeriodeDto(periode.tilPeriode(), ForeldelseVurderingType.IKKE_FORELDET, AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE))
            .collect(Collectors.toList());
        if (!foreldelsePerioder.isEmpty()) {
            vurdertForeldelseTjeneste.lagreVurdertForeldelseGrunnlag(behandlingId, foreldelsePerioder);
        }
    }

}

