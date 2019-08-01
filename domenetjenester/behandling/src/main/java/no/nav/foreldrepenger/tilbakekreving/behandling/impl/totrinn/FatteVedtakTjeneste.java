package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.VedtakAksjonspunktData;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagTotrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.SkjermlenkeTjeneste;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class FatteVedtakTjeneste {

    private AksjonspunktRepository aksjonspunktRepository;
    private KodeverkTabellRepository kodeverkTabellRepository;
    private TotrinnTjeneste totrinnTjeneste;
    private HistorikkRepository historikkRepository;

    FatteVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public FatteVedtakTjeneste(BehandlingRepositoryProvider repositoryProvider, TotrinnTjeneste totrinnTjeneste) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepository();
        this.totrinnTjeneste = totrinnTjeneste;
    }

    public void opprettTotrinnsVurdering(Behandling behandling, Collection<VedtakAksjonspunktData> aksjonspunkter) {
        behandling.setAnsvarligBeslutter(SubjectHandler.getSubjectHandler().getUid());

        List<Totrinnsvurdering> totrinnsvurderinger = new ArrayList<>();

        for (VedtakAksjonspunktData aks : aksjonspunkter) {
            Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aks.getAksjonspunktDefinisjon());
            if (!aks.isGodkjent()) {
                aksjonspunktRepository.setReåpnet(aksjonspunkt);
                aksjonspunktRepository.setToTrinnsBehandlingKreves(aksjonspunkt);
            }
            Collection<VurderÅrsak> vurderÅrsaker = kodeverkTabellRepository.finnVurderÅrsaker(aks.getVurderÅrsakskoder());

            Totrinnsvurdering totrinnsvurdering = Totrinnsvurdering.builder().medBehandling(behandling)
                    .medAksjonspunktDefinisjon(aksjonspunkt.getAksjonspunktDefinisjon())
                    .medGodkjent(aks.isGodkjent())
                    .medBegrunnelse(aks.getBegrunnelse()).build();

            vurderÅrsaker.forEach(vurderÅrsak -> totrinnsvurdering.leggTilVurderÅrsakTotrinnsvurdering(vurderÅrsak));
            totrinnsvurderinger.add(totrinnsvurdering);
        }
        totrinnTjeneste.settNyeTotrinnaksjonspunktvurderinger(behandling, totrinnsvurderinger);
        lagHistorikkinnslagFattVedtak(behandling);
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
