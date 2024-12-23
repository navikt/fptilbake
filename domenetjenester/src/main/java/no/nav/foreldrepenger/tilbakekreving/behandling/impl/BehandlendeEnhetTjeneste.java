package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;

@ApplicationScoped
public class BehandlendeEnhetTjeneste {

    private HistorikkRepositoryTeamAware historikkRepository;
    private BehandlingRepository behandlingRepository;

    private BehandlingEventPubliserer eventPubliserer;

    BehandlendeEnhetTjeneste() {
        // for CDI proxy
    }

    @Inject
    public BehandlendeEnhetTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                    BehandlingEventPubliserer eventPubliserer) {
        this.historikkRepository = repositoryProvider.getHistorikkRepositoryTeamAware();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eventPubliserer = eventPubliserer;
    }

    public void byttBehandlendeEnhet(Long behandlingId, OrganisasjonsEnhet nyEnhet, HistorikkAktør aktør) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        lagHistorikkInnslagForByttBehandlendeEnhet(behandling, nyEnhet, aktør);

        behandling.setBehandlendeOrganisasjonsEnhet(nyEnhet);
        behandlingRepository.lagre(behandling, lås);
        eventPubliserer.fireEvent(new BehandlingEnhetEvent(behandling));
    }

    private void lagHistorikkInnslagForByttBehandlendeEnhet(Behandling behandling, OrganisasjonsEnhet nyEnhet, HistorikkAktør aktør) {
        var historikkinnslag = lagHistorikkinnslag(behandling, nyEnhet, aktør);
        var historikkinnslag2 = lagHistorikkinnslag2(behandling, nyEnhet, aktør);
        historikkRepository.lagre(historikkinnslag, historikkinnslag2);
    }

    private static Historikkinnslag2 lagHistorikkinnslag2(Behandling behandling, OrganisasjonsEnhet nyEnhet, HistorikkAktør aktør) {
        var eksisterende = behandling.getBehandlendeOrganisasjonsEnhet();
        var fraMessage = eksisterende != null ? eksisterende.getEnhetId() + " " + eksisterende.getEnhetId() : "ukjent";
        return new Historikkinnslag2.Builder()
            .medAktør(aktør)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel("Bytt enhet")
            .addLinje(fraTilEquals("Behandlende enhet", fraMessage, nyEnhet.getEnhetId() + " " + nyEnhet.getEnhetNavn()))
            .build();
    }

    private static Historikkinnslag lagHistorikkinnslag(Behandling behandling, OrganisasjonsEnhet nyEnhet, HistorikkAktør aktør) {
        OrganisasjonsEnhet eksisterende = behandling.getBehandlendeOrganisasjonsEnhet();
        String fraMessage = eksisterende != null ? eksisterende.getEnhetId() + " " + eksisterende.getEnhetNavn() : "ukjent";
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
                .medHendelse(HistorikkinnslagType.BYTT_ENHET)
                .medEndretFelt(HistorikkEndretFeltType.BEHANDLENDE_ENHET,
                        fraMessage,
                        nyEnhet.getEnhetId() + " " + nyEnhet.getEnhetNavn());

        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(aktør);
        innslag.setType(HistorikkinnslagType.BYTT_ENHET);
        innslag.setBehandlingId(behandling.getId());
        builder.build(innslag);
        return innslag;
    }
}
