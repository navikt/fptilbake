package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.SkjermlenkeTjeneste;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;

@ApplicationScoped
public class FatteVedtakTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private HistorikkRepositoryTeamAware historikkRepository;

    FatteVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public FatteVedtakTjeneste(BehandlingRepositoryProvider repositoryProvider,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               TotrinnTjeneste totrinnTjeneste) {
        this.historikkRepository = repositoryProvider.getHistorikkRepositoryTeamAware();
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
            var historikkinnslag = lagHistorikkInnslagVurderPåNytt(behandling, totrinnsvurderings);
            var historikkinnslag2 = lagHistorikkInnslag2VurderPåNytt(behandling, totrinnsvurderings);
            historikkRepository.lagre(historikkinnslag, historikkinnslag2);
            return;
        }

        var historikkinnslag = lagHistorikkInnslagVedtakFattet(behandling);
        var historikkinnslag2 = lagHistorikkInnslag2VedtakFattet(behandling);
        historikkRepository.lagre(historikkinnslag, historikkinnslag2);
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return medTotrinnskontroll.stream()
                .anyMatch(a -> !Boolean.TRUE.equals(a.isGodkjent()));
    }

    private static Historikkinnslag2 lagHistorikkInnslag2VurderPåNytt(Behandling behandling, Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return new Historikkinnslag2.Builder()
            .medAktør(HistorikkAktør.BESLUTTER)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel("Sak retur")
            .medLinjer(lagTekstForHverTotrinnkontroll(medTotrinnskontroll))
            .build();
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
            return List.of(new HistorikkinnslagLinjeBuilder().bold(aksjonspunktNavn).bold("er godkjent"));
        }
        var linjer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        linjer.add(new HistorikkinnslagLinjeBuilder().bold(aksjonspunktNavn).bold("må vurderes på nytt"));
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

    private Historikkinnslag lagHistorikkInnslagVurderPåNytt(Behandling behandling, Collection<Totrinnsvurdering> medTotrinnskontroll) {
        Map<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> vurdering = new HashMap<>();
        List<HistorikkinnslagTotrinnsvurdering> vurderingUtenLenke = new ArrayList<>();

        HistorikkInnslagTekstBuilder delBuilder = new HistorikkInnslagTekstBuilder()
                .medHendelse(HistorikkinnslagType.SAK_RETUR);

        for (Totrinnsvurdering ttv : medTotrinnskontroll) {
            HistorikkinnslagTotrinnsvurdering totrinnsVurdering = lagHistorikkinnslagTotrinnsvurdering(ttv);
            LocalDateTime sistEndret = ttv.getEndretTidspunkt() != null ? ttv.getEndretTidspunkt() : ttv.getOpprettetTidspunkt();
            totrinnsVurdering.setAksjonspunktSistEndret(sistEndret);
            SkjermlenkeType skjermlenkeType = SkjermlenkeTjeneste.finnSkjermlenkeType(ttv.getAksjonspunktDefinisjon());
            if (skjermlenkeType != SkjermlenkeType.UDEFINERT) {
                List<HistorikkinnslagTotrinnsvurdering> aksjonspktVurderingListe = vurdering.computeIfAbsent(skjermlenkeType,
                        k -> new ArrayList<>());
                aksjonspktVurderingListe.add(totrinnsVurdering);
            } else {
                vurderingUtenLenke.add(totrinnsVurdering);
            }
        }
        delBuilder.medTotrinnsvurdering(vurdering, vurderingUtenLenke);

        return lagHistorikkinnslag(behandling, HistorikkinnslagType.SAK_RETUR, delBuilder);
    }

    private Historikkinnslag lagHistorikkinnslag(Behandling behandling, HistorikkinnslagType historikkinnslagType,
                                                 HistorikkInnslagTekstBuilder builder) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setBehandling(behandling);
        historikkinnslag.setAktør(HistorikkAktør.BESLUTTER);
        historikkinnslag.setType(historikkinnslagType);
        builder.build(historikkinnslag);
        return historikkinnslag;
    }

    private HistorikkinnslagTotrinnsvurdering lagHistorikkinnslagTotrinnsvurdering(Totrinnsvurdering ttv) {
        HistorikkinnslagTotrinnsvurdering totrinnsVurdering = new HistorikkinnslagTotrinnsvurdering();
        totrinnsVurdering.setAksjonspunktDefinisjon(ttv.getAksjonspunktDefinisjon());
        totrinnsVurdering.setBegrunnelse(ttv.getBegrunnelse());
        totrinnsVurdering.setGodkjent(Boolean.TRUE.equals(ttv.isGodkjent()));
        return totrinnsVurdering;
    }

    private static Historikkinnslag lagHistorikkInnslagVedtakFattet(Behandling behandling) {
        HistorikkinnslagType historikkinnslagType = HistorikkinnslagType.VEDTAK_FATTET;
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
                .medHendelse(historikkinnslagType)
                .medSkjermlenke(SkjermlenkeType.VEDTAK);
        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(HistorikkAktør.BESLUTTER); //det er alltid totrinnsvurdering for tilbakerkrevingssaker
        innslag.setType(historikkinnslagType);
        innslag.setBehandling(behandling);
        tekstBuilder.build(innslag);
        return innslag;
    }

    private static Historikkinnslag2 lagHistorikkInnslag2VedtakFattet(Behandling behandling) {
        return new Historikkinnslag2.Builder()
            .medAktør(HistorikkAktør.BESLUTTER) //det er alltid totrinnsvurdering for tilbakerkrevingssaker
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel(SkjermlenkeType.VEDTAK)
            .addLinje("Vedtak fattet")
            .build();
    }

}
