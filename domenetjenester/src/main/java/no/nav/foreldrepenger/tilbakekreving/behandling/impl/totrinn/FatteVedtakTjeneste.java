package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VedtakAksjonspunktData;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.SkjermlenkeTjeneste;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class FatteVedtakTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private HistorikkRepository historikkRepository;

    FatteVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public FatteVedtakTjeneste(BehandlingRepositoryProvider repositoryProvider,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               TotrinnTjeneste totrinnTjeneste) {
        this.historikkRepository = repositoryProvider.getHistorikkRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    public void opprettTotrinnsVurdering(Behandling behandling, Collection<VedtakAksjonspunktData> aksjonspunkter) {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandling.setAnsvarligBeslutter(SubjectHandler.getSubjectHandler().getUid());

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
            lagHistorikkInnslagVurderPåNytt(behandling, totrinnsvurderings);
            return;
        }

        lagHistorikkInnslagVedtakFattet(behandling);
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> medTotrinnskontroll) {
        return medTotrinnskontroll.stream()
                .anyMatch(a -> !Boolean.TRUE.equals(a.isGodkjent()));
    }

    private void lagHistorikkInnslagVurderPåNytt(Behandling behandling, Collection<Totrinnsvurdering> medTotrinnskontroll) {
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

        historikkRepository.lagre(lagHistorikkinnslag(behandling, HistorikkinnslagType.SAK_RETUR, delBuilder));
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

    private void lagHistorikkInnslagVedtakFattet(Behandling behandling) {
        HistorikkinnslagType historikkinnslagType = HistorikkinnslagType.VEDTAK_FATTET;
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
                .medHendelse(historikkinnslagType)
                .medSkjermlenke(SkjermlenkeType.VEDTAK);
        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(HistorikkAktør.BESLUTTER); //det er alltid totrinnsvurdering for tilbakerkrevingssaker
        innslag.setType(historikkinnslagType);
        innslag.setBehandling(behandling);
        tekstBuilder.build(innslag);

        historikkRepository.lagre(innslag);
    }

}
