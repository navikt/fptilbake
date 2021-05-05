package no.nav.foreldrepenger.tilbakekreving.behandling.steg.fattevedtak;

import static java.lang.Boolean.TRUE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

@BehandlingStegRef(kode = "FVEDSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class FatteVedtakSteg implements BehandlingSteg {

    private static final String DEFAULT_ANSVARLIG_SAKSBEHANDLER = "VL";

    private TotrinnRepository totrinnRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private TilbakekrevingBeregningTjeneste beregningTjeneste;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    FatteVedtakSteg() {
        // for CDI proxy
    }

    @Inject
    public FatteVedtakSteg(BehandlingRepositoryProvider repositoryProvider, TotrinnRepository totrinnRepository,
                           TilbakekrevingBeregningTjeneste beregningTjeneste,
                           HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.totrinnRepository = totrinnRepository;
        this.behandlingresultatRepository = repositoryProvider.getBehandlingresultatRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.beregningTjeneste = beregningTjeneste;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Collection<Totrinnsvurdering> totrinnsvurderinger = totrinnRepository.hentTotrinnsvurderinger(behandling);
        if (sendesTilbakeTilSaksbehandler(totrinnsvurderinger)) {
            List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = totrinnsvurderinger.stream()
                .filter(totrinnsvurdering -> !TRUE.equals(totrinnsvurdering.isGodkjent()))
                .map(Totrinnsvurdering::getAksjonspunktDefinisjon).collect(Collectors.toList());
            return BehandleStegResultat.tilbakeførtMedAksjonspunkter(aksjonspunktDefinisjoner);
        } else {
            //TODO Velge mer fingranulert ved revurdering
            opprettBehandlingVedtak(behandling);
            if (behandling.isAutomatiskSaksbehandlet()) {
                behandling.setAnsvarligBeslutter("VL");
                lagHistorikksinnslagForAutomatiskSaksbehandling(behandling);
            }
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> totrinnsvurderinger) {
        return totrinnsvurderinger.stream()
            .anyMatch(totrinnsvurdering -> !TRUE.equals(totrinnsvurdering.isGodkjent()));
    }

    private void opprettBehandlingVedtak(Behandling behandling) {
        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandling.getId());
        BehandlingResultatType behandlingResultatType = BehandlingResultatType.fraVedtakResultatType(beregningResultat.getVedtakResultatType());

        Optional<BehandlingVedtak> eksisterendeVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId());
        Behandlingsresultat behandlingsresultat;
        BehandlingVedtak behandlingVedtak;
        if (eksisterendeVedtak.isPresent()) {
            BehandlingVedtak eksisterende = eksisterendeVedtak.get();
            behandlingsresultat = oppdaterBehandlingsResultat(behandling, eksisterende.getBehandlingsresultat(), behandlingResultatType);
            behandlingVedtak = BehandlingVedtak.builderEndreEksisterende(eksisterende)
                .medAnsvarligSaksbehandler(finnSaksBehandler(behandling))
                .medBehandlingsresultat(behandlingsresultat)
                .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
                .medVedtaksdato(LocalDate.now()).build();
        } else {
            behandlingsresultat = opprettBehandlingsResultat(behandling, behandlingResultatType);
            behandlingVedtak = BehandlingVedtak.builder()
                .medAnsvarligSaksbehandler(finnSaksBehandler(behandling))
                .medBehandlingsresultat(behandlingsresultat)
                .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
                .medVedtaksdato(LocalDate.now()).build();
        }
        behandlingVedtakRepository.lagre(behandlingVedtak);
    }

    private Behandlingsresultat opprettBehandlingsResultat(Behandling behandling, BehandlingResultatType behandlingResultatType){
        Behandlingsresultat behandlingsresultat =Behandlingsresultat.builder().medBehandling(behandling)
            .medBehandlingResultatType(behandlingResultatType).build();
        behandlingresultatRepository.lagre(behandlingsresultat);
        return behandlingsresultat;
    }

    private Behandlingsresultat oppdaterBehandlingsResultat(Behandling behandling, Behandlingsresultat eksisterende,
                                                            BehandlingResultatType behandlingResultatType){
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderEndreEksisterende(eksisterende)
            .medBehandling(behandling)
            .medBehandlingResultatType(behandlingResultatType)
            .build();
        behandlingresultatRepository.lagre(behandlingsresultat);
        return behandlingsresultat;
    }

    private static String finnSaksBehandler(Behandling behandling) {
        if (behandling.getAnsvarligBeslutter() != null && !behandling.getAnsvarligBeslutter().isBlank()) {
            return behandling.getAnsvarligBeslutter();
        } else if (behandling.getAnsvarligSaksbehandler() != null && !behandling.getAnsvarligSaksbehandler().isBlank()) {
            return behandling.getAnsvarligSaksbehandler();
        }
        return DEFAULT_ANSVARLIG_SAKSBEHANDLER;
    }

    private void lagHistorikksinnslagForAutomatiskSaksbehandling(Behandling behandling) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.VEDTAK_FATTET_AUTOMATISK);
        historikkinnslag.setBehandling(behandling);
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);

        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();

        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandling.getId());
        tekstBuilder.medSkjermlenke(SkjermlenkeType.VEDTAK)
            .medResultat(beregningResultat.getVedtakResultatType())
            .medHendelse(HistorikkinnslagType.VEDTAK_FATTET_AUTOMATISK)
            .build(historikkinnslag);

        historikkTjenesteAdapter.lagInnslag(historikkinnslag);
    }

}
