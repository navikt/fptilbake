package no.nav.foreldrepenger.tilbakekreving.behandling.steg.foreslåvedtak;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ForeslåVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;


@BehandlingStegRef(kode = "FORVEDSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class ForeslåVedtakStegImpl implements ForeslåVedtakSteg {

    private static final Logger logger = LoggerFactory.getLogger(ForeslåVedtakStegImpl.class);

    private BehandlingRepository behandlingRepository;
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;

    ForeslåVedtakStegImpl(){
        // for CDI
    }

    @Inject
    public ForeslåVedtakStegImpl(BehandlingRepository behandlingRepository,
                                 ForeslåVedtakTjeneste foreslåVedtakTjeneste){
        this.behandlingRepository = behandlingRepository;
        this.foreslåVedtakTjeneste = foreslåVedtakTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if(behandling.isAutomatiskSaksbehandlet()){
            utførStegAutomatisk(behandling);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK));
    }

    protected void utførStegAutomatisk(Behandling behandling) {
        long behandlingId = behandling.getId();
        logger.info("utfører foreslå vedtak steg automatisk for behandling={}", behandlingId);
        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(behandlingId, HistorikkAktør.VEDTAKSLØSNINGEN);
    }

}
