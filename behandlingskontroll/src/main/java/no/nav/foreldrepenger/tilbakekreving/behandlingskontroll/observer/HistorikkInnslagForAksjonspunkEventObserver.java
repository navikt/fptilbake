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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;

/**
 * Observerer Aksjonspunkt*Events og registrerer HistorikkInnslag for enkelte hendelser (eks. gjenoppta og behandling på vent)
 */
@ApplicationScoped
public class HistorikkInnslagForAksjonspunkEventObserver {

    private HistorikkRepositoryTeamAware historikkRepository;
    private BehandlingRepository behandlingRepository;

    private HistorikkInnslagForAksjonspunkEventObserver() {
        // CDI
    }

    @Inject
    public HistorikkInnslagForAksjonspunkEventObserver(HistorikkRepositoryTeamAware historikkRepository, BehandlingRepository behandlingRepository) {
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
                        "Behandling på vent", HistorikkinnslagType.BEH_VENT, frist, venteårsak);
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
                        "Behandling gjenopptatt", HistorikkinnslagType.BEH_GJEN, null, null);
                }
            }
        }
    }

    private void opprettHistorikkinnslagForVenteFristRelaterteInnslag(Long behandlingId,
                                                                      Long fagsakId,
                                                                      String tittel,
                                                                      HistorikkinnslagType historikkinnslagType,
                                                                      LocalDateTime frist,
                                                                      Venteårsak venteårsak) {
        var historikkinnslag = lagHistorikkinnslag(behandlingId, fagsakId, historikkinnslagType, frist, venteårsak);
        var historikkinnslag2 = lagHistorikkinnslag2(behandlingId, fagsakId, tittel, frist, venteårsak);
        historikkRepository.lagre(historikkinnslag, historikkinnslag2);
    }

    private static Historikkinnslag2 lagHistorikkinnslag2(Long behandlingId,
                                                          Long fagsakId,
                                                          String tittel,
                                                          LocalDateTime fristTid,
                                                          Venteårsak venteårsak) {
        var historikkinnslagBuilder = new Historikkinnslag2.Builder();
        if (fristTid != null) {
            historikkinnslagBuilder.medTittel(tittel + " " + HistorikkinnslagLinjeBuilder.format(fristTid.toLocalDate()));
        } else {
            historikkinnslagBuilder.medTittel(tittel);
        }
        if (venteårsak != null) {
            historikkinnslagBuilder.addLinje(venteårsak.getNavn());
        }
        var erSystemBruker = SikkerhetContext.SYSTEM.equals(KontekstHolder.getKontekst().getContext()) ||
            Optional.ofNullable(KontekstHolder.getKontekst().getIdentType()).filter(IdentType::erSystem).isPresent() ||
            Optional.ofNullable(KontekstHolder.getKontekst().getUid()).map(String::toLowerCase).filter(s -> s.startsWith("srv")).isPresent();
        historikkinnslagBuilder
            .medAktør(erSystemBruker ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medBehandlingId(behandlingId)
            .medFagsakId(fagsakId);
        return historikkinnslagBuilder.build();
    }

    private static Historikkinnslag lagHistorikkinnslag(Long behandlingId,
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
        return historikkinnslag;
    }
}
