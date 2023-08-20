package no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktafeilutbetaling;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling.AutomatiskSaksbehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.AutomatiskFaktaFastsettelseTjeneste;

@BehandlingStegRef(BehandlingStegType.FAKTA_FEILUTBETALING)
@BehandlingTypeRef
@ApplicationScoped
public class FaktaFeilutbetalingSteg implements BehandlingSteg {

    private static final Logger LOG = LoggerFactory.getLogger(FaktaFeilutbetalingSteg.class);

    private BehandlingRepository behandlingRepository;
    private AutomatiskFaktaFastsettelseTjeneste faktaFastsettelseTjeneste;

    FaktaFeilutbetalingSteg() {
        // for CDI
    }

    @Inject
    public FaktaFeilutbetalingSteg(BehandlingRepository behandlingRepository,
                                   AutomatiskFaktaFastsettelseTjeneste faktaFastsettelseTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.faktaFastsettelseTjeneste = faktaFastsettelseTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.isAutomatiskSaksbehandlet()) {
            utførStegAutomatisk(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        return BehandleStegResultat.utførtMedAksjonspunkter(
                Collections.singletonList(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
    }

    protected void utførStegAutomatisk(Behandling behandling) {
        LOG.info("utfører fakta steg automatisk for behandling={}", behandling.getId());
        faktaFastsettelseTjeneste.fastsettFaktaAutomatisk(behandling, AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE);
    }

}
