package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegProsesseringResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegTilstandEndringEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;


/**
 * Visitor for å traversere behandlingssteg.
 * <p>
 * Thread-safety: Bør opprettes for hver traversering.
 */
class BehandlingStegVisitor {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(BehandlingStegVisitor.class);

    private final BehandlingRepository behandlingRepository;
    private final BehandlingskontrollTjeneste kontrollTjeneste;
    private final BehandlingskontrollKontekst kontekst;
    private final BehandlingModell behandlingModell;
    private final BehandlingStegKonfigurasjon behandlingStegKonfigurasjon;

    private final BehandlingskontrollEventPubliserer eventPubliserer;

    private final Behandling behandling;

    private final InternalManipulerBehandlingImpl manipulerInternBehandling;

    private final AksjonspunktRepository aksjonspunktRepository;

    private BehandlingStegModell stegModell;

    BehandlingStegVisitor(BehandlingRepositoryProvider repositoryProvider, Behandling behandling,
                          BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                          BehandlingStegModell stegModell, BehandlingskontrollKontekst kontekst,
                          BehandlingskontrollEventPubliserer eventPubliserer) {

        this.stegModell = Objects.requireNonNull(stegModell, "BehandlingStegModell");
        this.behandling = Objects.requireNonNull(behandling, "behandling");
        this.behandlingModell = Objects.requireNonNull(stegModell.getBehandlingModell(), "BehandlingModell");
        this.kontekst = Objects.requireNonNull(kontekst, "kontekst");
        this.kontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

        this.manipulerInternBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);

        if (eventPubliserer != null) {
            this.eventPubliserer = eventPubliserer;
        } else {
            this.eventPubliserer = BehandlingskontrollEventPubliserer.NULL_EVENT_PUB;
        }
        behandlingStegKonfigurasjon = behandlingskontrollTjeneste.getBehandlingStegKonfigurasjon();

    }

    protected BehandlingStegModell getStegModell() {
        return stegModell;
    }

    protected BehandlingStegProsesseringResultat prosesser() {
        BehandlingStegProsesseringResultat resultat = prosesserSteg(stegModell, false);
        return resultat;
    }

    protected BehandlingStegProsesseringResultat gjenoppta() {
        BehandlingStegProsesseringResultat resultat = prosesserSteg(stegModell, true);
        return resultat;
    }

    private BehandlingStegProsesseringResultat prosesserSteg(BehandlingStegModell stegModell, boolean gjenoppta) {
        BehandlingSteg steg = stegModell.getSteg();
        BehandlingStegType stegType = stegModell.getBehandlingStegType();

        // Sett riktig status for steget før det utføres
        BehandlingStegStatus førStegStatus = behandling.getBehandlingStegStatus();
        // Vanlig prosessering skal ikke gjennomføres hvis steget VENTER.
        if (!gjenoppta && BehandlingStegStatus.VENTER.equals(førStegStatus)) {
            return BehandlingStegProsesseringResultat.medMuligTransisjon(førStegStatus, BehandleStegResultat.settPåVent().getTransisjon());
        }
        BehandlingStegStatus førsteStegStatus = utledStegStatusFørUtføring(stegModell);
        oppdaterBehandlingStegStatus(behandling, stegType, førStegStatus, førsteStegStatus);

        // Utfør steg hvis tillatt av stegets før-status. Utled stegets nye status.
        BehandlingStegProsesseringResultat stegResultat;
        List<Aksjonspunkt> funnetAksjonspunkter = new ArrayList<>();
        if (erIkkePåVent(behandling) && førsteStegStatus.kanUtføreSteg()) {
            BehandleStegResultat resultat;
            // Her kan man sjekke om tilstand venter og evt la gjenoppta-modus kalle vanlig utfør
            if (gjenoppta) {
                resultat = steg.gjenopptaSteg(kontekst);
            } else {
                resultat = steg.utførSteg(kontekst);
            }

            reaktiverInaktiveAksjonspunkter(resultat);

            funnetAksjonspunkter = opprettAksjonspunkter(resultat, stegType);
            BehandlingStegStatus nyStegStatus = håndterResultatAvSteg(stegModell, resultat, behandling);
            stegResultat = BehandlingStegProsesseringResultat.medMuligTransisjon(nyStegStatus, resultat.getTransisjon());
        } else if (BehandlingStegStatus.erVedUtgang(førsteStegStatus)) {
            BehandlingStegStatus nyStegStatus = utledUtgangStegStatus(stegModell.getBehandlingStegType());
            stegResultat = BehandlingStegProsesseringResultat.utenOverhopp(nyStegStatus);
        } else {
            stegResultat = BehandlingStegProsesseringResultat.utenOverhopp(førsteStegStatus);
        }

        avsluttSteg(stegType, førsteStegStatus, stegResultat, funnetAksjonspunkter);

        return stegResultat;
    }

    private void avsluttSteg(BehandlingStegType stegType, BehandlingStegStatus førsteStegStatus, BehandlingStegProsesseringResultat stegResultat,
                           List<Aksjonspunkt> funnetAksjonspunkter) {

        log.info("Avslutter steg={}, transisjon={} med aksjonspunkter={}", stegType, stegResultat, funnetAksjonspunkter.stream().map(a-> a.getAksjonspunktDefinisjon()).collect(Collectors.toList()));

        Optional<BehandlingStegTilstand> stegTilstandFør = behandling.getSisteBehandlingStegTilstand();

        // Sett riktig status for steget etter at det er utført. Lagre eventuelle endringer fra steg på behandling
        guardAlleÅpneAksjonspunkterHarDefinertVurderingspunkt();
        oppdaterBehandlingStegStatus(behandling, stegType, førsteStegStatus, stegResultat.getNyStegStatus());

        // Publiser transisjonsevent
        StegTransisjon transisjon = behandlingModell.finnTransisjon(stegResultat.getTransisjon());
        BehandlingStegType tilSteg = finnFremoverhoppSteg(stegType, transisjon);
        eventPubliserer.fireEvent(new BehandlingTransisjonEvent(kontekst, stegResultat.getTransisjon(), stegTilstandFør, tilSteg, transisjon.erFremoverhopp()));

        // Publiser event om endring i stegets tilstand
        BehandlingStegTilstandEndringEvent behandlingStegTilstandEndringEvent = new BehandlingStegTilstandEndringEvent(kontekst, stegTilstandFør);
        behandlingStegTilstandEndringEvent.setNyTilstand(behandling.getBehandlingStegTilstand());
        eventPubliserer.fireEvent(behandlingStegTilstandEndringEvent);

        // Publiser de funnede aksjonspunktene
        kontrollTjeneste.aksjonspunkterFunnet(kontekst, stegType, funnetAksjonspunkter);
    }

    private BehandlingStegType finnFremoverhoppSteg(BehandlingStegType stegType, StegTransisjon transisjon) {
        BehandlingStegType tilSteg = null;
        if (transisjon.erFremoverhopp()) {
            BehandlingStegModell fraStegModell = behandlingModell.finnSteg(stegType);
            BehandlingStegModell tilStegModell = transisjon.nesteSteg(fraStegModell);
            tilSteg = tilStegModell != null ? tilStegModell.getBehandlingStegType() : null;
        }
        return tilSteg;
    }

    void markerOvergangTilNyttSteg(Optional<BehandlingStegTilstand> forrige, BehandlingStegType stegType) {
        log.info("Markerer nytt steg som aktivt: {}", stegType);

        // Flytt aktivt steg til gjeldende steg hvis de ikke er like
        BehandlingStegStatus sluttStatusForAndreSteg = behandlingStegKonfigurasjon.getUtført();
        settBehandlingStegSomGjeldende(stegType, sluttStatusForAndreSteg);

        Optional<BehandlingStegTilstand> ny = behandling.getBehandlingStegTilstand();
        fyrEventBehandlingStegOvergang(forrige, ny);
    }

    private boolean erIkkePåVent(Behandling behandling) {
        return !behandling.isBehandlingPåVent();
    }

    private void fyrEventBehandlingStegOvergang(Optional<BehandlingStegTilstand> forrige, Optional<BehandlingStegTilstand> ny) {
        BehandlingStegOvergangEvent event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(behandlingModell, forrige,
            ny, kontekst, false);

        eventPubliserer.fireEvent(event);
    }

    private void fyrEventBehandlingStegTilbakeføring(Optional<BehandlingStegTilstand> forrige, Optional<BehandlingStegTilstand> ny) {
        boolean erOverstyring = false;
        BehandlingStegOvergangEvent event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(
            behandlingModell, forrige, ny, kontekst, erOverstyring);

        eventPubliserer.fireEvent(event);
    }

    private void oppdaterBehandlingStegStatus(Behandling behandling,
                                              BehandlingStegType stegType,
                                              BehandlingStegStatus førsteStegStatus,
                                              BehandlingStegStatus nyStegStatus) {
        Optional<BehandlingStegTilstand> behandlingStegTilstand = behandling.getBehandlingStegTilstand(stegType);
        if (behandlingStegTilstand.isPresent()) {
            if (erForskjellig(førsteStegStatus, nyStegStatus)) {
                manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, stegType, nyStegStatus);
                behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
                eventPubliserer.fireEvent(kontekst, stegType, førsteStegStatus, nyStegStatus);
            }
        }
    }

    private static boolean erForskjellig(BehandlingStegStatus førsteStegStatus, BehandlingStegStatus nyStegStatus) {
        return !Objects.equals(nyStegStatus, førsteStegStatus);
    }

    private BehandlingStegStatus utledStegStatusFørUtføring(BehandlingStegModell stegModell) {

        BehandlingStegStatus nåBehandlingStegStatus = behandling.getBehandlingStegStatus();

        BehandlingStegType stegType = stegModell.getBehandlingStegType();

        if (erForbiInngang(nåBehandlingStegStatus)) {
            // Hvis vi har kommet forbi INNGANG, så gå direkte videre til det gjeldende statusen
            return nåBehandlingStegStatus;
        } else {
            // Hvis status er null/UDEFINERT eller INNGANG så reberegner vi for å se om vi kan STARTE
            List<AksjonspunktDefinisjon> kriterier = stegType.getAksjonspunktDefinisjonerInngang();

            List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter(kriterier);

            BehandlingStegStatus nyStatus = !åpneAksjonspunkter.isEmpty() ? behandlingStegKonfigurasjon.getInngang()
                : behandlingStegKonfigurasjon.getStartet();

            return nyStatus;
        }
    }

    private boolean erForbiInngang(BehandlingStegStatus nåBehandlingStegStatus) {
        return nåBehandlingStegStatus != null && !Objects.equals(behandlingStegKonfigurasjon.getInngang(), nåBehandlingStegStatus);
    }

    /**
     * Returner ny status på pågående steg.
     */
    private BehandlingStegStatus håndterResultatAvSteg(BehandlingStegModell stegModell, BehandleStegResultat resultat, Behandling behandling) {

        TransisjonIdentifikator transisjonIdentifikator = resultat.getTransisjon();
        if (transisjonIdentifikator == null) {
            throw new IllegalArgumentException("Utvikler-feil: mangler transisjon");
        }

        StegTransisjon transisjon = behandlingModell.finnTransisjon(transisjonIdentifikator);

        if (FellesTransisjoner.TILBAKEFØRT_TIL_AKSJONSPUNKT.getId().equals(transisjon.getId())) {
            // tilbakefør til tidligere steg basert på hvilke aksjonspunkter er åpne.
            Optional<BehandlingStegTilstand> forrige = behandling.getSisteBehandlingStegTilstand();
            BehandlingStegStatus behandlingStegStatus = håndterTilbakeføringTilTidligereSteg(behandling, stegModell.getBehandlingStegType());
            fyrEventBehandlingStegTilbakeføring(forrige, behandling.getSisteBehandlingStegTilstand());
            return behandlingStegStatus;
        }

        if (FellesTransisjoner.HENLAGT.getId().equals(transisjon.getId())) {
            return behandlingStegKonfigurasjon.getAvbrutt();
        }
        if (transisjon.erFremoverhopp()) {
            return behandlingStegKonfigurasjon.mapTilStatus(BehandlingStegResultat.FREMOVERFØRT);
        }
        if (FellesTransisjoner.UTFØRT.getId().equals(transisjon.getId())) {
            return utledUtgangStegStatus(stegModell.getBehandlingStegType());
        }
        if (FellesTransisjoner.STARTET.getId().equals(transisjon.getId())) {
            return behandlingStegKonfigurasjon.getStartet();
        }
        if (FellesTransisjoner.SETT_PÅ_VENT.getId().equals(transisjon.getId())) {
            return behandlingStegKonfigurasjon.getVenter();
        }
        throw new IllegalArgumentException("Utvikler-feil: ikke-håndtert transisjon " + transisjon.getId());
    }

    private BehandlingStegStatus utledUtgangStegStatus(BehandlingStegType behandlingStegType) {
        BehandlingStegStatus nyStegStatus;
        if (harÅpneAksjonspunkter(behandling, behandlingStegType)) {
            nyStegStatus = behandlingStegKonfigurasjon.getUtgang();
        } else {
            nyStegStatus = behandlingStegKonfigurasjon.getUtført();
        }
        return nyStegStatus;
    }

    private boolean harÅpneAksjonspunkter(Behandling behandling, BehandlingStegType behandlingStegType) {
        List<AksjonspunktDefinisjon> kriterier = behandlingStegType.getAksjonspunktDefinisjonerUtgang();

        List<Aksjonspunkt> utgangsAksjonspunkter = behandling.getÅpneAksjonspunkter().stream()
            .filter(a -> kriterier.contains(a.getAksjonspunktDefinisjon()))
            .collect(Collectors.toList());

        boolean åpneAksjonspunkter = utgangsAksjonspunkter
            .stream()
            .anyMatch((a) -> a.erÅpentAksjonspunkt());

        return åpneAksjonspunkter;
    }

    private BehandlingStegStatus håndterTilbakeføringTilTidligereSteg(Behandling behandling, BehandlingStegType inneværendeBehandlingStegType) {
        BehandlingStegStatus tilbakeførtStegStatus = behandlingStegKonfigurasjon.mapTilStatus(BehandlingStegResultat.TILBAKEFØRT);
        BehandlingStegStatus inneværendeBehandlingStegStatus = behandling.getBehandlingStegStatus();

        List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();
        if (!åpneAksjonspunkter.isEmpty()) {
            List<String> aksjonspunkter = åpneAksjonspunkter.stream().map(a -> a.getAksjonspunktDefinisjon().getKode()).collect(Collectors.toList());
            BehandlingStegModell nesteBehandlingStegModell = behandlingModell.finnTidligsteStegForAksjonspunktDefinisjon(aksjonspunkter);
            Optional<BehandlingStegStatus> nesteStegStatus = behandlingModell.finnStegStatusFor(nesteBehandlingStegModell.getBehandlingStegType(), aksjonspunkter);

            // oppdater inneværende steg
            oppdaterBehandlingStegStatus(behandling, inneværendeBehandlingStegType, inneværendeBehandlingStegStatus, tilbakeførtStegStatus);

            // oppdater nytt steg
            BehandlingStegType nesteStegtype = nesteBehandlingStegModell.getBehandlingStegType();
            oppdaterBehandlingStegType(nesteStegtype, nesteStegStatus.isPresent() ? nesteStegStatus.get() : null, tilbakeførtStegStatus);
        }
        return tilbakeførtStegStatus;
    }


    private void reaktiverInaktiveAksjonspunkter(BehandleStegResultat stegResultat) {
        List<AksjonspunktDefinisjon> apFraSteg = stegResultat.getAksjonspunktListe();
        behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(ap -> !ap.erAktivt())
            .filter(ap -> apFraSteg.contains(ap.getAksjonspunktDefinisjon()))
            .forEach(ap -> aksjonspunktRepository.reaktiver(ap));
    }

    /**
     * Lagrer nye aksjonspunkt, og gjenåpner dem hvis de alleerede står til avbrutt/utført
     */
    private List<Aksjonspunkt> opprettAksjonspunkter(BehandleStegResultat stegResultat, BehandlingStegType behandlingStegType) {
        List<AksjonspunktResultat> nyeApResultater = stegResultat.getAksjonspunktResultater();

        if (!nyeApResultater.isEmpty()) {
            List<Aksjonspunkt> funnetAksjonspunkter = new ArrayList<>();
            fjernGjensidigEkskluderendeAksjonspunkter(nyeApResultater);
            funnetAksjonspunkter.addAll(leggTilNyeAksjonspunkterPåBehandling(behandlingStegType, nyeApResultater, behandling));
            funnetAksjonspunkter.addAll(reåpneAvbrutteOgUtførteAksjonspunkter(nyeApResultater, behandling));
            return funnetAksjonspunkter;
        } else {
            return new ArrayList<>();
        }
    }

    private void fjernGjensidigEkskluderendeAksjonspunkter(List<AksjonspunktResultat> nyeApResultater) {
        Set<AksjonspunktDefinisjon> nyeApDef = nyeApResultater.stream().map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toSet());
        List<AksjonspunktDefinisjon> utelukkedeAksjonspunkter = behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().getUtelukkendeApdef().stream().anyMatch(nyeApDef::contains))
            .map(Aksjonspunkt::getAksjonspunktDefinisjon)
            .collect(Collectors.toList());
        // Dersom eksisterende aksjonspunkter på behandling er utelukket av de nye, så må de fjernes
        utelukkedeAksjonspunkter.forEach(utelukketApDef -> aksjonspunktRepository.fjernAksjonspunkt(behandling, utelukketApDef));
    }

    private List<Aksjonspunkt> reåpneAvbrutteOgUtførteAksjonspunkter(List<AksjonspunktResultat> nyeDefinisjoner,
                                                                     Behandling behandling) {

        Map<AksjonspunktDefinisjon, AksjonspunktResultat> aksjonspunktResultatMap = nyeDefinisjoner.stream()
            .collect(Collectors.toMap(AksjonspunktResultat::getAksjonspunktDefinisjon, Function.identity()));

        Set<Aksjonspunkt> skalReåpnes = behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.erUtført() || ap.erAvbrutt())
            .filter(ap -> aksjonspunktResultatMap.get(ap.getAksjonspunktDefinisjon()) != null)
            .collect(Collectors.toSet());

        List<Aksjonspunkt> reåpnedeAksjonspunkter = new ArrayList<>();
        skalReåpnes.forEach((Aksjonspunkt ap) -> {
            aksjonspunktRepository.setReåpnet(ap);
            aksjonspunktResultatMap.get(ap.getAksjonspunktDefinisjon()).getAksjonspunktModifiserer().accept(ap);
            reåpnedeAksjonspunkter.add(ap);
        });

        return reåpnedeAksjonspunkter;
    }

    private List<Aksjonspunkt> leggTilNyeAksjonspunkterPåBehandling(BehandlingStegType behandlingStegType,
                                                                    List<AksjonspunktResultat> nyeDefinisjoner,
                                                                    Behandling behandling) {

        List<AksjonspunktDefinisjon> eksisterendeDefinisjoner = behandling.getAksjonspunkter().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon)
            .collect(Collectors.toList());

        List<AksjonspunktResultat> nyeAksjonspunkt = nyeDefinisjoner.stream()
            .filter(apDefWrapper -> !eksisterendeDefinisjoner.contains(apDefWrapper.getAksjonspunktDefinisjon()))
            .collect(Collectors.toList());

        return leggTilAksjonspunkt(behandlingStegType, behandling, nyeAksjonspunkt);
    }

    private List<Aksjonspunkt> leggTilAksjonspunkt(BehandlingStegType behandlingStegType, Behandling behandling,
                                                   List<AksjonspunktResultat> nyeAksjonspunkt) {

        List<Aksjonspunkt> aksjonspunkter = new ArrayList<>();
        nyeAksjonspunkt.forEach((AksjonspunktResultat apResultat) -> {

            Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apResultat.getAksjonspunktDefinisjon(),
                behandlingStegType);
            apResultat.getAksjonspunktModifiserer().accept(aksjonspunkt);
            aksjonspunkter.add(aksjonspunkt);
        });
        return aksjonspunkter;
    }

    private void oppdaterBehandlingStegType(BehandlingStegType nesteStegType, BehandlingStegStatus nesteStegStatus, BehandlingStegStatus sluttStegStatusVedOvergang) {
        Objects.requireNonNull(behandlingRepository, "behandlingRepository");

        BehandlingStegType siste = behandling.getSisteBehandlingStegTilstand().map(BehandlingStegTilstand::getBehandlingSteg).orElse(null);

        if (!erSammeStegSomFør(nesteStegType, siste)) {

            // sett status for neste steg
            manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, nesteStegType, nesteStegStatus, sluttStegStatusVedOvergang);
        }
    }

    protected void settBehandlingStegSomGjeldende(BehandlingStegType nesteStegType, BehandlingStegStatus sluttStegStatusVedOvergang) {
        oppdaterBehandlingStegType(nesteStegType, null, sluttStegStatusVedOvergang);
    }

    private boolean erSammeStegSomFør(BehandlingStegType stegType, BehandlingStegType nåværendeBehandlingSteg) {
        return Objects.equals(nåværendeBehandlingSteg, stegType);
    }

    /**
     * TODO (FC: Trengs denne lenger? Aksjonspunkt har en not-null relasjon til Vurderingspunkt.
     * <p>
     * Verifiser at alle åpne aksjonspunkter har et definert vurderingspunkt i gjenværende steg hvor de må behandles.
     * Sikrer at ikke abstraktpunkt identifiseres ETTER at de skal være håndtert.
     */
    private void guardAlleÅpneAksjonspunkterHarDefinertVurderingspunkt() {
        BehandlingStegType aktivtBehandlingSteg = behandling.getAktivtBehandlingSteg();

        List<Aksjonspunkt> gjenværendeÅpneAksjonspunkt = new ArrayList<>(behandling.getÅpneAksjonspunkter());

        // TODO (FC): Denne bør håndteres med event ved overgang
        behandlingModell.hvertStegFraOgMed(aktivtBehandlingSteg)
            .forEach(bsm -> {
                filterVekkAksjonspunktHåndtertAvFremtidigVurderingspunkt(bsm, gjenværendeÅpneAksjonspunkt);
            });

        if (!gjenværendeÅpneAksjonspunkt.isEmpty()) {
            /*
             * TODO (FC): Lagre og sett behandling på vent i stedet for å kaste exception? Exception mest nyttig i test
             * og
             * utvikling, men i prod bør heller sette behandling til side hvis det skulle være så galt at
             * vurderingspunkt ikke er definert for et identifisert abstraktpunkt.
             */
            throw new IllegalStateException(
                "Utvikler-feil: Det er definert aksjonspunkt [" + //$NON-NLS-1$
                    Aksjonspunkt.getKoder(gjenværendeÅpneAksjonspunkt)
                    + "] som ikke er håndtert av noe steg" //$NON-NLS-1$
                    + (aktivtBehandlingSteg == null ? " i sekvensen " : " fra og med: " + aktivtBehandlingSteg)); //$NON-NLS-1$
        }
    }

    private void filterVekkAksjonspunktHåndtertAvFremtidigVurderingspunkt(BehandlingStegModell bsm, List<Aksjonspunkt> åpneAksjonspunkter) {
        BehandlingStegType stegType = bsm.getBehandlingStegType();
        List<AksjonspunktDefinisjon> inngangKriterier = stegType.getAksjonspunktDefinisjonerInngang();
        List<AksjonspunktDefinisjon> utgangKriterier = stegType.getAksjonspunktDefinisjonerUtgang();
        åpneAksjonspunkter.removeIf(elem -> {
            AksjonspunktDefinisjon elemAksDef = elem.getAksjonspunktDefinisjon();
            return elem.erÅpentAksjonspunkt() && (inngangKriterier.contains(elemAksDef) || utgangKriterier.contains(elemAksDef));
        });
    }

}
