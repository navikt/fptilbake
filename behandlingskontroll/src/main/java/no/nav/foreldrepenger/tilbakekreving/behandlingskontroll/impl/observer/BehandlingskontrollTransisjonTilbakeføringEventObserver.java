package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.observer;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

/**
 * Håndtere opprydding i Aksjonspunkt og Vilkår ved overhopp framover eller tilbakeføring.
 */
@ApplicationScoped
public class BehandlingskontrollTransisjonTilbakeføringEventObserver {

    private BehandlingRepository behandlingRepository;
    private BehandlingModellRepository modellRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingskontrollEventPubliserer eventPubliserer = BehandlingskontrollEventPubliserer.NULL_EVENT_PUB;

    @Inject
    public BehandlingskontrollTransisjonTilbakeføringEventObserver(BehandlingRepositoryProvider repositoryProvider,
                                                                   BehandlingModellRepository modellRepository, BehandlingskontrollEventPubliserer eventPubliserer) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.modellRepository = modellRepository;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

        if (eventPubliserer != null) {
            this.eventPubliserer = eventPubliserer;
        }
    }

    protected BehandlingskontrollTransisjonTilbakeføringEventObserver() {
        // for CDI proxy
    }

    public void observerBehandlingSteg(@Observes BehandlingStegOvergangEvent.BehandlingStegTilbakeføringEvent event) {
        Long behandlingId = event.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingModell modell = modellRepository.getModell(behandling.getType());
        guardIngenÅpneAutopunkter(behandling);

        BehandlingStegType førsteSteg = event.getFørsteSteg();
        BehandlingStegType sisteSteg = event.getSisteSteg();

        Optional<BehandlingStegStatus> førsteStegStatus = event.getFørsteStegStatus();

        boolean medInngangFørsteSteg = !førsteStegStatus.isPresent() || førsteStegStatus.get().erVedInngang();

        Set<String> aksjonspunktDefinisjonerEtterFra = modell.finnAksjonspunktDefinisjonerFraOgMed(førsteSteg, medInngangFørsteSteg);

        List<Aksjonspunkt> endredeAksjonspunkter = håndterAksjonspunkter(behandling, aksjonspunktDefinisjonerEtterFra, event,
                new HåndterRyddingAvAksjonspunktVedTilbakeføring(aksjonspunktRepository, førsteSteg, modell));

        modell.hvertStegFraOgMedTil(førsteSteg, sisteSteg, false)
                .collect(Collectors.toCollection(ArrayDeque::new))
                .descendingIterator() // stepper bakover
                .forEachRemaining(s -> s.getSteg().vedTransisjon(event.getKontekst(), behandling, s, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, førsteSteg, sisteSteg, event.getSkalTil()));

        aksjonspunkterTilbakeført(event.getKontekst(), endredeAksjonspunkter, event.getFraStegType());
    }

    private List<Aksjonspunkt> håndterAksjonspunkter(Behandling behandling, Set<String> mellomliggendeAksjonspunkt,
                                                     BehandlingStegOvergangEvent.BehandlingStegTilbakeføringEvent event, Consumer<Aksjonspunkt> action) {
        List<Aksjonspunkt> endredeAksjonspunkter = behandling.getAlleAksjonspunkterInklInaktive().stream()
                .filter(a -> a.erAktivt() || a.erManueltOpprettet())
                .filter(a -> !a.erAutopunkt()) // Autopunkt skal ikke håndteres; skal alltid være lukket ved tilbakehopp
                .filter(a -> mellomliggendeAksjonspunkt.contains(a.getAksjonspunktDefinisjon().getKode()))
                .collect(Collectors.toList());

        endredeAksjonspunkter.forEach(action);

        behandlingRepository.lagre(behandling, event.getKontekst().getSkriveLås());
        return endredeAksjonspunkter;
    }

    private void guardIngenÅpneAutopunkter(Behandling behandling) {
        Optional<Aksjonspunkt> autopunkt = behandling.getAksjonspunkter().stream()
                .filter(Aksjonspunkt::erAutopunkt)
                .filter(Aksjonspunkt::erÅpentAksjonspunkt)
                .findFirst();

        if (autopunkt.isPresent()) {
            throw new IllegalStateException(
                    "Utvikler-feil: Tilbakehopp ikke tillatt for autopunkt '" + //$NON-NLS-1$
                            autopunkt.get().getAksjonspunktDefinisjon().getNavn() + "'"); //$NON-NLS-1$
        }
    }

    private void aksjonspunkterTilbakeført(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter, BehandlingStegType behandlingStegType) {
        if (!aksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunktTilbakeførtEvent(kontekst, aksjonspunkter, behandlingStegType));
        }
    }

}
