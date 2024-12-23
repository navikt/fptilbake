package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VedtakAksjonspunktData;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@ApplicationScoped
public class FatteVedtakTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private HistorikkinnslagRepository historikkRepository;

    FatteVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public FatteVedtakTjeneste(BehandlingRepositoryProvider repositoryProvider,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               TotrinnTjeneste totrinnTjeneste) {
        this.historikkRepository = repositoryProvider.getHistorikkinnslagRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    public void opprettTotrinnsVurdering(Behandling behandling, Collection<VedtakAksjonspunktData> aksjonspunkter) {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandling.setAnsvarligBeslutter(KontekstHolder.getKontekst().getUid());

        List<Totrinnsvurdering> totrinnsvurderinger = new ArrayList<>();
        List<Aksjonspunkt> skalReåpnes = new ArrayList<>();

        for (VedtakAksjonspunktData aks : aksjonspunkter) {
            Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aks.getAksjonspunktDefinisjon());
            if (!aks.isGodkjent()) {
                skalReåpnes.add(aksjonspunkt);
            }
            Set<VurderÅrsak> vurderÅrsaker = aks.getVurderÅrsakskoder().stream().map(VurderÅrsak::fraKode).collect(Collectors.toSet());

            Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medBehandling(behandling)
                    .medAksjonspunktDefinisjon(aksjonspunkt.getAksjonspunktDefinisjon())
                    .medGodkjent(aks.isGodkjent())
                    .medBegrunnelse(aks.getBegrunnelse()).build();

            vurderÅrsaker.forEach(totrinnsvurdering::leggTilVurderÅrsakTotrinnsvurdering);
            totrinnsvurderinger.add(totrinnsvurdering);
        }
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, totrinnsvurderinger);
        lagHistorikkinnslagFattVedtak(behandling);
        // Noe spesialhåndtering ifm totrinn og tilbakeføring fra FVED
        if (!skalReåpnes.isEmpty()) {
            behandlingskontrollTjeneste.lagreAksjonspunkterReåpnet(kontekst, skalReåpnes, false, true);
        }
    }

    public void lagHistorikkinnslagFattVedtak(Behandling behandling) {
        Collection<Totrinnsvurdering> totrinnsvurderings = totrinnTjeneste.hentTotrinnsvurderinger(behandling);
        if (sendesTilbakeTilSaksbehandler(totrinnsvurderings)) {
            var historikkinnslag = new Historikkinnslag.Builder()
                .medAktør(HistorikkAktør.BESLUTTER)
                .medFagsakId(behandling.getFagsakId())
                .medBehandlingId(behandling.getId())
                .medTittel("Sak retur")
                .medLinjer(lagTekstForHverTotrinnkontroll(totrinnsvurderings))
                .build();
            historikkRepository.lagre(historikkinnslag);
        } else {
            var historikkinnslag = new Historikkinnslag.Builder()
                .medAktør(HistorikkAktør.BESLUTTER) // det er alltid totrinnsvurdering for tilbakerkrevingssaker
                .medFagsakId(behandling.getFagsakId())
                .medBehandlingId(behandling.getId())
                .medTittel(SkjermlenkeType.VEDTAK)
                .addLinje("Vedtak er fattet")
                .build();
            historikkRepository.lagre(historikkinnslag);
        }
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return medTotrinnskontroll.stream()
                .anyMatch(a -> !Boolean.TRUE.equals(a.isGodkjent()));
    }

    private static List<HistorikkinnslagLinjeBuilder> lagTekstForHverTotrinnkontroll(Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return medTotrinnskontroll.stream()
            .sorted(Comparator.comparing(ttv -> ttv.getEndretTidspunkt() != null ? ttv.getEndretTidspunkt() : ttv.getOpprettetTidspunkt()))
            .map(FatteVedtakTjeneste::tilHistorikkinnslagTekst)
            .map(FatteVedtakTjeneste::leggTilLinjeskift)
            .flatMap(Collection::stream)
            .toList();
    }

    private static List<HistorikkinnslagLinjeBuilder> tilHistorikkinnslagTekst(Totrinnsvurdering ttv) {
        var aksjonspunktNavn = ttv.getAksjonspunktDefinisjon().getNavn();
        if (Boolean.TRUE.equals(ttv.isGodkjent())) {
            return List.of(new HistorikkinnslagLinjeBuilder().bold(aksjonspunktNavn + ":").bold("Godkjent"));
        }
        var linjer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        linjer.add(new HistorikkinnslagLinjeBuilder().bold(aksjonspunktNavn + ":").bold("Må vurderes på nytt"));
        if (ttv.getBegrunnelse() != null) {
            linjer.add(plainTekstLinje(String.format("Kommentar: %s", ttv.getBegrunnelse())));
        }
        return linjer;
    }

    private static List<HistorikkinnslagLinjeBuilder> leggTilLinjeskift(List<HistorikkinnslagLinjeBuilder> eksistrendeLinjer) {
        var linjer = new ArrayList<>(eksistrendeLinjer);
        linjer.add(HistorikkinnslagLinjeBuilder.LINJESKIFT);
        return linjer;
    }
}
