package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.observer;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;

/**
 * Observerer Aksjonspunkt*Events og registrerer HistorikkInnslag for enkelte hendelser (eks. gjenoppta og behandling på vent)
 */
@ApplicationScoped
public class HistorikkInnslagForAksjonspunkEventObserver {

    private HistorikkRepository historikkRepository;

    private HistorikkInnslagForAksjonspunkEventObserver() {
        // CDI
    }

    @Inject
    public HistorikkInnslagForAksjonspunkEventObserver(HistorikkRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    public void oppretteHistorikkForBehandlingPåVent(@Observes AksjonspunktStatusEvent aksjonspunkterFunnetEvent) {
        BehandlingskontrollKontekst ktx = aksjonspunkterFunnetEvent.getKontekst();
        for (Aksjonspunkt aksjonspunkt : aksjonspunkterFunnetEvent.getAksjonspunkter()) {
            if (AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && aksjonspunkt.getFristTid() != null) {
                LocalDateTime frist = aksjonspunkt.getFristTid();
                Venteårsak venteårsak = aksjonspunkt.getVenteårsak();
                opprettHistorikkinnslagForVenteFristRelaterteInnslag(ktx.getBehandlingId(), ktx.getFagsakId(),
                        HistorikkinnslagType.BEH_VENT, frist, venteårsak);
            }
        }
    }

    public void oppretteHistorikkForGjenopptattBehandling(@Observes AksjonspunktStatusEvent aksjonspunkterFunnetEvent) {
        for (Aksjonspunkt aksjonspunkt : aksjonspunkterFunnetEvent.getAksjonspunkter()) {
            BehandlingskontrollKontekst ktx = aksjonspunkterFunnetEvent.getKontekst();

            if (!AksjonspunktStatus.OPPRETTET.equals(aksjonspunkt.getStatus()) && aksjonspunkt.getFristTid() != null) {
                opprettHistorikkinnslagForVenteFristRelaterteInnslag(ktx.getBehandlingId(), ktx.getFagsakId(),
                        HistorikkinnslagType.BEH_GJEN, null, null);
            }
        }
    }

    private void opprettHistorikkinnslagForVenteFristRelaterteInnslag(Long behandlingId,
                                                                      Long fagsakId,
                                                                      HistorikkinnslagType historikkinnslagType,
                                                                      LocalDateTime frist,
                                                                      Venteårsak venteårsak) {
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        if (frist != null) {
            builder.medHendelse(historikkinnslagType, frist.toLocalDate());
        } else {
            builder.medHendelse(historikkinnslagType);
        }
        if (venteårsak != null) {
            builder.medÅrsak(venteårsak);
        }
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        var erSystemBruker = SikkerhetContext.SYSTEM.equals(KontekstHolder.getKontekst().getContext()) ||
            Optional.ofNullable(KontekstHolder.getKontekst().getIdentType()).filter(IdentType::erSystem).isPresent() ||
            Optional.ofNullable(KontekstHolder.getKontekst().getUid()).map(String::toLowerCase).filter(s -> s.startsWith("srv")).isPresent();
        historikkinnslag.setAktør(erSystemBruker ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER);
        historikkinnslag.setType(historikkinnslagType);
        historikkinnslag.setBehandlingId(behandlingId);
        historikkinnslag.setFagsakId(fagsakId);
        builder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }
}
