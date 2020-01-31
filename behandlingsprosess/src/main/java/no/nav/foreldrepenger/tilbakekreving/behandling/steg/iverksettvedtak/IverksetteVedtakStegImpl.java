package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;

@BehandlingStegRef(kode = "IVEDSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class IverksetteVedtakStegImpl implements IverksetteVedtakSteg {

    private static final Logger log = LoggerFactory.getLogger(IverksetteVedtakStegImpl.class);

    private BehandlingVedtakRepository behandlingVedtakRepository;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskIverksett prosessTaskIverksett;


    IverksetteVedtakStegImpl() {
        // for CDI proxy
    }

    @Inject
    public IverksetteVedtakStegImpl(BehandlingRepositoryProvider repositoryProvider, ProsessTaskIverksett prosessTaskIverksett) {
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.prosessTaskIverksett = prosessTaskIverksett;
    }


    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();

        Optional<BehandlingVedtak> fantVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        if (!fantVedtak.isPresent()) {
            throw BehandlingRepositoryFeil.FACTORY.fantIkkeBehandlingVedtak(behandlingId).toException();
        }
        BehandlingVedtak vedtak = fantVedtak.get();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (IverksettingStatus.IKKE_IVERKSATT.equals(vedtak.getIverksettingStatus())) {
            log.info("Behandling {}: Iverksetter vedtak", behandlingId);
            vedtak.setIverksettingStatus(IverksettingStatus.UNDER_IVERKSETTING);
            behandlingVedtakRepository.lagre(vedtak);

            boolean kanSendeVedtaksBrev = erRevurderingOpprettesForKlage(behandling);
            prosessTaskIverksett.opprettIverksettingstasker(behandling,kanSendeVedtaksBrev);
            return BehandleStegResultat.settPåVent();
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    @Override
    public final BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        log.info("Behandling {}: Iverksetting fullført", kontekst.getBehandlingId());
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private boolean erRevurderingOpprettesForKlage(Behandling behandling){
        if(BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType())){
            List<BehandlingÅrsak> behandlingÅrsaker = behandling.getBehandlingÅrsaker();
            return behandlingÅrsaker.stream()
                .anyMatch(behandlingÅrsak -> BehandlingÅrsakType.klageÅrsaker()
                    .contains(behandlingÅrsak.getBehandlingÅrsakType()));
        }
        return false;
    }
}
