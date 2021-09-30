package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingManglerKravgrunnlagFristenUtløptEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;

@BehandlingStegRef(kode = "TBKGSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class MottattGrunnlagSteg implements BehandlingSteg {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingManglerKravgrunnlagFristenUtløptEventPubliserer utløptEventPubliserer;
    private Period ventefrist;

    public MottattGrunnlagSteg() {
        // CDI
    }

    @Inject
    public MottattGrunnlagSteg(BehandlingRepository behandlingRepository,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                               BehandlingManglerKravgrunnlagFristenUtløptEventPubliserer utløptEventPubliserer,
                               @KonfigVerdi(value = "frist.grunnlag.tbkg") Period ventefrist) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.utløptEventPubliserer = utløptEventPubliserer;
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
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG,
            fristTid, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        if (gåttOverFristen(fristTid)) {
            /* Hvis fristen har gått ut, og grunnlag fra økonomi ikke har blitt mottatt, publiserer BehandlingFristenUtløptEvent for å sende data til FPLOS .
             * Etter hvert kan saksbehandler se oppgaven i fplos.Saksbehandler kan åpne oppgaven som åpner behandling på vent med mer informasjon.
             */
            utløptEventPubliserer.fireEvent(behandling, fristTid);
        }
        return BehandleStegResultat.settPåVent();
    }

    private LocalDateTime hentFrist(Behandling behandling) {
        Set<Aksjonspunkt> aksjonspunkter = behandling.getAksjonspunkter();
        return aksjonspunkter.stream()
            .filter(o -> AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(o.getAksjonspunktDefinisjon()))
            .map(Aksjonspunkt::getFristTid)
            .findFirst().orElse(null);
    }

    private boolean gåttOverFristen(LocalDateTime fristTid) {
        return fristTid != null && LocalDateTime.now().toLocalDate().isAfter(fristTid.toLocalDate());
    }

}
