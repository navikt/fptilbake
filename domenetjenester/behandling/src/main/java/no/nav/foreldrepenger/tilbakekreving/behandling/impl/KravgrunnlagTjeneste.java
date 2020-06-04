package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingUtil.sjekkAvvikHvisSisteDagIHelgen;
import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil.beregnBelop;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_FEILUTBETALING;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Days;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

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
                                ProsessTaskRepository prosessTaskRepository,
                                GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                SlettGrunnlagEventPubliserer slettGrunnlagEventPubliserer) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;

        this.kravgrunnlagEventPubliserer = slettGrunnlagEventPubliserer;
    }


    /**
     * WARNING denne metoden returerer ikke komplette perioder, periodene inneholder eks. ikke YTEL-posteringer
     * <p>
     * Dette kan være overraskende.
     */
    public List<KravgrunnlagPeriode432> finnKravgrunnlagPerioderMedFeilutbetaltPosteringer(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        return finnKravgrunnlagPerioderMedFeilutbetaltPosteringer(kravgrunnlag.getPerioder());
    }

    /**
     * WARNING denne metoden returerer ikke komplette perioder, periodene inneholder eks. ikke YTEL-posteringer
     */
    private List<KravgrunnlagPeriode432> finnKravgrunnlagPerioderMedFeilutbetaltPosteringer(List<KravgrunnlagPeriode432> allePerioder) {
        List<KravgrunnlagPeriode432> feilutbetaltPerioder = new ArrayList<>();
        for (KravgrunnlagPeriode432 kravgrunnlagPeriode432 : allePerioder) {
            List<KravgrunnlagBelop433> posteringer = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
                .filter(belop433 -> belop433.getKlasseType().equals(KlasseType.FEIL)).collect(Collectors.toList());
            if (!posteringer.isEmpty()) {
                kravgrunnlagPeriode432.setKravgrunnlagBeloper433(posteringer);
                feilutbetaltPerioder.add(kravgrunnlagPeriode432);
            }
        }
        return feilutbetaltPerioder;
    }

    public List<UtbetaltPeriode> utledLogiskPeriode(List<KravgrunnlagPeriode432> feilutbetaltPerioder) {
        LocalDate førsteDag = null;
        LocalDate sisteDag = null;
        BigDecimal belopPerPeriode = BigDecimal.ZERO;
        feilutbetaltPerioder.sort(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        List<UtbetaltPeriode> beregnetPerioider = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : feilutbetaltPerioder) {
            // for første gang
            Periode periode = kgPeriode.getPeriode();
            if (førsteDag == null && sisteDag == null) {
                førsteDag = periode.getFom();
                sisteDag = periode.getTom();
            } else {
                // beregn forskjellen mellom to perioder
                int antallDager = Days.between(sisteDag, periode.getFom()).getAmount();
                // hvis forskjellen er mer enn 1 dager eller siste dag er i helgen
                if (antallDager > 1 && sjekkAvvikHvisSisteDagIHelgen(sisteDag, antallDager)) {
                    // lag ny perioder hvis forskjellen er mer enn 1 dager
                    beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
                    førsteDag = periode.getFom();
                    belopPerPeriode = BigDecimal.ZERO;
                }
                sisteDag = periode.getTom();
            }
            belopPerPeriode = belopPerPeriode.add(beregnBelop(kgPeriode.getKravgrunnlagBeloper433()));
        }
        //FIXME sammenligningen er ikke riktig implementert, må bruke compareTo != 0
        if (belopPerPeriode != BigDecimal.ZERO) {
            beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
        }
        return beregnetPerioider;
    }

    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431, boolean kravgrunnlagetErGyldig) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (KravStatusKode.ENDRET.equals(kravgrunnlag431.getKravStatusKode())) {
            //TODO KravgrunnlagTjeneste bør ikke være ansvarlig for å bytte steg/sette på vent. Bør heller ha en tjeneste/observer som lytter og flytter til riktig steg/på vent.
            logger.info("Mottok endret kravgrunnlag for behandlingId={}", behandlingId);
            boolean erIFaktaSteg = FAKTA_FEILUTBETALING.equals(behandling.getAktivtBehandlingSteg());
            boolean erForbiFaktaSteg = behandlingskontrollTjeneste.erStegPassert(behandling, FAKTA_FEILUTBETALING);
            boolean erFørFaktaSteg = !erIFaktaSteg && !erForbiFaktaSteg;
            //forutsatt at FPTILBAKE allrede har fått SPER melding for den behandlingen og sett behandling på vent med VenteÅrsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
            if (erForbiFaktaSteg && kravgrunnlagetErGyldig) {
                logger.info("Hopper tilbake til {} pga endret kravgrunnlag for behandlingId={}", FAKTA_FEILUTBETALING.getKode(), behandlingId);
                BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
                behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
                behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, FAKTA_FEILUTBETALING);
            }
            if (!kravgrunnlagetErGyldig && erFørFaktaSteg) {
                logger.info("Setter behandling på vent pga kravgrunnlag endret til et ugyldig kravgrunnlag for behandlingId={}", behandlingId);
                behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, LocalDateTime.now().plusDays(7), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
            }
            fyrKravgrunnlagEndretEvent(behandlingId);
        }
        kravgrunnlagRepository.lagre(behandlingId, kravgrunnlag431);
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
    }

    private void fyrKravgrunnlagEndretEvent(Long behandlingId) {
        logger.info("Sletter gammel grunnlag data for behandlingId={}", behandlingId);
        kravgrunnlagEventPubliserer.fireKravgrunnlagEndretEvent(behandlingId);
    }

}
