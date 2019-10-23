package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.GrunnlagSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

@BehandlingStegRef(kode = "TBKGSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class MottattGrunnlagStegImpl implements GrunnlagSteg {

    private static final Logger log = LoggerFactory.getLogger(MottattGrunnlagStegImpl.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private Period ventefrist;

    public MottattGrunnlagStegImpl() {
        // CDI
    }

    @Inject
    public MottattGrunnlagStegImpl(BehandlingRepository behandlingRepository,
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
        LocalDateTime fristTid = FPDateUtil.nå().plus(ventefrist);
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
            /* Hvis fristen har gått ut, og grunnlag fra økonomi ikke har blitt mottatt, logger vi en feil med level ERROR.
             * Dersom en exception kastes, så vil ikke ny settPåVent status bli persistert, og steget blir satt (feilaktig) som utført.
             * Hvis denne meldingen logges, må det kontrolleres at oppdragssystemet (OS) er oppe, tilgjengelig for fptilbake, og at OS ikke har feil.
             */
            String saksnummer = behandlingRepository.hentSaksnummerForBehandling(behandling.getId());
            log.error("Grunnlag fra Økonomi har ikke blitt mottatt innen fristen: %s, behandlingen kan ikke fortsette uten grunnlaget." +
                    " Kontroller at økonomisystemet er tilgjengelig og har grunnlag for behandling med saksnummer: %s . [ behandlingId: %s ]",
                fristTid, saksnummer, behandling.getId());
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
        return fristTid != null && FPDateUtil.nå().toLocalDate().isAfter(fristTid.toLocalDate());
    }

}
