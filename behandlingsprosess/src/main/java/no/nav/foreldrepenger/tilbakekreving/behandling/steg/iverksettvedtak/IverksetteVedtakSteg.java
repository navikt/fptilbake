package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;

@BehandlingStegRef(kode = BehandlingStegKode.IVERKSETT_VEDTAK)
@BehandlingTypeRef
@ApplicationScoped
public class IverksetteVedtakSteg implements BehandlingSteg {

    private static final Logger log = LoggerFactory.getLogger(IverksetteVedtakSteg.class);

    private BehandlingVedtakRepository behandlingVedtakRepository;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskIverksett prosessTaskIverksett;


    IverksetteVedtakSteg() {
        // for CDI proxy
    }

    @Inject
    public IverksetteVedtakSteg(BehandlingRepositoryProvider repositoryProvider, ProsessTaskIverksett prosessTaskIverksett) {
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.prosessTaskIverksett = prosessTaskIverksett;
    }


    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();

        Optional<BehandlingVedtak> fantVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        if (fantVedtak.isEmpty()) {
            throw BehandlingRepositoryFeil.fantIkkeBehandlingVedtak(behandlingId);
        }
        BehandlingVedtak vedtak = fantVedtak.get();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (IverksettingStatus.IKKE_IVERKSATT.equals(vedtak.getIverksettingStatus())) {
            log.info("Behandling {}: Iverksetter vedtak", behandlingId);
            vedtak.setIverksettingStatus(IverksettingStatus.UNDER_IVERKSETTING);
            behandlingVedtakRepository.lagre(vedtak);

            boolean sendVedtaksbrev = !behandling.isAutomatiskSaksbehandlet() && !erRevurderingOpprettetForKlage(behandling);
            prosessTaskIverksett.opprettIverksettingstasker(behandling, sendVedtaksbrev);
            return BehandleStegResultat.settPåVent();
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    @Override
    public final BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        log.info("Behandling {}: Iverksetting fullført", kontekst.getBehandlingId());
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private boolean erRevurderingOpprettetForKlage(Behandling behandling) {
        return BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType()) &&
                behandling.getBehandlingÅrsaker().stream()
                        .anyMatch(årsak -> BehandlingÅrsakType.KLAGE_ÅRSAKER.contains(årsak.getBehandlingÅrsakType()));
    }
}
