package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.observer;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;

/**
 * Observerer Aksjonspunkt*Events og registrerer HistorikkInnslag for enkelte hendelser (eks. gjenoppta og behandling på vent)
 */
@ApplicationScoped
public class HistorikkInnslagForAksjonspunkEventObserver {

    private HistorikkinnslagRepository historikkRepository;
    private BehandlingRepository behandlingRepository;

    private HistorikkInnslagForAksjonspunkEventObserver() {
        // CDI
    }

    @Inject
    public HistorikkInnslagForAksjonspunkEventObserver(HistorikkinnslagRepository historikkRepository, BehandlingRepository behandlingRepository) {
        this.historikkRepository = historikkRepository;
        this.behandlingRepository = behandlingRepository;
    }

    public void oppretteHistorikkForBehandlingPåVent(@Observes AksjonspunktStatusEvent aksjonspunkterFunnetEvent) {
        BehandlingskontrollKontekst ktx = aksjonspunkterFunnetEvent.getKontekst();
        for (Aksjonspunkt aksjonspunkt : aksjonspunkterFunnetEvent.getAksjonspunkter()) {
            if (AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && aksjonspunkt.getFristTid() != null) {
                LocalDateTime frist = aksjonspunkt.getFristTid();
                Venteårsak venteårsak = aksjonspunkt.getVenteårsak();
                opprettHistorikkinnslagForVenteFristRelaterteInnslag(ktx.getBehandlingId(), ktx.getFagsakId(),
                        "Behandlingen er satt på vent", frist, venteårsak);
            }
        }
    }

    public void oppretteHistorikkForGjenopptattBehandling(@Observes AksjonspunktStatusEvent aksjonspunkterFunnetEvent) {
        for (Aksjonspunkt aksjonspunkt : aksjonspunkterFunnetEvent.getAksjonspunkter()) {
            BehandlingskontrollKontekst ktx = aksjonspunkterFunnetEvent.getKontekst();

            if (aksjonspunkt.erUtført() && aksjonspunkt.getFristTid() != null) {
                // Unngå dobbelinnslag (innslag ved manuellTaAvVent) + konvensjon med påVent->SBH=null og manuellGjenoppta->SBH=ident
                var manueltTattAvVent = Optional.ofNullable(behandlingRepository.hentBehandling(ktx.getBehandlingId()))
                    .map(Behandling::getAnsvarligSaksbehandler).isPresent();
                if (!manueltTattAvVent) {
                    opprettHistorikkinnslagForVenteFristRelaterteInnslag(ktx.getBehandlingId(), ktx.getFagsakId(),
                        "Behandling er gjenopptatt", null, null);
                }
            }
        }
    }


    private void opprettHistorikkinnslagForVenteFristRelaterteInnslag(Long behandlingId, Long fagsakId, String tittel, LocalDateTime fristTid, Venteårsak venteårsak) {
        var historikkinnslagBuilder = new Historikkinnslag.Builder();
        if (fristTid != null) {
            historikkinnslagBuilder.medTittel(tittel + " " + HistorikkinnslagLinjeBuilder.format(fristTid.toLocalDate()));
        } else {
            historikkinnslagBuilder.medTittel(tittel);
        }
        if (venteårsak != null && !Venteårsak.UDEFINERT.equals(venteårsak)) {
            historikkinnslagBuilder.addLinje(venteårsak.getNavn());
        }
        var erSystemBruker = SikkerhetContext.SYSTEM.equals(KontekstHolder.getKontekst().getContext()) ||
            Optional.ofNullable(KontekstHolder.getKontekst().getIdentType()).filter(IdentType::erSystem).isPresent() ||
            Optional.ofNullable(KontekstHolder.getKontekst().getUid()).map(String::toLowerCase).filter(s -> s.startsWith("srv")).isPresent();
        historikkinnslagBuilder
            .medAktør(erSystemBruker ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medBehandlingId(behandlingId)
            .medFagsakId(fagsakId);
        historikkRepository.lagre(historikkinnslagBuilder.build());
    }
}
