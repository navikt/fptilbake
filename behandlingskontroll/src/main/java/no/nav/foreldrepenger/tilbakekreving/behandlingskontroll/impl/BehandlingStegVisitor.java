package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegTilstandSnapshot;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.StegProsesseringResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKontrollRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;


/**
 * Visitor for å traversere ett (1) behandlingssteg.
 */
class BehandlingStegVisitor {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BehandlingStegVisitor.class);

    private final BehandlingRepository behandlingRepository;
    private final BehandlingskontrollKontekst kontekst;
    private final BehandlingModell behandlingModell;
    private final BehandlingskontrollEventPubliserer eventPubliserer;

    private final Behandling behandling;

    private final AksjonspunktKontrollRepository aksjonspunktKontrollRepository;

    private final BehandlingStegModell stegModell;

    BehandlingStegVisitor(BehandlingskontrollServiceProvider serviceProvider, Behandling behandling,
                          BehandlingStegModell stegModell, BehandlingskontrollKontekst kontekst) {

        this.stegModell = Objects.requireNonNull(stegModell, "BehandlingStegModell");
        this.behandling = Objects.requireNonNull(behandling, "behandling");
        this.behandlingModell = Objects.requireNonNull(stegModell.getBehandlingModell(), "BehandlingModell");
        this.kontekst = Objects.requireNonNull(kontekst, "kontekst");
        this.behandlingRepository = serviceProvider.getBehandlingRepository();
        this.aksjonspunktKontrollRepository = serviceProvider.getAksjonspunktKontrollRepository();

        this.eventPubliserer = Objects.requireNonNullElse(serviceProvider.getEventPubliserer(), BehandlingskontrollEventPubliserer.NULL_EVENT_PUB);
    }

    private static boolean erForskjellig(BehandlingStegStatus førsteStegStatus, BehandlingStegStatus nyStegStatus) {
        return !Objects.equals(nyStegStatus, førsteStegStatus);
    }

    protected BehandlingStegModell getStegModell() {
        return stegModell;
    }

    protected StegProsesseringResultat prosesser() {
        return prosesserSteg(stegModell, false);
    }

    protected StegProsesseringResultat gjenoppta() {
        return prosesserSteg(stegModell, true);
    }

    private StegProsesseringResultat prosesserSteg(BehandlingStegModell stegModell, boolean gjenoppta) {
        BehandlingSteg steg = stegModell.getSteg();
        BehandlingStegType stegType = stegModell.getBehandlingStegType();
        var apHåndterer = new AksjonspunktResultatOppretter(aksjonspunktKontrollRepository, behandling);

        // Sett riktig status for steget før det utføres
        BehandlingStegStatus førStegStatus = behandling.getBehandlingStegStatus();
        LOG.info("Prosesserer steg={}, stegStatus={}", stegType, førStegStatus);
        // Vanlig prosessering skal ikke gjennomføres hvis steget VENTER.
        if (!gjenoppta && BehandlingStegStatus.VENTER.equals(førStegStatus)) {
            return StegProsesseringResultat.medMuligTransisjon(førStegStatus, BehandleStegResultat.settPåVent().getTransisjon());
        }
        var førsteStegStatus = utledStegStatusFørUtføring(stegModell);
        oppdaterBehandlingStegStatus(behandling, stegType, førStegStatus, førsteStegStatus);


        // Utfør steg hvis tillatt av stegets før-status. Utled stegets nye status.
        StegProsesseringResultat stegResultat;
        List<Aksjonspunkt> funnetAksjonspunkter = new ArrayList<>();
        if (erIkkePåVent(behandling) && førsteStegStatus.kanUtføreSteg()) {
            BehandleStegResultat resultat;
            // Her kan man sjekke om tilstand venter og evt la gjenoppta-modus kalle vanlig utfør
            if (gjenoppta) {
                resultat = steg.gjenopptaSteg(kontekst);
            } else {
                resultat = steg.utførSteg(kontekst);
            }

            funnetAksjonspunkter.addAll(apHåndterer.opprettAksjonspunkter(resultat.getAksjonspunktResultater(), stegType));
            var nyStegStatus = håndterResultatAvSteg(stegModell, resultat, behandling);
            stegResultat = StegProsesseringResultat.medMuligTransisjon(nyStegStatus, resultat.getTransisjon());
        } else if (BehandlingStegStatus.erVedUtgang(førsteStegStatus)) {
            BehandlingStegStatus nyStegStatus = utledUtgangStegStatus(stegModell.getBehandlingStegType());
            stegResultat = StegProsesseringResultat.utenOverhopp(nyStegStatus);
        } else {
            stegResultat = StegProsesseringResultat.utenOverhopp(førsteStegStatus);
        }

        avsluttSteg(stegType, førsteStegStatus, stegResultat, funnetAksjonspunkter);

        return stegResultat;
    }

    private void avsluttSteg(BehandlingStegType stegType, BehandlingStegStatus førsteStegStatus,
                             StegProsesseringResultat stegResultat, List<Aksjonspunkt> funnetAksjonspunkter) {

        LOG.info("Avslutter steg={}, transisjon={} og funnet aksjonspunkter={}", stegType, stegResultat,
                funnetAksjonspunkter.stream().map(Aksjonspunkt::getAksjonspunktDefinisjon).toList());

        Optional<BehandlingStegTilstand> stegTilstandFør = behandling.getSisteBehandlingStegTilstand();

        // Sett riktig status for steget etter at det er utført. Lagre eventuelle endringer fra steg på behandling
        guardAlleÅpneAksjonspunkterHarDefinertVurderingspunkt();
        oppdaterBehandlingStegStatus(behandling, stegType, førsteStegStatus, stegResultat.getNyStegStatus());

        // Publiser transisjonsevent
        StegTransisjon transisjon = behandlingModell.finnTransisjon(stegResultat.getTransisjon());
        BehandlingStegType tilSteg = finnFremoverhoppSteg(stegType, transisjon);
        eventPubliserer.fireEvent(opprettEvent(stegResultat, transisjon, stegTilstandFør.orElse(null), tilSteg));

        // Publiser de funnede aksjonspunktene
        if (!funnetAksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunktStatusEvent(kontekst, funnetAksjonspunkter, stegType));
        }
    }

    private BehandlingTransisjonEvent opprettEvent(StegProsesseringResultat stegResultat, StegTransisjon transisjon,
                                                   BehandlingStegTilstand fraTilstand, BehandlingStegType tilSteg) {

        return new BehandlingTransisjonEvent(kontekst, stegResultat.getTransisjon(), fraTilstand, tilSteg,
                transisjon.getMålstegHvisHopp().isPresent());
    }

    private BehandlingStegType finnFremoverhoppSteg(BehandlingStegType stegType, StegTransisjon transisjon) {
        BehandlingStegType tilSteg = null;
        if (transisjon.getMålstegHvisHopp().isPresent()) {
            var fraStegModell = behandlingModell.finnSteg(stegType);
            var tilStegModell = transisjon.nesteSteg(fraStegModell);
            tilSteg = tilStegModell != null ? tilStegModell.getBehandlingStegType() : null;
        }
        return tilSteg;
    }

    void markerOvergangTilNyttSteg(BehandlingStegType stegType, BehandlingStegTilstandSnapshot forrigeTilstand) {
        LOG.info("Markerer nytt steg som aktivt: {}", stegType);

        // Flytt aktivt steg til gjeldende steg hvis de ikke er like
        var sluttStatusForAndreSteg = BehandlingStegKonfigurasjon.getUtført();
        oppdaterBehandlingStegType(forrigeTilstand, stegType, null, sluttStatusForAndreSteg);
    }

    private boolean erIkkePåVent(Behandling behandling) {
        return !behandling.isBehandlingPåVent();
    }

    private void oppdaterBehandlingStegStatus(Behandling behandling,
                                              BehandlingStegType stegType,
                                              BehandlingStegStatus førsteStegStatus,
                                              BehandlingStegStatus nyStegStatus) {
        var behandlingStegTilstand = behandling.getBehandlingStegTilstand(stegType);
        if (behandlingStegTilstand.isPresent()) {
            if (erForskjellig(førsteStegStatus, nyStegStatus)) {
                InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, stegType, nyStegStatus, BehandlingStegStatus.UTFØRT);
                behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
            }
        }
    }

    private BehandlingStegStatus utledStegStatusFørUtføring(BehandlingStegModell stegModell) {

        var nåBehandlingStegStatus = behandling.getBehandlingStegStatus();

        var stegType = stegModell.getBehandlingStegType();

        if (erForbiInngang(nåBehandlingStegStatus)) {
            // Hvis vi har kommet forbi INNGANG, så gå direkte videre til det gjeldende
            // statusen
            return nåBehandlingStegStatus;
        }
        var måHåndereAksjonspunktHer = behandling.getÅpneAksjonspunkter().stream()
                .anyMatch(ap -> skalHåndteresHer(stegType, ap, VurderingspunktType.INN));

        var nyStatus = måHåndereAksjonspunktHer ? BehandlingStegKonfigurasjon.getInngang()
                : BehandlingStegKonfigurasjon.getStartet();

        return nyStatus;
    }

    private boolean skalHåndteresHer(BehandlingStegType stegType, Aksjonspunkt ap, VurderingspunktType vurderingspunktType) {
        return ap.getAksjonspunktDefinisjon().getBehandlingSteg().equals(stegType)
                && ap.getAksjonspunktDefinisjon().getVurderingspunktType().equals(vurderingspunktType);
    }

    private boolean erForbiInngang(BehandlingStegStatus nåBehandlingStegStatus) {
        return (nåBehandlingStegStatus != null) && !Objects.equals(BehandlingStegKonfigurasjon.getInngang(), nåBehandlingStegStatus);
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
            return håndterTilbakeføringTilTidligereSteg(behandling, stegModell.getBehandlingStegType());
        }

        if (FellesTransisjoner.HENLAGT.getId().equals(transisjon.getId())) {
            return BehandlingStegKonfigurasjon.getAvbrutt();
        }
        if (transisjon.getMålstegHvisHopp().isPresent()) {
            return BehandlingStegKonfigurasjon.mapTilStatus(transisjon.getRetningForHopp());
        }
        if (FellesTransisjoner.UTFØRT.getId().equals(transisjon.getId())) {
            return utledUtgangStegStatus(stegModell.getBehandlingStegType());
        }
        if (FellesTransisjoner.STARTET.getId().equals(transisjon.getId())) {
            return BehandlingStegKonfigurasjon.getStartet();
        }
        if (FellesTransisjoner.SETT_PÅ_VENT.getId().equals(transisjon.getId())) {
            return BehandlingStegKonfigurasjon.getVenter();
        }
        throw new IllegalArgumentException("Utvikler-feil: ikke-håndtert transisjon " + transisjon.getId());
    }

    private BehandlingStegStatus utledUtgangStegStatus(BehandlingStegType behandlingStegType) {
        BehandlingStegStatus nyStegStatus;
        if (harÅpneAksjonspunkter(behandling, behandlingStegType)) {
            nyStegStatus = BehandlingStegKonfigurasjon.getUtgang();
        } else {
            nyStegStatus = BehandlingStegKonfigurasjon.getUtført();
        }
        return nyStegStatus;
    }

    private boolean harÅpneAksjonspunkter(Behandling behandling, BehandlingStegType behandlingStegType) {
        return behandling.getÅpneAksjonspunkter()
                .stream()
                .anyMatch(ap -> skalHåndteresHer(behandlingStegType, ap, VurderingspunktType.UT));
    }

    private BehandlingStegStatus håndterTilbakeføringTilTidligereSteg(Behandling behandling, BehandlingStegType inneværendeBehandlingStegType) {
        BehandlingStegStatus tilbakeførtStegStatus = BehandlingStegKonfigurasjon.mapTilStatus(BehandlingStegResultat.TILBAKEFØRT);

        List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();
        if (!åpneAksjonspunkter.isEmpty()) {
            // Eksisterende
            var inneværendeBehandlingStegStatus = behandling.getBehandlingStegStatus();
            var forrige = BehandlingModellImpl.tilBehandlingsStegSnapshot(behandling.getSisteBehandlingStegTilstand());

            var aksjonspunkter = åpneAksjonspunkter.stream().map(Aksjonspunkt::getAksjonspunktDefinisjon).toList();
            var nesteBehandlingStegModell = behandlingModell.finnTidligsteStegForAksjonspunktDefinisjon(aksjonspunkter);
            var nesteStegStatus = behandlingModell.finnStegStatusFor(nesteBehandlingStegModell.getBehandlingStegType(), aksjonspunkter);

            // oppdater inneværende steg
            oppdaterBehandlingStegStatus(behandling, inneværendeBehandlingStegType, inneværendeBehandlingStegStatus, tilbakeførtStegStatus);

            // oppdater nytt steg
            BehandlingStegType nesteStegtype = nesteBehandlingStegModell.getBehandlingStegType();
            oppdaterBehandlingStegType(forrige, nesteStegtype, nesteStegStatus.orElse(null), tilbakeførtStegStatus);
        }
        return tilbakeførtStegStatus;
    }

    private void oppdaterBehandlingStegType(BehandlingStegTilstandSnapshot forrigeTilstand,
                                            BehandlingStegType nesteStegType, BehandlingStegStatus nesteStegStatus,
                                            BehandlingStegStatus sluttStegStatusVedOvergang) {
        Objects.requireNonNull(behandlingRepository, "behandlingRepository");

        if (!Objects.equals(nesteStegType, Optional.ofNullable(forrigeTilstand).map(BehandlingStegTilstandSnapshot::getSteg).orElse(null))) {
            var forrigeStatus = behandling.getStatus();

            // sett steg og status for neste steg
            InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, nesteStegType, nesteStegStatus, sluttStegStatusVedOvergang);

            // Events
            var event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(behandlingModell, forrigeTilstand,
                BehandlingModellImpl.tilBehandlingsStegSnapshot(behandling.getBehandlingStegTilstand()), kontekst);
            eventPubliserer.fireEvent(event);
            eventPubliserer.fireEvent(kontekst, forrigeStatus, behandling.getStatus());
        }
    }

    /**
     * <p>
     * Verifiser at alle åpne aksjonspunkter har et definert vurderingspunkt i gjenværende steg hvor de må behandles.
     * Sikrer at ikke abstraktpunkt identifiseres ETTER at de skal være håndtert.
     */
    private void guardAlleÅpneAksjonspunkterHarDefinertVurderingspunkt() {
        var aktivtBehandlingSteg = behandling.getAktivtBehandlingSteg();

        List<Aksjonspunkt> gjenværendeÅpneAksjonspunkt = new ArrayList<>(behandling.getÅpneAksjonspunkter());

        behandlingModell.hvertStegFraOgMed(aktivtBehandlingSteg)
                .forEach(bsm -> {
                    filterVekkAksjonspunktHåndtertAvFremtidigVurderingspunkt(bsm, gjenværendeÅpneAksjonspunkt);
                });

        if (!gjenværendeÅpneAksjonspunkt.isEmpty()) {
            throw new IllegalStateException(
                    "Utvikler-feil: Det er definert aksjonspunkt [" + //$NON-NLS-1$
                            Aksjonspunkt.getKoder(gjenværendeÅpneAksjonspunkt)
                            + "] som ikke er håndtert av noe steg" //$NON-NLS-1$
                            + (aktivtBehandlingSteg == null ? " i sekvensen " : " fra og med: " + aktivtBehandlingSteg)); //$NON-NLS-1$
        }
    }

    private void filterVekkAksjonspunktHåndtertAvFremtidigVurderingspunkt(BehandlingStegModell bsm, List<Aksjonspunkt> åpneAksjonspunkter) {
        var stegType = bsm.getBehandlingStegType();
        var inngangKriterier = stegType.getAksjonspunktDefinisjonerInngang();
        var utgangKriterier = stegType.getAksjonspunktDefinisjonerUtgang();
        åpneAksjonspunkter.removeIf(elem -> {
            var elemAksDef = elem.getAksjonspunktDefinisjon();
            return elem.erÅpentAksjonspunkt() && (inngangKriterier.contains(elemAksDef) || utgangKriterier.contains(elemAksDef));
        });
    }

}
