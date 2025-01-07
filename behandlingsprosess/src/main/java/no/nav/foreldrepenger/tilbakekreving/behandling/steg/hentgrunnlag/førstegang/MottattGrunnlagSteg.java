package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.AutomatiskSaksbehandlingVurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;

@BehandlingStegRef(BehandlingStegType.TBKGSTEG)
@BehandlingTypeRef
@ApplicationScoped
public class MottattGrunnlagSteg implements BehandlingSteg {

    private static final Logger LOG = LoggerFactory.getLogger(MottattGrunnlagSteg.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste;

    public MottattGrunnlagSteg() {
        // CDI
    }

    @Inject
    public MottattGrunnlagSteg(BehandlingRepository behandlingRepository,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                               AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.halvtRettsgebyrTjeneste = halvtRettsgebyrTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        var fristTid = kanFortsetteEtter(kontekst.getBehandlingId(), LocalDateTime.now().plus(Frister.KRAVGRUNNLAG_FØRSTE));

        if (fristTid.isBefore(LocalDateTime.now())) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }

        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                BehandlingStegType.TBKGSTEG, fristTid, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        return BehandleStegResultat.settPåVent();
    }

    @Override
    public BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        var fristTid = kanFortsetteEtter(kontekst.getBehandlingId(), hentFrist(behandling));
        if (fristTid.isBefore(LocalDateTime.now())) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }

        if (gåttOverFristen(fristTid)) {
            LOG.error("MottaGrunnlagSteg: Behandling {} gjenopptatt, over frist, uten kravgrunnlag. Skal ikke lenger forekomme", behandling.getId());
        }
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG,
            fristTid, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        return BehandleStegResultat.settPåVent();
    }

    private LocalDateTime hentFrist(Behandling behandling) {
        return behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)
            .map(Aksjonspunkt::getFristTid).orElseGet(() -> LocalDateTime.now().plus(Frister.KRAVGRUNNLAG_FØRSTE));
    }

    private boolean gåttOverFristen(LocalDateTime fristTid) {
        return fristTid != null && LocalDate.now().isAfter(fristTid.toLocalDate());
    }

    private LocalDateTime kanFortsetteEtter(Behandling behandling, LocalDateTime gjeldendeFrist) {
        if (gjenopptaBehandlingTjeneste.kanGjenopptaSteg(behandling.getId())) {
            // Sørg for at de under halvt rettegebyr blir liggende til de kan behandles automatisk uten å ha aktivt aksjonspunkt i fakta mer enn en halv time.
            if (halvtRettsgebyrTjeneste.lavFeilutbetalingKanVentePåAutomatiskBehandling(behandling)) {
                var fristFraGrunnlag = halvtRettsgebyrTjeneste.ventefristForTilfelleSomKanAutomatiskSaksbehandles(behandling.getId());
                return gjeldendeFrist == null || fristFraGrunnlag.isAfter(gjeldendeFrist) ? fristFraGrunnlag : gjeldendeFrist;
            } else {
                return LocalDateTime.now().minusHours(1);
            }
        }
        return gjeldendeFrist;
    }

}
