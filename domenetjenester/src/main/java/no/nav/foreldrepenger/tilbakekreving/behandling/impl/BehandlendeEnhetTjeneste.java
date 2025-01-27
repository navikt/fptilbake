package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

@ApplicationScoped
public class BehandlendeEnhetTjeneste {

    private HistorikkinnslagRepository historikkRepository;
    private BehandlingRepository behandlingRepository;

    private BehandlingEventPubliserer eventPubliserer;

    BehandlendeEnhetTjeneste() {
        // for CDI proxy
    }

    @Inject
    public BehandlendeEnhetTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                    BehandlingEventPubliserer eventPubliserer) {
        this.historikkRepository = repositoryProvider.getHistorikkinnslagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eventPubliserer = eventPubliserer;
    }

    public void byttBehandlendeEnhet(Long behandlingId, OrganisasjonsEnhet nyEnhet, HistorikkAktør aktør) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        historikkRepository.lagre(lagHistorikkinnslag2(behandling, nyEnhet, aktør));

        behandling.setBehandlendeOrganisasjonsEnhet(nyEnhet);
        behandlingRepository.lagre(behandling, lås);
        eventPubliserer.fireEvent(new BehandlingEnhetEvent(behandling));
    }

    private static Historikkinnslag lagHistorikkinnslag2(Behandling behandling, OrganisasjonsEnhet nyEnhet, HistorikkAktør aktør) {
        var eksisterende = behandling.getBehandlendeOrganisasjonsEnhet();
        var fraMessage = eksisterende != null ? eksisterende.getEnhetId() + " " + eksisterende.getEnhetId() : "ukjent";
        return new Historikkinnslag.Builder()
            .medAktør(aktør)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel("Bytt enhet")
            .addLinje(fraTilEquals("Behandlende enhet", fraMessage, nyEnhet.getEnhetId() + " " + nyEnhet.getEnhetNavn()))
            .build();
    }
}
