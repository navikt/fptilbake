package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class KravgrunnlagTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(KravgrunnlagTjeneste.class);

    private KravgrunnlagRepository kravgrunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private SlettGrunnlagEventPubliserer kravgrunnlagEventPubliserer;


    KravgrunnlagTjeneste() {
        // For CDI
    }

    @Inject
    public KravgrunnlagTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                SlettGrunnlagEventPubliserer slettGrunnlagEventPubliserer) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;

        this.kravgrunnlagEventPubliserer = slettGrunnlagEventPubliserer;
    }


    public List<UtbetaltPeriode> utledLogiskPeriode(Long behandlingId) {
        return LogiskPeriodeTjeneste.utledLogiskPeriode(finnFeilutbetalingPrPeriode(behandlingId));
    }

    private SortedMap<Periode, BigDecimal> finnFeilutbetalingPrPeriode(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        SortedMap<Periode, BigDecimal> feilutbetalingPrPeriode = new TreeMap<>(Periode.COMPARATOR);
        for (KravgrunnlagPeriode432 kravgrunnlagPeriode432 : kravgrunnlag.getPerioder()) {
            BigDecimal feilutbetalt = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
                .filter(beløp433 -> beløp433.getKlasseType() == KlasseType.FEIL)
                .map(KravgrunnlagBelop433::getNyBelop)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

            if (feilutbetalt.compareTo(BigDecimal.ZERO) != 0) {
                feilutbetalingPrPeriode.put(kravgrunnlagPeriode432.getPeriode(), feilutbetalt);
            }
        }
        return feilutbetalingPrPeriode;
    }


    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (KravStatusKode.ENDRET.equals(kravgrunnlag431.getKravStatusKode())) {
            //TODO KravgrunnlagTjeneste bør ikke være ansvarlig for å bytte steg/sette på vent. Bør heller ha en tjeneste/observer som lytter og flytter til riktig steg/på vent.
            håndterEndretkravgrunnlagFraØkonomi(behandling, kravgrunnlag431, kravgrunnlagetErGyldig);
        } else {
            kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
            gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
        }
    }

    private void håndterEndretkravgrunnlagFraØkonomi(Behandling behandling, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        long behandlingId = behandling.getId();
        logger.info("Mottok endret kravgrunnlag for behandlingId={}", behandlingId);
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        if (!kravgrunnlagetErGyldig) {
            logger.info("Setter behandling på vent pga kravgrunnlag endret til et ugyldig kravgrunnlag for behandlingId={}", behandlingId);
            behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, LocalDateTime.now().plusDays(7), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        } else {
            boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
            //forutsatt at FPTILBAKE allrede har fått SPER melding for den behandlingen og sett behandling på vent med VenteÅrsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
            if (erForbiFaktaSteg) {
                logger.info("Hopper tilbake til {} pga endret kravgrunnlag for behandlingId={}", FAKTA_FEILUTBETALING.getKode(), behandlingId);
                BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
                behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
                behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, FAKTA_FEILUTBETALING);
            }
            fyrKravgrunnlagEndretEvent(behandlingId);
            gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
        }
    }

    private void fyrKravgrunnlagEndretEvent(Long behandlingId) {
        logger.info("Sletter gammel grunnlag data for behandlingId={}", behandlingId);
        kravgrunnlagEventPubliserer.fireKravgrunnlagEndretEvent(behandlingId);
    }

}
