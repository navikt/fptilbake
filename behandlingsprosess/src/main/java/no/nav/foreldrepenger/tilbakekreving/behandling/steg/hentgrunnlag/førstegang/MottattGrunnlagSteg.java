package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
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

@BehandlingStegRef(BehandlingStegType.TBKGSTEG)
@BehandlingTypeRef
@ApplicationScoped
public class MottattGrunnlagSteg implements BehandlingSteg {

    private static final Logger LOG = LoggerFactory.getLogger(MottattGrunnlagSteg.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private Period ventefrist;

    public MottattGrunnlagSteg() {
        // CDI
    }

    @Inject
    public MottattGrunnlagSteg(BehandlingRepository behandlingRepository,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                               @KonfigVerdi(value = "frist.grunnlag.tbkg") Period ventefrist) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.ventefrist = ventefrist;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        if (gjenopptaBehandlingTjeneste.kanGjenopptaSteg(kontekst.getBehandlingId())) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        LocalDateTime fristTid = LocalDateTime.now().plus(ventefrist);
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                BehandlingStegType.TBKGSTEG, fristTid, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        return BehandleStegResultat.settPåVent();
    }

    @Override
    public BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        if (gjenopptaBehandlingTjeneste.kanGjenopptaSteg(behandling.getId())) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }

        LocalDateTime fristTid = hentFrist(behandling);
        if (gåttOverFristen(fristTid)) {
            LOG.error("MottaGrunnlagSteg: Behandling {} gjenopptatt, over frist, uten kravgrunnlag. Skal ikke lenger forekomme", behandling.getId());
        }
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG,
            fristTid, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        return BehandleStegResultat.settPåVent();
    }

    private LocalDateTime hentFrist(Behandling behandling) {
        return behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)
            .map(Aksjonspunkt::getFristTid).orElse(null);
    }

    private boolean gåttOverFristen(LocalDateTime fristTid) {
        return fristTid != null && LocalDate.now().isAfter(fristTid.toLocalDate());
    }

}
