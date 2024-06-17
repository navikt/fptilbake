package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_IVERKSETT_VEDTAK;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModellVisitor;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegTilstandSnapshot;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegUtfall;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKontrollRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.vedtak.exception.TekniskException;

/**
 * ALLE ENDRINGER I DENNE KLASSEN SKAL KLARERES OG KODE-REVIEWES MED ANSVARLIG APPLIKASJONSARKITEKT (SE
 * UTVIKLERHÅNDBOK).
 */
@RequestScoped // må være RequestScoped sålenge ikke nøstet prosessering støttes.
public class BehandlingskontrollTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(BehandlingskontrollTjeneste.class);

    private AksjonspunktKontrollRepository aksjonspunktKontrollRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private BehandlingModellRepository behandlingModellRepository;
    private BehandlingskontrollEventPubliserer eventPubliserer;

    /**
     * Sjekker om vi allerede kjører Behandlingskontroll, og aborter forsøk på nøsting av kall (støttes ikke p.t.).
     * <p>
     * Funker sålenge denne tjenesten er en {@link RequestScoped} bean.
     */
    private AtomicBoolean nøstetProsseringGuard = new AtomicBoolean();
    private BehandlingskontrollServiceProvider serviceProvider;

    BehandlingskontrollTjeneste() {
        // for CDI proxy
    }

    /**
     * SE KOMMENTAR ØVERST
     */
    @Inject
    public BehandlingskontrollTjeneste(BehandlingskontrollServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.behandlingRepository = serviceProvider.getBehandlingRepository();
        this.behandlingresultatRepository = serviceProvider.getBehandlingresultatRepository();
        this.behandlingModellRepository = serviceProvider.getBehandlingModellRepository();
        this.aksjonspunktKontrollRepository = serviceProvider.getAksjonspunktKontrollRepository();
        this.eventPubliserer = serviceProvider.getEventPubliserer();
    }

    /**
     * Prosesser behandling fra dit den sist har kommet.
     * Avhengig av vurderingspunkt (inngang- og utgang-kriterier) vil steget kjøres på nytt.
     *
     * @param kontekst - kontekst for prosessering. Opprettes gjennom {@link #initBehandlingskontroll(Long)}
     */
    public void prosesserBehandling(BehandlingskontrollKontekst kontekst) {
        var behandling = hentBehandling(kontekst);
        if (Objects.equals(BehandlingStatus.AVSLUTTET.getKode(), behandling.getStatus().getKode())) {
            return;
        }
        var modell = getModell(behandling.getType());
        BehandlingModellVisitor stegVisitor = new TekniskBehandlingStegVisitor(serviceProvider, kontekst);

        prosesserBehandling(kontekst, modell, stegVisitor);
    }

    /**
     * Prosesser forutsatt behandling er i angitt steg og status venter og steget.
     * Vil kalle gjenopptaSteg for angitt steg, senere vanlig framdrift
     *
     * @param kontekst           - kontekst for prosessering. Opprettes gjennom {@link #initBehandlingskontroll(Long)}
     * @param behandlingStegType - precondition steg
     */
    public void prosesserBehandlingGjenopptaHvisStegVenter(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType) {
        var behandling = hentBehandling(kontekst);
        if (Objects.equals(BehandlingStatus.AVSLUTTET.getKode(), behandling.getStatus().getKode())) {
            return;
        }
        var tilstand = behandling.getBehandlingStegTilstand(behandlingStegType);
        if (tilstand.isPresent() && BehandlingStegStatus.VENTER.equals(tilstand.get().getBehandlingStegStatus())) {
            var modell = getModell(behandling.getType());
            BehandlingModellVisitor stegVisitor = new TekniskBehandlingStegVenterVisitor(serviceProvider, kontekst);

            prosesserBehandling(kontekst, modell, stegVisitor);
        }
    }

    private BehandlingStegUtfall prosesserBehandling(BehandlingskontrollKontekst kontekst, BehandlingModell modell, BehandlingModellVisitor visitor) {

        validerOgFlaggStartetProsessering();
        BehandlingStegUtfall behandlingStegUtfall;
        try {
            fyrEventBehandlingskontrollStartet(kontekst, modell);
            behandlingStegUtfall = doProsesserBehandling(kontekst, modell, visitor);
            fyrEventBehandlingskontrollStoppet(kontekst, modell, behandlingStegUtfall);
        } catch (RuntimeException e) {
            fyrEventBehandlingskontrollException(kontekst, modell, e);
            throw e;
        } finally {
            ferdigProsessering();
        }
        return behandlingStegUtfall;
    }

    /**
     * Prosesser behandling enten fra akitvt steg eller steg angitt av aksjonspunktDefinsjonerKoder dersom noen er eldre
     *
     * @see #prosesserBehandling(BehandlingskontrollKontekst)
     */
    public void behandlingTilbakeføringTilTidligsteAksjonspunkt(BehandlingskontrollKontekst kontekst,
                                                                Collection<AksjonspunktDefinisjon> oppdaterteAksjonspunkter) {

        if ((oppdaterteAksjonspunkter == null) || oppdaterteAksjonspunkter.isEmpty()) {
            return;
        }

        var behandlingId = kontekst.getBehandlingId();
        var behandling = serviceProvider.hentBehandling(behandlingId);

        var stegType = behandling.getAktivtBehandlingSteg();

        var modell = getModell(behandling.getType());

        validerOgFlaggStartetProsessering();
        try {
            doTilbakeføringTilTidligsteAksjonspunkt(behandling, stegType, modell, oppdaterteAksjonspunkter);
        } finally {
            ferdigProsessering();
        }
    }

    public boolean behandlingTilbakeføringHvisTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                                      BehandlingStegType tidligereStegType) {

        if (!erSenereSteg(kontekst, tidligereStegType)) {
            behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, tidligereStegType);
            return true;
        }
        return false;
    }

    public boolean erSenereSteg(BehandlingskontrollKontekst kontekst, BehandlingStegType tidligereStegType) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        return sammenlignRekkefølge(behandling.getType(),
                behandling.getAktivtBehandlingSteg(), tidligereStegType) < 0;
    }

    /**
     * FLytt prosesen til et tidlligere steg.
     *
     * @throws IllegalStateException dersom tidligereSteg er etter aktivt steg i behandlingen (i følge BehandlingsModell for gitt
     *                               BehandlingType).
     */
    public void behandlingTilbakeføringTilTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                                  BehandlingStegType tidligereStegType) {

        final var startStatusForNyttSteg = BehandlingStegKonfigurasjon.getInngang();
        var behandlingId = kontekst.getBehandlingId();
        var behandling = serviceProvider.hentBehandling(behandlingId);

        var stegType = behandling.getAktivtBehandlingSteg();

        var modell = getModell(behandling.getType());

        validerOgFlaggStartetProsessering();
        try {
            doTilbakeføringTilTidligereBehandlngSteg(behandling, modell, tidligereStegType, stegType, startStatusForNyttSteg);
        } finally {
            ferdigProsessering();
        }

    }

    public int sammenlignRekkefølge(BehandlingType behandlingType, BehandlingStegType stegA, BehandlingStegType stegB) {
        var modell = getModell(behandlingType);
        return modell.erStegAFørStegB(stegA, stegB) ? -1
                : modell.erStegAFørStegB(stegB, stegA) ? 1
                : 0;
    }

    public boolean erStegPassert(Long behandlingId, BehandlingStegType behandlingSteg) {
        var behandling = serviceProvider.hentBehandling(behandlingId);
        return erStegPassert(behandling, behandlingSteg);
    }

    public boolean erStegPassert(Behandling behandling, BehandlingStegType behandlingSteg) {
        return sammenlignRekkefølge(behandling.getType(),
                behandling.getAktivtBehandlingSteg(), behandlingSteg) > 0;
    }

    public boolean erIStegEllerSenereSteg(Long behandlingId, BehandlingStegType behandlingSteg) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        return sammenlignRekkefølge(behandling.getType(),
                behandling.getAktivtBehandlingSteg(), behandlingSteg) >= 0;
    }

    /**
     * Flytt prosessen til senere steg. Hopper over eventuelt mellomliggende steg.
     * <p>
     * Alle mellomliggende steg og aksjonspunkt vil bli satt til AVBRUTT når dette skjer. Prosessen vil ikke kjøres.
     * Det gjelder også dersom neste steg er det definerte neste steget i prosessen (som normalt skulle blitt kalt
     * gjennom {@link #prosesserBehandling(BehandlingskontrollKontekst)}.
     *
     * @throws IllegalStateException dersom senereSteg er før eller lik aktivt steg i behandlingen (i følge BehandlingsModell for gitt
     *                               BehandlingType).
     */
    public void behandlingFramføringTilSenereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                            BehandlingStegType senereSteg) {
        final var statusInngang = BehandlingStegKonfigurasjon.getInngang();
        var behandlingId = kontekst.getBehandlingId();
        var behandling = serviceProvider.hentBehandling(behandlingId);

        var inneværendeSteg = behandling.getAktivtBehandlingSteg();

        var modell = getModell(behandling.getType());

        validerOgFlaggStartetProsessering();
        try {
            doFramføringTilSenereBehandlingSteg(senereSteg, statusInngang, behandling, inneværendeSteg, modell);
        } finally {
            ferdigProsessering();
        }

    }

    /**
     * Initier ny Behandlingskontroll, oppretter kontekst som brukes til sikre at parallle behandlinger og kjøringer går
     * i tur og orden. Dette skjer gjennom å opprette en {@link BehandlingLås} som legges ved ved lagring.
     *
     * @param behandlingId - må være med
     */
    public BehandlingskontrollKontekst initBehandlingskontroll(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); //$NON-NLS-1$
        // først lås
        var lås = serviceProvider.taLås(behandlingId);
        // så les
        var behandling = serviceProvider.hentBehandling(behandlingId);
        return new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
    }

    /**
     * Initierer ny behandlingskontroll for en ny behandling, som ikke er lagret i behandlingsRepository
     * og derfor ikke har fått tildelt behandlingId
     *
     * @param behandling - må være med
     */
    public BehandlingskontrollKontekst initBehandlingskontroll(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$
        // først lås
        var lås = serviceProvider.taLås(behandling.getId());

        // så les
        return new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
    }

    public void aksjonspunkterEndretStatus(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                                           List<Aksjonspunkt> aksjonspunkter) {
        // handlinger som skal skje når funnet
        if (!aksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunktStatusEvent(kontekst, aksjonspunkter, behandlingStegType));
        }
    }

    public List<Aksjonspunkt> lagreAksjonspunkterFunnet(BehandlingskontrollKontekst kontekst, List<AksjonspunktDefinisjon> aksjonspunkter) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        List<Aksjonspunkt> nyeAksjonspunkt = new ArrayList<>();
        aksjonspunkter.forEach(apdef -> nyeAksjonspunkt.add(aksjonspunktKontrollRepository.leggTilAksjonspunkt(behandling, apdef)));
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        aksjonspunkterEndretStatus(kontekst, null, nyeAksjonspunkt);
        return nyeAksjonspunkt;
    }

    public List<Aksjonspunkt> lagreAksjonspunkterFunnet(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                                                        List<AksjonspunktDefinisjon> aksjonspunkter) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        List<Aksjonspunkt> nyeAksjonspunkt = new ArrayList<>();
        aksjonspunkter
                .forEach(apdef -> nyeAksjonspunkt.add(aksjonspunktKontrollRepository.leggTilAksjonspunkt(behandling, apdef, behandlingStegType)));
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        aksjonspunkterEndretStatus(kontekst, behandlingStegType, nyeAksjonspunkt);
        return nyeAksjonspunkt;
    }

    public void lagreAksjonspunkterUtført(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                                          List<Aksjonspunkt> aksjonspunkter) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        List<Aksjonspunkt> utførte = new ArrayList<>();
        aksjonspunkter.stream().filter(ap -> !ap.erUtført()).forEach(ap -> {
            aksjonspunktKontrollRepository.setTilUtført(ap);
            utførte.add(ap);
        });
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        aksjonspunkterEndretStatus(kontekst, behandlingStegType, utførte);
    }

    public void lagreAksjonspunktOpprettetUtførtUtenEvent(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType, AksjonspunktDefinisjon aksjonspunkt) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        var nyttAksjonspunkt = aksjonspunktKontrollRepository.leggTilAksjonspunkt(behandling, aksjonspunkt, behandlingStegType);
        aksjonspunktKontrollRepository.setTilUtført(nyttAksjonspunkt);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    public void lagreAksjonspunkterAvbrutt(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                                           List<Aksjonspunkt> aksjonspunkter) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        lagreAksjonspunkterAvbrutt(kontekst, behandling, behandlingStegType, aksjonspunkter);
    }

    private void lagreAksjonspunkterAvbrutt(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                            BehandlingStegType behandlingStegType, List<Aksjonspunkt> aksjonspunkter) {

        List<Aksjonspunkt> avbrutte = new ArrayList<>();
        aksjonspunkter.stream().filter(ap -> !ap.erAvbrutt()).forEach(ap -> {
            aksjonspunktKontrollRepository.setTilAvbrutt(ap);
            avbrutte.add(ap);
        });
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        aksjonspunkterEndretStatus(kontekst, behandlingStegType, avbrutte);
    }

    public void lagreAksjonspunkterReåpnet(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter, boolean beholdToTrinnVurdering,
                                           boolean setTotrinn) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        List<Aksjonspunkt> reåpnet = new ArrayList<>();
        aksjonspunkter.stream().filter(ap -> !ap.erOpprettet()).forEach(ap -> {
            if (beholdToTrinnVurdering) {
                aksjonspunktKontrollRepository.setReåpnet(ap);
            } else {
                aksjonspunktKontrollRepository.setReåpnetMedTotrinn(ap, setTotrinn);
            }
            reåpnet.add(ap);
        });
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        aksjonspunkterEndretStatus(kontekst, null, reåpnet);
    }

    /**
     * Lagrer en ny behandling i behandlingRepository og fyrer av event om at en Behandling er opprettet
     */
    public void opprettBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling, Consumer<Behandling> etterLagring) {
        final var fagsakLås = serviceProvider.taFagsakLås(behandling.getFagsakId());
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        serviceProvider.oppdaterLåsVersjon(fagsakLås);
        etterLagring.accept(behandling);
        eventPubliserer.fireEvent(kontekst, null, behandling.getStatus());
    }

    public void avsluttBehandling(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = hentBehandling(kontekst);
        BehandlingStatus gammelStatus = behandling.getStatus();
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        eventPubliserer.fireEvent(kontekst, gammelStatus, behandling.getStatus());

    }

    /**
     * Setter behandlingen på vent.
     *
     * @param behandling
     * @param aksjonspunktDefinisjonIn hvilket Aksjonspunkt skal holde i 'ventingen'
     * @param fristTid                 Frist før Behandlingen å adresseres
     * @param venteårsak               Årsak til ventingen.
     */
    public Aksjonspunkt settBehandlingPåVentUtenSteg(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                                     LocalDateTime fristTid, Venteårsak venteårsak) {
        return settBehandlingPåVent(behandling, aksjonspunktDefinisjonIn, null, fristTid, venteårsak);
    }

    /**
     * Setter behandlingen på vent med angitt hvilket steg det står i.
     *
     * @param behandling
     * @param aksjonspunktDefinisjonIn hvilket Aksjonspunkt skal holde i 'ventingen'
     * @param stegType                 aksjonspunktet står i.
     * @param fristTid                 Frist før Behandlingen å adresseres
     * @param venteårsak               Årsak til ventingen.
     */
    public Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                             BehandlingStegType stegType, LocalDateTime fristTid, Venteårsak venteårsak) {
        var kontekst = initBehandlingskontroll(behandling);
        // Nullstill ansvarlig saksbehandler dersom settes på vent utenom i sluttfasen
        if (!behandling.erOrdinærSaksbehandlingAvsluttet()) {
            behandling.setAnsvarligSaksbehandler(null);
        } else {
            // Finn ut hvor dette oppstår
            try {
                throw new IllegalStateException("Satt på vent mens ligger hos beslutter");
            } catch (Exception e) {
                LOG.info("FPTILBAKE: Satt på vent mens status {}", behandling.getStatus(), e);
            }
        }
        var aksjonspunkt = aksjonspunktKontrollRepository.settBehandlingPåVent(behandling, aksjonspunktDefinisjonIn, stegType, fristTid,
                venteårsak);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        if (aksjonspunkt != null) {
            aksjonspunkterEndretStatus(kontekst, aksjonspunkt.getBehandlingStegFunnet(), singletonList(aksjonspunkt));
        }
        return aksjonspunkt;
    }

    /**
     * Setter autopunkter til utført (som en del av å gjenoppta behandlingen). Dette klargjør kun behandligen for
     * prosessering, men vil ikke drive prosessen videre.
     * Bruk {@link #prosesserBehandling(BehandlingskontrollKontekst)} el. tilsvarende for det.
     */
    private void settAutopunkterTilUtført(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        var åpneAutopunkter = behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT);
        lagreAksjonspunkterUtført(kontekst, behandling.getAktivtBehandlingSteg(), åpneAutopunkter);
    }

    /**
     * Setter autopunkter av en spessifik aksjonspunktdefinisjon til utført. Dette klargjør kun behandligen for
     * prosessering, men vil ikke drive prosessen videre.
     *
     * @param aksjonspunktDefinisjon Aksjonspunktdefinisjon til de aksjonspunktene som skal lukkes
     *                               Bruk {@link #prosesserBehandling(BehandlingskontrollKontekst)} el. tilsvarende for det.
     */
    public void settAutopunktTilUtført(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingskontrollKontekst kontekst) {
        var åpneAksjonspunktAvDef = behandling.getÅpneAksjonspunkter(List.of(aksjonspunktDefinisjon));
        lagreAksjonspunkterUtført(kontekst, behandling.getAktivtBehandlingSteg(), åpneAksjonspunktAvDef);
    }

    private void settAutopunkterTilAvbrutt(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        var åpneAutopunkter = behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT);
        lagreAksjonspunkterAvbrutt(kontekst, behandling, behandling.getAktivtBehandlingSteg(), åpneAutopunkter);
    }

    public void taBehandlingAvVentSetAlleAutopunktUtført(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        doForberedGjenopptak(behandling, kontekst, false);
    }

    public void taBehandlingAvVentSetAlleAutopunktUtførtForHenleggelse(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        doForberedGjenopptak(behandling, kontekst, true);
    }

    private void doForberedGjenopptak(Behandling behandling, BehandlingskontrollKontekst kontekst, boolean erHenleggelse) {
        var aksjonspunkterSomMedførerTilbakehopp = behandling.getÅpneAksjonspunkter().stream()
                .filter(Aksjonspunkt::tilbakehoppVedGjenopptakelse)
                .collect(Collectors.toList());

        if (aksjonspunkterSomMedførerTilbakehopp.size() > 1) {
            throw new TekniskException("FP-105126",
                    String.format("BehandlingId %s har flere enn et aksjonspunkt, hvor aksjonspunktet fører til tilbakehopp ved gjenopptakelse. Kan ikke gjenopptas.", behandling.getId()));
        }
        if (erHenleggelse) {
            settAutopunkterTilAvbrutt(kontekst, behandling);
        } else {
            settAutopunkterTilUtført(kontekst, behandling);
        }
        if (aksjonspunkterSomMedførerTilbakehopp.size() == 1) {
            var ap = aksjonspunkterSomMedførerTilbakehopp.get(0);
            var behandlingStegFunnet = ap.getBehandlingStegFunnet();
            behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, behandlingStegFunnet);
            // I tilfelle tilbakehopp reåpner autopunkt - de skal reutledes av steget.
            settAutopunkterTilUtført(kontekst, behandling);
        }
    }

    public void henleggBehandling(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        // valider invarianter
        Objects.requireNonNull(årsak, "årsak"); //$NON-NLS-1$

        var stegTilstandFør = doHenleggBehandling(kontekst, årsak);

        // FIXME (MAUR): Bør løses via FellesTransisjoner og unngå hardkoding av
        // BehandlingStegType her.
        // må fremoverføres for å trigge riktig events for opprydding
        behandlingFramføringTilSenereBehandlingSteg(kontekst, BehandlingStegType.IVERKSETT_VEDTAK);

        publiserFremhoppTransisjonHenleggelse(kontekst, stegTilstandFør, BehandlingStegType.IVERKSETT_VEDTAK);

        // sett Avsluttet og fyr status
        avsluttBehandling(kontekst);
    }

    public void henleggBehandlingFraSteg(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        // valider invarianter
        Objects.requireNonNull(årsak, "årsak"); //$NON-NLS-1$

        var stegTilstandFør = doHenleggBehandling(kontekst, årsak);

        // TODO håndter henleggelse fra tidlig steg. Nå avbrytes steget og behandlingen
        // framoverføres ikke (ok?).
        // OBS mulig rekursiv prosessering kan oppstå (evt må BehKtrl framføre til ived
        // og fortsette)
        publiserFremhoppTransisjonHenleggelse(kontekst, stegTilstandFør, BehandlingStegType.IVERKSETT_VEDTAK);

        // sett Avsluttet og fyr status
        avsluttBehandling(kontekst);
    }

    private Optional<BehandlingStegTilstand> doHenleggBehandling(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        var behandling = hentBehandling(kontekst);

        if (behandling.erSaksbehandlingAvsluttet()) {
            throw new TekniskException("FPT-143308", String.format("BehandlingId %s er allerede avsluttet, kan ikke henlegges",
                    behandling.getId()));
        }

        // sett årsak
        behandlingresultatRepository.henlegg(behandling, årsak);

        BehandlingStegType behandlingStegType = null;
        var stegTilstandFør = behandling.getBehandlingStegTilstand();
        if (stegTilstandFør.isPresent()) {
            behandlingStegType = stegTilstandFør.get().getBehandlingSteg();
        }

        // avbryt aksjonspunkt
        var åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();
        åpneAksjonspunkter.forEach(aksjonspunktKontrollRepository::setTilAvbrutt);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        eventPubliserer.fireEvent(new AksjonspunktStatusEvent(kontekst, åpneAksjonspunkter, behandlingStegType));
        return stegTilstandFør;
    }


    private void publiserFremhoppTransisjonHenleggelse(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> stegTilstandFør,
                                                       BehandlingStegType stegEtter) {
        // Publiser tranisjonsevent (eventobserver(e) håndterer tilhørende
        // tranisjonsregler)
        var erOverhopp = true;
        var event = new BehandlingTransisjonEvent(kontekst, FREMHOPP_TIL_IVERKSETT_VEDTAK, stegTilstandFør.orElse(null),
                stegEtter, erOverhopp);
        eventPubliserer.fireEvent(event);
    }

    public boolean skalAksjonspunktLøsesIEllerEtterSteg(BehandlingType behandlingType, BehandlingStegType behandlingSteg, AksjonspunktDefinisjon apDef) {

        var modell = getModell(behandlingType);
        var apLøsesteg = Optional.ofNullable(modell
                        .finnTidligsteStegForAksjonspunktDefinisjon(singletonList(apDef)))
                .map(BehandlingStegModell::getBehandlingStegType)
                .orElse(null);
        if (apLøsesteg == null) {
            // AksjonspunktDefinisjon finnes ikke på stegene til denne behandlingstypen. Ap
            // kan derfor ikke løses.
            return false;
        }

        return behandlingSteg.equals(apLøsesteg) || modell.erStegAFørStegB(behandlingSteg, apLøsesteg);
    }

    // TODO: (PK-49128) Midlertidig løsning for å filtrere aksjonspunkter til høyre
    // for steg i hendelsemodul
    public Set<AksjonspunktDefinisjon> finnAksjonspunktDefinisjonerFraOgMed(Behandling behandling, BehandlingStegType steg, boolean medInngangOgså) {
        var modell = getModell(behandling.getType());
        return modell.finnAksjonspunktDefinisjonerFraOgMed(steg, medInngangOgså);
    }

    protected BehandlingStegUtfall doProsesserBehandling(BehandlingskontrollKontekst kontekst, BehandlingModell modell,
                                                         BehandlingModellVisitor visitor) {

        var behandling = hentBehandling(kontekst);

        if (Objects.equals(BehandlingStatus.AVSLUTTET.getKode(), behandling.getStatus().getKode())) {
            throw new IllegalStateException("Utviklerfeil: Kan ikke prosessere avsluttet behandling"); //$NON-NLS-1$
        }

        var startSteg = behandling.getAktivtBehandlingSteg();
        var behandlingStegUtfall = modell.prosesserFra(startSteg, visitor);

        if (behandlingStegUtfall == null) {
            avsluttBehandling(kontekst);
        }
        return behandlingStegUtfall;
    }

    protected void doFramføringTilSenereBehandlingSteg(BehandlingStegType senereSteg, final BehandlingStegStatus startStatusForNyttSteg,
                                                       Behandling behandling, BehandlingStegType inneværendeSteg, BehandlingModell modell) {
        if (!erSenereSteg(modell, inneværendeSteg, senereSteg)) {
            throw new IllegalStateException(
                    "Kan ikke angi steg [" + senereSteg + "] som er før eller lik inneværende steg [" + inneværendeSteg + "]" + "for behandlingId " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            + behandling.getId());
        }
        oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, senereSteg, startStatusForNyttSteg,
                BehandlingStegStatus.AVBRUTT);
    }

    protected void doTilbakeføringTilTidligereBehandlngSteg(Behandling behandling, BehandlingModell modell,
                                                            BehandlingStegType tidligereStegType, BehandlingStegType stegType,
                                                            final BehandlingStegStatus startStatusForNyttSteg) {
        if (behandling.erSaksbehandlingAvsluttet() && !"BoXFM".equals(behandling.getFagsak().getSaksnummer().getVerdi())) {
            throw new IllegalStateException(
                    "Kan ikke tilbakeføre fra [" + stegType + "]");
        }
        if (!erLikEllerTidligereSteg(modell, stegType, tidligereStegType)) {
            throw new IllegalStateException(
                    "Kan ikke angi steg [" + tidligereStegType + "] som er etter [" + stegType + "]" + "for behandlingId " + behandling.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        if (tidligereStegType.equals(stegType) && (behandling.getBehandlingStegStatus() != null)
                && behandling.getBehandlingStegStatus().erVedInngang()) {
            // Her står man allerede på steget man skal tilbakeføres, på inngang -> ingen
            // tilbakeføring gjennomføres.
            return;
        }
        oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, tidligereStegType, startStatusForNyttSteg,
                BehandlingStegStatus.TILBAKEFØRT);
    }

    protected void doTilbakeføringTilTidligsteAksjonspunkt(Behandling behandling, BehandlingStegType stegType, BehandlingModell modell,
                                                           Collection<AksjonspunktDefinisjon> oppdaterteAksjonspunkter) {
        Consumer<BehandlingStegType> oppdaterBehandlingStegStatus = (bst) -> {
            var stegStatus = modell.finnStegStatusFor(bst, oppdaterteAksjonspunkter);
            if (stegStatus.isPresent()
                    && !(Objects.equals(stegStatus.get(), behandling.getBehandlingStegStatus())
                    && Objects.equals(bst, behandling.getAktivtBehandlingSteg()))) {
                // er på starten av steg med endret aksjonspunkt. Ikke kjør steget her, kun
                // oppdater
                oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, bst, stegStatus.get(),
                        BehandlingStegStatus.TILBAKEFØRT);
            }
        };

        var førsteAksjonspunktSteg = modell
                .finnTidligsteStegForAksjonspunktDefinisjon(oppdaterteAksjonspunkter);

        var aksjonspunktStegType = førsteAksjonspunktSteg == null ? null
                : førsteAksjonspunktSteg.getBehandlingStegType();

        if (Objects.equals(stegType, aksjonspunktStegType)) {
            // samme steg, kan ha ny BehandlingStegStatus
            oppdaterBehandlingStegStatus.accept(stegType);
        } else {
            // tilbakeføring til tidligere steg
            var revidertStegType = modell.finnFørsteSteg(stegType, aksjonspunktStegType);
            oppdaterBehandlingStegStatus.accept(revidertStegType.getBehandlingStegType());
        }
    }

    protected void fireEventBehandlingStegOvergang(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                   BehandlingStegTilstandSnapshot forrigeTilstand, BehandlingStegTilstandSnapshot nyTilstand) {
        var modell = getModell(behandling.getType());
        var event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(modell, forrigeTilstand, nyTilstand, kontekst);
        getEventPubliserer().fireEvent(event);
    }

    protected void oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(Behandling behandling, BehandlingStegType revidertStegType,
                                                                                           BehandlingStegStatus behandlingStegStatus,
                                                                                           BehandlingStegStatus sluttStatusForAndreÅpneSteg) {
        // Eksisterende tilstand og status
        var statusFør = behandling.getStatus();
        var fraTilstand = BehandlingModellImpl.tilBehandlingsStegSnapshot(behandling.getBehandlingStegTilstand());

        // Oppdater behandling og lagre
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, revidertStegType, behandlingStegStatus, sluttStatusForAndreÅpneSteg);
        var skriveLås = behandlingRepository.taSkriveLås(behandling);
        var kontekst = new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), skriveLås);
        behandlingRepository.lagre(behandling, skriveLås);

        // Publiser oppdatering
        var statusEtter = behandling.getStatus();
        var tilTilstand = BehandlingModellImpl.tilBehandlingsStegSnapshot(behandling.getBehandlingStegTilstand());
        fireEventBehandlingStegOvergang(kontekst, behandling, fraTilstand, tilTilstand);
        eventPubliserer.fireEvent(kontekst, statusFør, statusEtter);
    }

    protected Behandling hentBehandling(BehandlingskontrollKontekst kontekst) {
        Objects.requireNonNull(kontekst, "kontekst"); //$NON-NLS-1$
        var behandlingId = kontekst.getBehandlingId();
        return serviceProvider.hentBehandling(behandlingId);
    }

    protected BehandlingskontrollEventPubliserer getEventPubliserer() {
        return eventPubliserer;
    }

    protected BehandlingModell getModell(BehandlingType behandlingType) {
        return behandlingModellRepository.getModell(behandlingType);
    }

    private void fyrEventBehandlingskontrollException(BehandlingskontrollKontekst kontekst, BehandlingModell modell, RuntimeException e) {
        var behandling = hentBehandling(kontekst);
        var stoppetEvent = new BehandlingskontrollEvent.ExceptionEvent(kontekst, modell, behandling.getAktivtBehandlingSteg(),
                behandling.getBehandlingStegStatus(), e);
        eventPubliserer.fireEvent(stoppetEvent);
    }

    private void fyrEventBehandlingskontrollStoppet(BehandlingskontrollKontekst kontekst, BehandlingModell modell, BehandlingStegUtfall stegUtfall) {
        var behandling = hentBehandling(kontekst);
        BehandlingskontrollEvent event;
        if (behandling.erAvsluttet()) {
            event = new BehandlingskontrollEvent.AvsluttetEvent(kontekst, modell, behandling.getAktivtBehandlingSteg(), behandling.getBehandlingStegStatus());
        } else if (stegUtfall == null) {
            event = new BehandlingskontrollEvent.StoppetEvent(kontekst, modell,
                    behandling.getSisteBehandlingStegTilstand().map(BehandlingStegTilstand::getBehandlingSteg).orElse(null), null);
        } else {
            event = new BehandlingskontrollEvent.StoppetEvent(kontekst, modell, stegUtfall.behandlingStegType(), stegUtfall.resultat());
        }
        eventPubliserer.fireEvent(event);
    }

    private void fyrEventBehandlingskontrollStartet(BehandlingskontrollKontekst kontekst, BehandlingModell modell) {
        var behandling = hentBehandling(kontekst);
        var startetEvent = new BehandlingskontrollEvent.StartetEvent(kontekst, modell, behandling.getAktivtBehandlingSteg(),
                behandling.getBehandlingStegStatus());
        eventPubliserer.fireEvent(startetEvent);
    }

    public void fremoverTransisjon(TransisjonIdentifikator transisjonId, BehandlingskontrollKontekst kontekst) {
        var behandling = serviceProvider.hentBehandling(kontekst.getBehandlingId());
        var stegTilstandFør = behandling.getBehandlingStegTilstand();
        var fraSteg = behandling.getAktivtBehandlingSteg();

        // Flytt behandlingssteg-peker fremover
        var modell = getModell(behandling.getType());
        var transisjon = modell.finnTransisjon(transisjonId);
        var fraStegModell = modell.finnSteg(fraSteg);
        var tilStegModell = transisjon.nesteSteg(fraStegModell);
        var tilSteg = tilStegModell.getBehandlingStegType();

        behandlingFramføringTilSenereBehandlingSteg(kontekst, tilSteg);

        // Publiser tranisjonsevent (eventobserver(e) håndterer tilhørende
        // tranisjonsregler)
        var event = new BehandlingTransisjonEvent(kontekst, transisjonId, stegTilstandFør.orElse(null), tilSteg,
                transisjon.getMålstegHvisHopp().isPresent());
        eventPubliserer.fireEvent(event);
    }

    public boolean inneholderSteg(Behandling behandling, BehandlingStegType behandlingStegType) {
        var modell = getModell(behandling.getType());
        return modell.hvertSteg()
                .anyMatch(steg -> steg.getBehandlingStegType().equals(behandlingStegType));
    }

    private boolean erSenereSteg(BehandlingModell modell, BehandlingStegType inneværendeSteg,
                                 BehandlingStegType forventetSenereSteg) {
        return modell.erStegAFørStegB(inneværendeSteg, forventetSenereSteg);
    }

    private boolean erLikEllerTidligereSteg(BehandlingModell modell, BehandlingStegType inneværendeSteg,
                                            BehandlingStegType forventetTidligereSteg) {
        // TODO (BIXBITE) skal fjernes når innlegging av papirsøknad er inn i et steg
        if (inneværendeSteg == null) {
            return false;
        }
        if (Objects.equals(inneværendeSteg, forventetTidligereSteg)) {
            return true;
        }
        var førsteSteg = modell.finnFørsteSteg(inneværendeSteg, forventetTidligereSteg).getBehandlingStegType();
        return Objects.equals(forventetTidligereSteg, førsteSteg);
    }

    public void oppdaterBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    private void validerOgFlaggStartetProsessering() {
        if (nøstetProsseringGuard.get()) {
            throw new IllegalStateException("Støtter ikke nøstet prosessering i " + getClass().getSimpleName());
        }
        nøstetProsseringGuard.set(true);
    }

    private void ferdigProsessering() {
        nøstetProsseringGuard.set(false);
    }

}
