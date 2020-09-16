package no.nav.foreldrepenger.tilbakekreving.behandling.steg.fattevedtak;

import static java.lang.Boolean.TRUE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.vedtak.util.StringUtils;

@BehandlingStegRef(kode = "FVEDSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class FatteVedtakStegImpl implements FatteVedtakSteg {

    private static final String DEFAULT_ANSVARLIG_SAKSBEHANDLER = "VL";

    private TotrinnRepository totrinnRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private TilbakekrevingBeregningTjeneste beregningTjeneste;


    FatteVedtakStegImpl() {
        // for CDI proxy
    }

    @Inject
    public FatteVedtakStegImpl(BehandlingRepositoryProvider repositoryProvider, TotrinnRepository totrinnRepository, TilbakekrevingBeregningTjeneste beregningTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.totrinnRepository = totrinnRepository;
        this.behandlingresultatRepository = repositoryProvider.getBehandlingresultatRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.beregningTjeneste = beregningTjeneste;
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
            opprettBehandlingVedtak(behandling, BehandlingResultatType.FASTSATT);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
    }

    private boolean sendesTilbakeTilSaksbehandler(Collection<Totrinnsvurdering> totrinnsvurderinger) {
        return totrinnsvurderinger.stream()
            .anyMatch(totrinnsvurdering -> !TRUE.equals(totrinnsvurdering.isGodkjent()));
    }

    private void opprettBehandlingVedtak(Behandling behandling, BehandlingResultatType behandlingResultatType) {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandling(behandling)
            .medBehandlingResultatType(behandlingResultatType).build();
        behandlingresultatRepository.lagre(behandlingsresultat);
        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandling.getId());

        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler(finnSaksBehandler(behandling))
            .medBehandlingsresultat(behandlingsresultat)
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .medVedtaksdato(LocalDate.now())
            .medVedtakResultat(beregningResultat.getVedtakResultatType()).build();

        behandlingVedtakRepository.lagre(behandlingVedtak);
    }

    private static String finnSaksBehandler(Behandling behandling) {
        if (!StringUtils.isBlank(behandling.getAnsvarligBeslutter())) {
            return behandling.getAnsvarligBeslutter();
        } else if (!StringUtils.isBlank(behandling.getAnsvarligSaksbehandler())) {
            return behandling.getAnsvarligSaksbehandler();
        }
        return DEFAULT_ANSVARLIG_SAKSBEHANDLER;
    }

}
