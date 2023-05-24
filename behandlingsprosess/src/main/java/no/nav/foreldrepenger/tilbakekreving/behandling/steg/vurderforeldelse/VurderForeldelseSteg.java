package no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurderforeldelse;

import static java.util.Collections.singletonList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.AutomatiskVurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurderForeldelseAksjonspunktUtleder;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling.AutomatiskSaksbehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

@BehandlingStegRef(BehandlingStegType.FORELDELSEVURDERINGSTEG)
@BehandlingTypeRef
@ApplicationScoped
public class VurderForeldelseSteg implements BehandlingSteg {

    private static final Logger logger = LoggerFactory.getLogger(VurderForeldelseSteg.class);

    private BehandlingRepository behandlingRepository;
    private VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder;
    private AutomatiskVurdertForeldelseTjeneste automatiskVurdertForeldelseTjeneste;

    VurderForeldelseSteg() {
        // For CDI
    }

    @Inject
    public VurderForeldelseSteg(BehandlingRepositoryProvider repositoryProvider,
                                VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder,
                                AutomatiskVurdertForeldelseTjeneste automatiskVurdertForeldelseTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vurderForeldelseAksjonspunktUtleder = vurderForeldelseAksjonspunktUtleder;
        this.automatiskVurdertForeldelseTjeneste = automatiskVurdertForeldelseTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.isAutomatiskSaksbehandlet()) {
            utførStegAutomatisk(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        } else {
            return vurderForeldelseAksjonspunktUtleder.utledAksjonspunkt(kontekst.getBehandlingId())
                .map(ap -> BehandleStegResultat.utførtMedAksjonspunkter(singletonList(ap)))
                .orElseGet(BehandleStegResultat::utførtUtenAksjonspunkter);
        }
    }

    protected void utførStegAutomatisk(Behandling behandling) {
        logger.info("utfører foreldelse steg automatisk for behandling={}", behandling.getId());
        automatiskVurdertForeldelseTjeneste.automatiskVurdetForeldelse(behandling,
                AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE);
    }

}

