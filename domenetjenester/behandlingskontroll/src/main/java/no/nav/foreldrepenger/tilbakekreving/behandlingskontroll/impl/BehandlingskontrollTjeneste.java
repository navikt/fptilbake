package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_IVERKSETT_VEDTAK;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.cxf.common.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktAvbruttEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModellVisitor;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegTilstandEndringEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegUtfall;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakLåsRepository;

/**
 * ALLE ENDRINGER I DENNE KLASSEN SKAL KLARERES OG KODE-REVIEWES MED ANSVARLIG APPLIKASJONSARKITEKT (SE
 * UTVIKLERHÅNDBOK).
 */
@RequestScoped // må være RequestScoped sålenge ikke nøstet prosessering støttes.
public class BehandlingskontrollTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BehandlingskontrollTjeneste.class);

    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private BehandlingModellRepository behandlingModellRepository;
    private InternalManipulerBehandling manipulerInternBehandling;
    private BehandlingskontrollEventPubliserer eventPubliserer = BehandlingskontrollEventPubliserer.NULL_EVENT_PUB;
    private BehandlingStegKonfigurasjon behandlingStegKonfigurasjon;
    private FagsakLåsRepository fagsakLåsRepository;

    /**
     * Sjekker om vi allerede kjører Behandlingskontroll, og aborter forsøk på nøsting av kall (støttes ikke p.t.).
     * <p>
     * Funker sålenge denne tjenesten er en {@link RequestScoped} bean.
     */
    private AtomicBoolean nøstetProsseringGuard = new AtomicBoolean();

    BehandlingskontrollTjeneste() {
        // for CDI proxy
    }

    /**
     * SE KOMMENTAR ØVERST
     */
    @Inject
    public BehandlingskontrollTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                       BehandlingModellRepository behandlingModellRepository,
                                       BehandlingskontrollEventPubliserer eventPubliserer) {

        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingresultatRepository = repositoryProvider.getBehandlingresultatRepository();
        this.behandlingModellRepository = behandlingModellRepository;
        this.manipulerInternBehandling = new InternalManipulerBehandling(repositoryProvider);
        this.behandlingStegKonfigurasjon = behandlingModellRepository.getBehandlingStegKonfigurasjon();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.fagsakLåsRepository = repositoryProvider.getFagsakLåsRepository();
        if (eventPubliserer != null) {
            this.eventPubliserer = eventPubliserer;
        }
    }

    /**
         * Prosesser behandling fra dit den sist har kommet.
         * Avhengig av vurderingspunkt (inngang- og utgang-kriterier) vil steget kjøres på nytt.
         *
         * @param kontekst
         *            - kontekst for prosessering. Opprettes gjennom {@link #initBehandlingskontroll(Long)}
         */
    public void prosesserBehandling(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = hentBehandling(kontekst);
        if (behandling.erAvsluttet()) {
            return;
        }
        BehandlingModell modell = getModell(behandling);
        BehandlingModellVisitor stegVisitor = new TekniskBehandlingStegVisitor(repositoryProvider, this, kontekst, eventPubliserer);

        prosesserBehandling(kontekst, modell, stegVisitor);
    }

    /**
         * Prosesser forutsatt behandling er i angitt steg og status venter og steget.
         * Vil kalle gjenopptaSteg for angitt steg, senere vanlig framdrift
         *
         * @param kontekst
         *            - kontekst for prosessering. Opprettes gjennom {@link #initBehandlingskontroll(Long)}
         * @param behandlingStegType
         *            - precondition steg
         */
    public void prosesserBehandlingGjenopptaHvisStegVenter(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType) {
        Behandling behandling = hentBehandling(kontekst);
        if (behandling.erAvsluttet()) {
            return;
        }
        Optional<BehandlingStegTilstand> tilstand = behandling.getBehandlingStegTilstand(behandlingStegType);
        if (tilstand.isPresent() && BehandlingStegStatus.VENTER.equals(tilstand.get().getBehandlingStegStatus())) {
            BehandlingModell modell = getModell(behandling);
            BehandlingModellVisitor stegVisitor = new TekniskBehandlingStegVenterVisitor(repositoryProvider, this, kontekst, eventPubliserer);

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
                                                                Collection<String> oppdaterteAksjonspunkter, boolean erOverstyring) {
        if (CollectionUtils.isEmpty(oppdaterteAksjonspunkter)) {
            return;
        }

        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        BehandlingStegType stegType = behandling.getAktivtBehandlingSteg();

        BehandlingModell modell = getModell(behandling);

        validerOgFlaggStartetProsessering();
        try {
            doTilbakeføringTilTidligsteAksjonspunkt(behandling, stegType, modell, oppdaterteAksjonspunkter, erOverstyring);
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
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        BehandlingModell modell = getModell(behandling);
        BehandlingStegType aktivtBehandlingSteg = behandling.getAktivtBehandlingSteg();
        return erSenereSteg(modell, aktivtBehandlingSteg, tidligereStegType);
    }

    /**
         * FLytt prosesen til et tidlligere steg.
         *
         * @throws IllegalStateException
         *             dersom tidligereSteg er etter aktivt steg i behandlingen (i følge BehandlingsModell for gitt
         *             BehandlingType).
         */
    public void behandlingTilbakeføringTilTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                                  BehandlingStegType tidligereStegType) {

        final BehandlingStegStatus startStatusForNyttSteg = getStatusKonfigurasjon().getUtgang();
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        BehandlingStegType stegType = behandling.getAktivtBehandlingSteg();

        BehandlingModell modell = getModell(behandling);

        validerOgFlaggStartetProsessering();
        try {
            doTilbakeføringTilTidligereBehandlngSteg(behandling, modell, tidligereStegType, stegType, startStatusForNyttSteg);
        } finally {
            ferdigProsessering();
        }

    }

    public int sammenlignRekkefølge(Behandling behandling, BehandlingStegType stegA, BehandlingStegType stegB) {
        BehandlingModell modell = getModell(behandling);
        return modell.erStegAFørStegB(stegA, stegB) ? -1
                : modell.erStegAFørStegB(stegB, stegA) ? 1
                : 0;
    }

    /**
         * Flytt prosessen til senere steg. Hopper over eventuelt mellomliggende steg.
         *
         * Alle mellomliggende steg og aksjonspunkt vil bli satt til AVBRUTT når dette skjer. Prosessen vil ikke kjøres.
         * Det gjelder også dersom neste steg er det definerte neste steget i prosessen (som normalt skulle blitt kalt
         * gjennom {@link #prosesserBehandling(BehandlingskontrollKontekst)}.
         *
         * @throws IllegalStateException
         *             dersom senereSteg er før eller lik aktivt steg i behandlingen (i følge BehandlingsModell for gitt
         *             BehandlingType).
         */
    public void behandlingFramføringTilSenereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                            BehandlingStegType senereSteg) {
        final BehandlingStegStatus statusInngang = getStatusKonfigurasjon().getInngang();
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        BehandlingStegType inneværendeSteg = behandling.getAktivtBehandlingSteg();

        BehandlingModell modell = getModell(behandling);

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
         * @param behandlingId
         *            - må være med
         */
    public BehandlingskontrollKontekst initBehandlingskontroll(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); //$NON-NLS-1$

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return initBehandlingskontroll(behandling);
    }

    /**
         * Initierer ny behandlingskontroll for en ny behandling, som ikke er lagret i behandlingsRepository
         * og derfor ikke har fått tildelt behandlingId
         *
         * @param behandling
         *            - må være med
         */
    public BehandlingskontrollKontekst initBehandlingskontroll(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$
        // først lås
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        // så les
        return new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
    }

    /**
         * Markerer ett eller flere aksjonspunkter som utført og oppdaterer status og steg på bakgrunn av det.
         * <p>
         * Bør bare kalles dersom status eller begrunnelse for Utført endres.
         *
         * @param behandlingStegType
         *            - steg utført i.
         */
    public void aksjonspunkterUtført(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter,
                                     BehandlingStegType behandlingStegType) {
        if (!aksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunktUtførtEvent(kontekst, aksjonspunkter, behandlingStegType));
        }
    }

    /**
         * Markerer at ett eller flere aksjonspunkter er funnet i et gitt behandlingsteg
         */
    // for symmetri med aksjonspunkterUtført
    public void aksjonspunkterFunnet(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                                     List<Aksjonspunkt> aksjonspunkter) {
        // handlinger som skal skje når funnet
        if (!aksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunkterFunnetEvent(kontekst, aksjonspunkter, behandlingStegType));
        }
    }

    public BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon() {
        return behandlingStegKonfigurasjon;
    }

    /**
         * Lagrer en ny behandling i behandlingRepository og fyrer av event om at en Behandling er opprettet
         */
    public void opprettBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        final FagsakLås fagsakLås = fagsakLåsRepository.taLås(behandling.getFagsak());
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        fagsakLåsRepository.oppdaterLåsVersjon(fagsakLås);
        eventPubliserer.fireEvent(kontekst, null, behandling.getStatus());
    }

    /**
         * Opprett ny behandling for gitt fagsak og BehandlingType.
         * <p>
         * Vil alltid opprette ny behandling, selv om det finnes eksisterende åpen behandling på fagsaken.
         *
         * @param fagsak
         *            - fagsak med eller uten eksisterende behandling
         * @param behandlingType
         *            - type behandling
         * @param behandlingOppdaterer
         *            - funksjon for oppdatering av grunnlag
         * @return Behandling - nylig opprettet og lagret.
         */
    public Behandling opprettNyBehandling(Fagsak fagsak, BehandlingType behandlingType, Consumer<Behandling> behandlingOppdaterer) {
        Behandling.Builder behandlingBuilder = Behandling.nyBehandlingFor(fagsak, behandlingType);
        Behandling nyBehandling = behandlingBuilder.build();
        behandlingOppdaterer.accept(nyBehandling);

        BehandlingskontrollKontekst kontekst = this.initBehandlingskontroll(nyBehandling);
        this.opprettBehandling(kontekst, nyBehandling);
        return nyBehandling;
    }

    /**
         * Oppretter eller oppdaterer en behandling.
         * <p>
         * <ul>
         * <li>Oppdaterer eksisterende behandling hvis det finnes en åpen behandling.</li>
         * <li>Oppretter ny behanding dersom det ikke finnes eksisterende åpen behandling.</li>
         * <li>Benytter grunnlag fra tidligere behandling hvis det finnes eksisterende avsluttede behandlinger.</li>
         * </ul>
         *
         * @param fagsak
         *            - fagsak med eller uten eksisterende behandling
         * @param behandlingType
         *            - type behandling
         * @param behandlingOppdaterer
         *            - funksjon for å oppdatere grunnlag eller behandling under arbeid
         * @return Behandling - opprettet eller oppdatert, og lagret.
         */
    public Behandling opprettNyEllerOppdaterEksisterendeBehandling(Fagsak fagsak, BehandlingType behandlingType,
                                                                   Consumer<Behandling> behandlingOppdaterer) {
        Optional<Behandling> behandlingOpt = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId(), behandlingType);
        if (behandlingOpt.isPresent()) {
            Behandling behandling = behandlingOpt.get();
            if (behandling.erSaksbehandlingAvsluttet()) {
                return opprettNyFraTidligereBehandling(behandling, behandlingType, behandlingOppdaterer);
            } else {
                oppdaterEksisterendeBehandling(behandling, behandlingOppdaterer, false);
                return behandlingRepository.hentBehandling(behandling.getId());
            }
        } else {
            return opprettNyBehandling(fagsak, behandlingType, behandlingOppdaterer);
        }
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
         * @param aksjonspunktDefinisjon hvilket Aksjonspunkt skal holde i 'ventingen'
         * @param fristTid Frist før Behandlingen å adresseres
         * @param venteårsak Årsak til ventingen.
         *
         */
    public Aksjonspunkt settBehandlingPåVentUtenSteg(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                                     LocalDateTime fristTid, Venteårsak venteårsak) {
        return settBehandlingPåVent(behandling, aksjonspunktDefinisjonIn, null, fristTid, venteårsak);
    }

    /**
         * Setter behandlingen på vent med angitt hvilket steg det står i.
         *
         * @param behandling
         * @param aksjonspunktDefinisjon hvilket Aksjonspunkt skal holde i 'ventingen'
         * @param BehandlingStegType stegType aksjonspunktet står i.
         * @param fristTid Frist før Behandlingen å adresseres
         * @param venteårsak Årsak til ventingen.
         *
         */
    public Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                             BehandlingStegType stegType, LocalDateTime fristTid, Venteårsak venteårsak) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.settBehandlingPåVent(behandling, aksjonspunktDefinisjonIn, stegType, fristTid,
                venteårsak);
        behandlingRepository.lagre(behandling, lås);
        if (aksjonspunkt != null) {
            BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
            aksjonspunkterFunnet(kontekst, aksjonspunkt.getBehandlingStegFunnet(), Arrays.asList(aksjonspunkt));
        }
        return aksjonspunkt;
    }

    /**
         * Setter autopunkter til utført (som en del av å gjenoppta behandlingen). Dette klargjør kun behandligen for
         * prosessering, men vil ikke drive prosessen videre.
         * Bruk {@link #prosesserBehandling(BehandlingskontrollKontekst)} el. tilsvarende for det.
         */
    public void settAutopunkterTilUtført(BehandlingskontrollKontekst kontekst, boolean erHenleggelse) {
        Behandling behandling = hentBehandling(kontekst);
        List<Aksjonspunkt> åpneAutopunkter = behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT);
        if (åpneAutopunkter.isEmpty()) {
            return;
        }
        åpneAutopunkter.forEach(autopunkt -> aksjonspunktRepository.setTilUtført(autopunkt));
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());

        if (!erHenleggelse) {
            // Publiser at aksjonspunktet er "normalt" utført
            aksjonspunkterUtført(kontekst, åpneAutopunkter, behandling.getAktivtBehandlingSteg());
        }
    }

    /**
         * Setter autopunkter av en spessifik aksjonspunktdefinisjon til utført. Dette klargjør kun behandligen for
         * prosessering, men vil ikke drive prosessen videre.
         *
         * @param aksjonspunktDefinisjon Aksjonspunktdefinisjon til de aksjonspunktene som skal lukkes
         *            Bruk {@link #prosesserBehandling(BehandlingskontrollKontekst)} el. tilsvarende for det.
         */
    public void settAutopunktTilUtført(AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingskontrollKontekst kontekst) {
        Behandling behandling = hentBehandling(kontekst);
        Optional<Aksjonspunkt> aksjonspunktMedDefinisjonOptional = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);

        aksjonspunktMedDefinisjonOptional.ifPresent(aksjonspunkt -> {
            if (aksjonspunkt.erÅpentAksjonspunkt()) {
                aksjonspunktRepository.setTilUtført(aksjonspunkt);
                behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
                aksjonspunkterUtført(kontekst, singletonList(aksjonspunkt), behandling.getAktivtBehandlingSteg());
            }
        });
    }

    /**
         * Setter tilbake aktivt steg hvis autopunkt er av type fødsel AUTO_VENT_PÅ_FØDSELREGISTRERING.
         * Dette er et spesialtilfelle som krever at steget kjøres om igjen (eneste kjente tilfelle)
         */
    public void taBehandlingAvVent(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        // Kjør steget på nytt ved gjenopptakelse fra venting, når aksjonspunktet er markert for dette
        List<Aksjonspunkt> aksjonspunkterSomMedførerTilbakehopp = behandling.getÅpneAksjonspunkter().stream()
            .filter(Aksjonspunkt::tilbakehoppVedGjenopptakelse)
            .collect(Collectors.toList());

        if (aksjonspunkterSomMedførerTilbakehopp.size() > 1) {
            throw BehandlingskontrollFeil.FACTORY.kanIkkeGjenopptaBehandlingFantFlereAksjonspunkterSomMedførerTilbakehopp(behandling.getId()).toException();
        }
        if (aksjonspunkterSomMedførerTilbakehopp.size() == 1) {
            Aksjonspunkt ap = aksjonspunkterSomMedførerTilbakehopp.get(0);
            BehandlingStegType behandlingStegFunnet = ap.getBehandlingStegFunnet();
            logger.info("Fant aksjonspunkt kode={}, status={}, tilbakehopp={}, behandlingStegFunnet={} som medfører tilbakehopp på behandlingId={}",
                ap.getAksjonspunktDefinisjon().getKode(), ap.getStatus().getKode(), ap.tilbakehoppVedGjenopptakelse(), behandlingStegFunnet, behandling.getId());
            aksjonspunktRepository.setTilUtført(ap);
            eventPubliserer.fireEvent(new AksjonspunktUtførtEvent(kontekst, singletonList(ap), behandlingStegFunnet));
            behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, behandlingStegFunnet);
        }
    }

    /** Henlegg en behandling */
    public void henleggBehandling(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        // valider invarianter
        Objects.requireNonNull(årsak, "årsak"); //$NON-NLS-1$

        Optional<BehandlingStegTilstand> stegTilstandFør = doHenleggBehandling(kontekst, årsak);

        // FIXME: Bør løses via FellesTransisjoner og unngå hardkoding av BehandlingStegType her.
        // må fremoverføres for å trigge riktig events for opprydding
        behandlingFramføringTilSenereBehandlingSteg(kontekst, BehandlingStegType.IVERKSETT_VEDTAK);

        publiserFremhoppTransisjon(kontekst, stegTilstandFør, BehandlingStegType.IVERKSETT_VEDTAK);

        // sett Avsluttet og fyr status
        avsluttBehandling(kontekst);
    }

    private Optional<BehandlingStegTilstand> doHenleggBehandling(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        Behandling behandling = hentBehandling(kontekst);

        if (behandling.erSaksbehandlingAvsluttet()) {
            throw BehandlingskontrollFeil.FACTORY.kanIkkeHenleggeAvsluttetBehandling(behandling.getId()).toException();
        }

        // sett årsak
        behandlingresultatRepository.hent(behandling)
                .ifPresentOrElse(eksisterende -> oppdaterBehandlingresultat(eksisterende, årsak), // oppdater eksisterende
                        () -> opprettBehandlingresultat(behandling, årsak)); // opprett ny

        BehandlingStegType behandlingStegType = null;
        Optional<BehandlingStegTilstand> stegTilstandFør = behandling.getBehandlingStegTilstand();
        if (stegTilstandFør.isPresent()) {
            behandlingStegType = stegTilstandFør.get().getBehandlingSteg();
        }

        // avbryt aksjonspunkt
        List<Aksjonspunkt> åpneAksjonspunkter = behandling.getÅpneAksjonspunkter();
        åpneAksjonspunkter.forEach(aksjonspunktRepository::setTilAvbrutt);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        eventPubliserer.fireEvent(new AksjonspunktAvbruttEvent(kontekst, åpneAksjonspunkter, behandlingStegType));
        return stegTilstandFør;
    }

    private void oppdaterBehandlingresultat(Behandlingsresultat eksisterende, BehandlingResultatType årsak) {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderEndreEksisterende(eksisterende)
                .medBehandlingResultatType(årsak)
                .build();
        behandlingresultatRepository.lagre(behandlingsresultat);
    }

    private void opprettBehandlingresultat(Behandling behandling, BehandlingResultatType årsak) {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
                .medBehandling(behandling)
                .medBehandlingResultatType(årsak)
                .build();
        behandlingresultatRepository.lagre(behandlingsresultat);
    }

    /**
         * Ny metode som forbereder en behandling for prosessering - setter autopunkt til utført og evt tilbakeføring ved gjenopptak.
         * Behandlingen skal være klar til prosessering uten åpne autopunkt når kallet er ferdig.
         */
    public void taBehandlingAvVentSetAlleAutopunktUtført(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        List<Aksjonspunkt> aksjonspunkterSomMedførerTilbakehopp = behandling.getÅpneAksjonspunkter().stream()
            .filter(Aksjonspunkt::tilbakehoppVedGjenopptakelse)
            .collect(Collectors.toList());

        if (aksjonspunkterSomMedførerTilbakehopp.size() > 1) {
            throw BehandlingskontrollFeil.FACTORY.kanIkkeGjenopptaBehandlingFantFlereAksjonspunkterSomMedførerTilbakehopp(behandling.getId()).toException();
        }
        settAutopunkterTilUtført(kontekst, false);
        if (aksjonspunkterSomMedførerTilbakehopp.size() == 1) {
            Aksjonspunkt ap = aksjonspunkterSomMedførerTilbakehopp.get(0);
            BehandlingStegType behandlingStegFunnet = ap.getBehandlingStegFunnet();
            behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, behandlingStegFunnet);
            // I tilfelle tilbakehopp reåpner autopunkt - de skal reutledes av steget.
            settAutopunkterTilUtført(kontekst, false);
        }
    }

    private void publiserFremhoppTransisjon(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> stegTilstandFør, BehandlingStegType stegEtter) {
        // Publiser tranisjonsevent (eventobserver(e) håndterer tilhørende tranisjonsregler)
        boolean erOverhopp = true;
        BehandlingTransisjonEvent event = new BehandlingTransisjonEvent(kontekst, FREMHOPP_TIL_IVERKSETT_VEDTAK, stegTilstandFør, stegEtter, erOverhopp);
        eventPubliserer.fireEvent(event);
    }

    public boolean erStegPassert(Behandling behandling, BehandlingStegType behandlingSteg) {

        BehandlingModell modell = getModell(behandling);
        BehandlingStegType aktivtSteg = behandling.getAktivtBehandlingSteg();

        return !modell.erStegAFørStegB(aktivtSteg, behandlingSteg) && !aktivtSteg.equals(behandlingSteg);
    }

    /**
         * Sjekker i behandlingsmodellen om aksjonspunktet skal løses i eller etter det angitte steget.
         *
         * @param behandling
         * @param behandlingSteg steget som aksjonspunktet skal sjekkes mot
         * @param aksjonspunktDefinisjon aksjonspunktet som skal sjekkes
         * @return true dersom aksjonspunktet skal løses i eller etter det angitte steget.
         */
    public boolean skalAksjonspunktReaktiveresIEllerEtterSteg(Behandling behandling, BehandlingStegType behandlingSteg,
                                                              AksjonspunktDefinisjon apDef) {

        BehandlingModell modell = getModell(behandling);
        BehandlingStegType apLøsesteg = Optional.ofNullable(modell
                .finnTidligsteStegForAksjonspunktDefinisjon(singletonList(apDef.getKode())))
                .map(BehandlingStegModell::getBehandlingStegType)
                .orElse(null);
        if (apLøsesteg == null) {
            // AksjonspunktDefinisjon finnes ikke på stegene til denne behandlingstypen. Ap kan derfor ikke løses.
            return false;
        }

        return behandlingSteg.equals(apLøsesteg) || modell.erStegAFørStegB(behandlingSteg, apLøsesteg);
    }

    // TODO: (PK-49128) Midlertidig løsning for å filtrere aksjonspunkter til høyre for steg i hendelsemodul
    public Set<String> finnAksjonspunktDefinisjonerFraOgMed(Behandling behandling, BehandlingStegType steg, boolean medInngangOgså) {
        BehandlingModell modell = getModell(behandling);
        return modell.finnAksjonspunktDefinisjonerFraOgMed(steg, medInngangOgså);
    }

    protected BehandlingStegUtfall doProsesserBehandling(BehandlingskontrollKontekst kontekst,
                                                         BehandlingModell modell,
                                                         BehandlingModellVisitor visitor) {
        Behandling behandling = hentBehandling(kontekst);

        if (Objects.equals(BehandlingStatus.AVSLUTTET.getKode(), behandling.getStatus().getKode())) {
            throw new IllegalStateException("Utviklerfeil: Kan ikke prosessere avsluttet behandling"); //$NON-NLS-1$
        }

        BehandlingStegType startSteg = behandling.getAktivtBehandlingSteg();
        BehandlingStegUtfall behandlingStegUtfall = modell.prosesserFra(startSteg, visitor);

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
                BehandlingStegStatus.AVBRUTT, false);
    }

    protected void doTilbakeføringTilTidligereBehandlngSteg(Behandling behandling, BehandlingModell modell,
                                                            BehandlingStegType tidligereStegType, BehandlingStegType stegType,
                                                            final BehandlingStegStatus startStatusForNyttSteg) {
        if (!erLikEllerTidligereSteg(modell, stegType, tidligereStegType)) {
            throw new IllegalStateException(
                    "Kan ikke angi steg [" + tidligereStegType + "] som er etter [" + stegType + "]" + "for behandlingId " + behandling.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        if (tidligereStegType.equals(stegType) && behandling.getBehandlingStegStatus() != null && behandling.getBehandlingStegStatus().erVedInngang()) {
            // Her står man allerede på steget man skal tilbakeføres, på inngang -> ingen tilbakeføring gjennomføres.
            return;
        }
        oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, tidligereStegType, startStatusForNyttSteg,
                BehandlingStegStatus.TILBAKEFØRT, false);
    }

    protected void doTilbakeføringTilTidligsteAksjonspunkt(Behandling behandling, BehandlingStegType stegType, BehandlingModell modell,
                                                           Collection<String> oppdaterteAksjonspunkter, boolean erOverstyring) {
        Consumer<BehandlingStegType> oppdaterBehandlingStegStatus = (bst) -> {
            Optional<BehandlingStegStatus> stegStatus = modell.finnStegStatusFor(bst, oppdaterteAksjonspunkter);
            if (stegStatus.isPresent()
                    && !(Objects.equals(stegStatus.get(), behandling.getBehandlingStegStatus())
                    && Objects.equals(bst, behandling.getAktivtBehandlingSteg()))) {
                // er på starten av steg med endret aksjonspunkt. Ikke kjør steget her, kun oppdater
                oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, bst, stegStatus.get(),
                        BehandlingStegStatus.TILBAKEFØRT, erOverstyring);
            }
        };

        BehandlingStegModell førsteAksjonspunktSteg = modell
                .finnTidligsteStegForAksjonspunktDefinisjon(oppdaterteAksjonspunkter);

        BehandlingStegType aksjonspunktStegType = førsteAksjonspunktSteg == null ? null
                : førsteAksjonspunktSteg.getBehandlingStegType();

        if (Objects.equals(stegType, aksjonspunktStegType)) {
            // samme steg, kan ha ny BehandlingStegStatus
            oppdaterBehandlingStegStatus.accept(stegType);
        } else {
            // tilbakeføring til tidligere steg
            BehandlingStegModell revidertStegType = modell.finnFørsteSteg(stegType, aksjonspunktStegType);
            oppdaterBehandlingStegStatus.accept(revidertStegType.getBehandlingStegType());
        }
    }

    protected void fireEventBehandlingStegOvergang(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                   Optional<BehandlingStegTilstand> forrigeTilstand, Optional<BehandlingStegTilstand> nyTilstand, boolean erOverstyring) {
        BehandlingModell modell = getModell(behandling);
        BehandlingStegOvergangEvent event = BehandlingModellImpl.nyBehandlingStegOvergangEvent(modell, forrigeTilstand, nyTilstand,
                kontekst, erOverstyring);
        getEventPubliserer().fireEvent(event);
    }

    private void fireEventBehandlingStegTilstandEndring(BehandlingskontrollKontekst kontekst,
                                                        Optional<BehandlingStegTilstand> stegFør, Optional<BehandlingStegTilstand> stegEtter) {
        BehandlingStegTilstandEndringEvent event = new BehandlingStegTilstandEndringEvent(kontekst, stegFør);
        event.setNyTilstand(stegEtter);
        getEventPubliserer().fireEvent(event);
    }

    protected void oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(Behandling behandling, BehandlingStegType revidertStegType,
                                                                                           BehandlingStegStatus behandlingStegStatus,
                                                                                           BehandlingStegStatus sluttStatusForAndreÅpneSteg,
                                                                                           boolean erOverstyring) {
        oppdaterEksisterendeBehandling(behandling,
                (beh) -> manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, revidertStegType, behandlingStegStatus,
                        sluttStatusForAndreÅpneSteg), erOverstyring);
    }

    protected Behandling hentBehandling(BehandlingskontrollKontekst kontekst) {
        Objects.requireNonNull(kontekst, "kontekst"); //$NON-NLS-1$
        Long behandlingId = kontekst.getBehandlingId();
        return behandlingRepository.hentBehandling(behandlingId);
    }

    protected BehandlingskontrollEventPubliserer getEventPubliserer() {
        return eventPubliserer;
    }

    protected BehandlingModell getModell(Behandling behandling) {
        return behandlingModellRepository.getModell(behandling.getType());
    }

    private void fyrEventBehandlingskontrollException(BehandlingskontrollKontekst kontekst, BehandlingModell modell, RuntimeException e) {
        Behandling behandling = hentBehandling(kontekst);
        BehandlingskontrollEvent.ExceptionEvent stoppetEvent = new BehandlingskontrollEvent.ExceptionEvent(kontekst, modell, behandling.getAktivtBehandlingSteg(), behandling.getBehandlingStegStatus(), e);
        eventPubliserer.fireEvent(stoppetEvent);
    }

    private void fyrEventBehandlingskontrollStoppet(BehandlingskontrollKontekst kontekst, BehandlingModell modell, BehandlingStegUtfall stegUtfall) {
        Behandling behandling = hentBehandling(kontekst);
        BehandlingskontrollEvent event;
        if (behandling.erAvsluttet()) {
            event = new BehandlingskontrollEvent.AvsluttetEvent(kontekst, modell, behandling.getAktivtBehandlingSteg(), behandling.getBehandlingStegStatus());
        } else if (stegUtfall == null) {
            event = new BehandlingskontrollEvent.StoppetEvent(kontekst, modell, behandling.getSisteBehandlingStegTilstand().map(BehandlingStegTilstand::getBehandlingSteg).orElse(null), null);
        } else {
            event = new BehandlingskontrollEvent.StoppetEvent(kontekst, modell, stegUtfall.getBehandlingStegType(), stegUtfall.getResultat());
        }
        eventPubliserer.fireEvent(event);
    }

    private void fyrEventBehandlingskontrollStartet(BehandlingskontrollKontekst kontekst, BehandlingModell modell) {
        Behandling behandling = hentBehandling(kontekst);
        BehandlingskontrollEvent.StartetEvent startetEvent = new BehandlingskontrollEvent.StartetEvent(kontekst, modell, behandling.getAktivtBehandlingSteg(), behandling.getBehandlingStegStatus());
        eventPubliserer.fireEvent(startetEvent);
    }

    private void oppdaterEksisterendeBehandling(Behandling behandling,
                                                Consumer<Behandling> behandlingOppdaterer, boolean erOverstyring) {

        BehandlingStatus statusFør = behandling.getStatus();
        Optional<BehandlingStegTilstand> stegFør = behandling.getBehandlingStegTilstand();

        // Oppdater behandling og lagre
        behandlingOppdaterer.accept(behandling);
        BehandlingLås skriveLås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), skriveLås);
        behandlingRepository.lagre(behandling, skriveLås);

        // Publiser oppdatering
        BehandlingStatus statusEtter = behandling.getStatus();
        Optional<BehandlingStegTilstand> stegEtter = behandling.getBehandlingStegTilstand();
        fireEventBehandlingStegOvergang(kontekst, behandling, stegFør, stegEtter, erOverstyring);
        fireEventBehandlingStegTilstandEndring(kontekst, stegFør, stegEtter);
        eventPubliserer.fireEvent(kontekst, statusFør, statusEtter);
    }


    public void fremoverTransisjon(TransisjonIdentifikator transisjonId, BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Optional<BehandlingStegTilstand> stegTilstandFør = behandling.getBehandlingStegTilstand();
        BehandlingStegType fraSteg = behandling.getAktivtBehandlingSteg();

        // Flytt behandlingssteg-peker fremover
        BehandlingModell modell = getModell(behandling);
        StegTransisjon transisjon = modell.finnTransisjon(transisjonId);
        BehandlingStegModell fraStegModell = modell.finnSteg(fraSteg);
        BehandlingStegModell tilStegModell = transisjon.nesteSteg(fraStegModell);
        BehandlingStegType tilSteg = tilStegModell.getBehandlingStegType();

        behandlingFramføringTilSenereBehandlingSteg(kontekst, tilSteg);

        // Publiser tranisjonsevent (eventobserver(e) håndterer tilhørende tranisjonsregler)
        BehandlingTransisjonEvent event = new BehandlingTransisjonEvent(kontekst, transisjonId, stegTilstandFør, tilSteg, transisjon.erFremoverhopp());
        eventPubliserer.fireEvent(event);
    }

    public boolean inneholderSteg(Behandling behandling, BehandlingStegType behandlingStegType) {
        BehandlingModell modell = getModell(behandling);
        return modell.hvertSteg()
            .anyMatch(steg -> steg.getBehandlingStegType().equals(behandlingStegType));
    }

    private BehandlingStegKonfigurasjon getStatusKonfigurasjon() {
        if (behandlingStegKonfigurasjon == null) {
            behandlingStegKonfigurasjon = behandlingModellRepository.getBehandlingStegKonfigurasjon();
        }
        return behandlingStegKonfigurasjon;
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
        } else {
            BehandlingStegType førsteSteg = modell.finnFørsteSteg(inneværendeSteg, forventetTidligereSteg).getBehandlingStegType();
            return Objects.equals(forventetTidligereSteg, førsteSteg);
        }
    }

    private Behandling opprettNyFraTidligereBehandling(Behandling gammelBehandling, BehandlingType behandlingType,
                                                       Consumer<Behandling> behandlingOppdaterer) {
        // ta skrive lås på gammel behandling før vi gjør noe annet
        initBehandlingskontroll(gammelBehandling);

        Behandling nyBehandling = behandlingRepository.opprettNyBehandlingBasertPåTidligere(gammelBehandling, behandlingType);
        behandlingOppdaterer.accept(nyBehandling);

        BehandlingskontrollKontekst kontekst = this.initBehandlingskontroll(nyBehandling);
        this.opprettBehandling(kontekst, nyBehandling);

        return nyBehandling;
    }

    /** Oppdaterer behandling. (lagrer) */
    public void oppdaterBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    private void validerOgFlaggStartetProsessering() {
        if (nøstetProsseringGuard.get()) {
            throw new IllegalStateException("Støtter ikke nøstet prosessering i " + getClass().getSimpleName());
        } else {
            nøstetProsseringGuard.set(true);
        }
    }

    private void ferdigProsessering() {
        nøstetProsseringGuard.set(false);
    }

}
