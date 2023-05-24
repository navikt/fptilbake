package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingUtil.bestemFristForBehandlingVent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VergeDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@ApplicationScoped
@Transactional
public class BehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BehandlingTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private VergeRepository vergeRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private FagsystemKlient fagsystemKlient;

    private Period defaultVentefrist;

    BehandlingTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                              BehandlingskontrollProvider behandlingskontrollProvider,
                              FagsakTjeneste fagsakTjeneste,
                              HistorikkinnslagTjeneste historikkinnslagTjeneste,
                              FagsystemKlient fagsystemKlient,
                              @KonfigVerdi("frist.brukerrespons.varsel") Period defaultVentefrist) {
        this.behandlingskontrollTjeneste = behandlingskontrollProvider.getBehandlingskontrollTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollProvider.getBehandlingskontrollAsynkTjeneste();
        this.fagsakTjeneste = fagsakTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.fagsystemKlient = fagsystemKlient;
        this.defaultVentefrist = defaultVentefrist;

        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = behandlingRepositoryProvider.getEksternBehandlingRepository();
        this.behandlingresultatRepository = behandlingRepositoryProvider.getBehandlingresultatRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.vergeRepository = behandlingRepositoryProvider.getVergeRepository();
    }

    public List<Behandling> hentBehandlinger(Saksnummer saksnummer) {
        return behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummer);
    }

    public void settBehandlingPaVent(Long behandlingsId, LocalDate frist, Venteårsak venteårsak) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING;
        doSetBehandlingPåVent(behandlingsId, aksjonspunktDefinisjon, frist, venteårsak);
    }

    public void endreBehandlingPåVent(Behandling behandling, LocalDate frist, Venteårsak venteårsak) {
        if (!behandling.isBehandlingPåVent()) {
            throw BehandlingFeil.kanIkkeEndreVentefristForBehandlingIkkePaVent(behandling.getId());
        }
        AksjonspunktDefinisjon aksjonspunktDefinisjon = behandling.getBehandlingPåVentAksjonspunktDefinisjon();
        doSetBehandlingPåVent(behandling.getId(), aksjonspunktDefinisjon, frist, venteårsak);
    }

    public Behandling hentBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    public Behandling hentBehandling(UUID behandlingUUId) {
        return behandlingRepository.hentBehandling(behandlingUUId);
    }

    public Long hentBehandlingId(UUID behandlingUUId) {
        return hentBehandling(behandlingUUId).getId();
    }

    public Behandling opprettKunBehandlingManuell(Saksnummer saksnummer, UUID eksternUuid,
                                                  FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {

        return opprettFørstegangsbehandling(saksnummer, eksternUuid, null, null, fagsakYtelseType, behandlingType);
    }

    public Long opprettBehandlingAutomatisk(Saksnummer saksnummer, UUID eksternUuid, Henvisning henvisning,
                                            AktørId aktørId, FagsakYtelseType fagsakYtelseType,
                                            BehandlingType behandlingType) {
        Behandling behandling = opprettFørstegangsbehandling(saksnummer, eksternUuid, henvisning, aktørId, fagsakYtelseType, behandlingType);
        var gruppe = behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
        return behandling.getId();
    }

    public void kanEndreBehandling(Long behandlingId, Long versjon) {
        Boolean kanEndreBehandling = behandlingRepository.erVersjonUendret(behandlingId, versjon);
        if (!kanEndreBehandling) {
            throw BehandlingFeil.endringerHarForekommetPåSøknaden();
        }
    }

    public void setAnsvarligSaksbehandlerFraKontekst(Behandling behandling) {
        var kontekst = KontekstHolder.getKontekst();
        var bruker = kontekst.getIdentType().erSystem() ? null : kontekst.getUid();
        if (bruker != null) {
            var lås = behandlingRepository.taSkriveLås(behandling);
            behandling.setAnsvarligSaksbehandler(bruker);
            behandlingRepository.lagre(behandling, lås);
        }
    }

    public boolean erBehandlingHenlagt(Behandling behandling) {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingresultatRepository.hent(behandling);
        return behandlingsresultat.isPresent() && behandlingsresultat.get().erBehandlingHenlagt();
    }

    public boolean kanOppretteBehandling(Saksnummer saksnummer, UUID eksternUuid) {
        return !(harÅpenBehandling(saksnummer) || finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(eksternUuid));
    }

    public void oppdaterBehandlingMedEksternReferanse(Saksnummer saksnummer, Henvisning henvisning, UUID eksternUuid) {
        List<Behandling> behandlinger = behandlingRepository.hentAlleBehandlingerForSaksnummer(saksnummer);
        if (behandlinger.isEmpty()) {
            throw BehandlingFeil.fantIngenTilbakekrevingBehandlingForSaksnummer(saksnummer);
        }
        Optional<Behandling> åpenTilbakekrevingBehandling = behandlinger.stream()
                .filter(behandling -> !behandling.erAvsluttet())
                .filter(behandling -> BehandlingType.TILBAKEKREVING.equals(behandling.getType()))
                .findAny();

        if (åpenTilbakekrevingBehandling.isPresent()) {
            Behandling behandling = åpenTilbakekrevingBehandling.get();
            EksternBehandling eksternBehandling = new EksternBehandling(behandling, henvisning, eksternUuid);
            eksternBehandlingRepository.lagre(eksternBehandling);
        }
    }

    public Optional<BehandlingVedtak> hentBehandlingvedtakForBehandlingId(long behandlingId) {
        return behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
    }

    private void doSetBehandlingPåVent(Long behandlingId, AksjonspunktDefinisjon apDef, LocalDate frist, Venteårsak venteårsak) {
        LocalDateTime fristTid = bestemFristForBehandlingVent(frist, defaultVentefrist);

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        BehandlingStegType behandlingStegFunnet = behandling.getAksjonspunktMedDefinisjonOptional(apDef)
                .map(Aksjonspunkt::getBehandlingStegFunnet)
                .orElse(null); // Dersom autopunkt ikke allerede er opprettet, så er det ikke tilknyttet steg
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, apDef, behandlingStegFunnet, fristTid, venteårsak);

    }

    private SamletEksternBehandlingInfo hentEksternBehandlingMedAktørId(UUID eksternUuid) {
        return fagsystemKlient.hentBehandlingsinfo(eksternUuid, Tillegsinformasjon.PERSONOPPLYSNINGER);
    }


    private Behandling opprettFørstegangsbehandling(Saksnummer saksnummer, UUID eksternUuid, Henvisning henvisning, AktørId aktørId, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {
        logger.info("Oppretter Tilbakekrevingbehandling for [saksnummer: {} ] for ekstern Uuid [ {} ]", saksnummer, eksternUuid);
        validateHarIkkeÅpenTilbakekrevingBehandling(saksnummer, eksternUuid);
        boolean manueltOpprettet = false;
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto;
        if (aktørId == null) {
            //FIXME trenger en mer intuitiv måte å skille manuell/automatisk opprettelse enn ved at aktørid er satt eller ikke.
            SamletEksternBehandlingInfo samletEksternBehandlingInfo = hentEksternBehandlingMedAktørId(eksternUuid);
            aktørId = samletEksternBehandlingInfo.getAktørId();
            eksternBehandlingsinfoDto = samletEksternBehandlingInfo.getGrunninformasjon();
            manueltOpprettet = true;
        } else {
            eksternBehandlingsinfoDto = fagsystemKlient.hentBehandling(eksternUuid);
        }
        henvisning = hentHenvisningHvisIkkeFinnes(henvisning, eksternBehandlingsinfoDto);

        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer, aktørId, fagsakYtelseType, eksternBehandlingsinfoDto.getSpråkkodeEllerDefault());

        Behandling behandling = Behandling.nyBehandlingFor(fagsak, behandlingType)
                .medManueltOpprettet(manueltOpprettet).build();
        OrganisasjonsEnhet organisasjonsEnhet = hentEnhetFraEksternBehandling(eksternBehandlingsinfoDto);
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, henvisning, eksternUuid);
        eksternBehandlingRepository.lagre(eksternBehandling);

        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling); // FIXME: sjekk om journalpostId skal hentes ///

        hentVergeInformasjonFraFpsak(behandling.getId());

        return behandling;
    }

    public OrganisasjonsEnhet hentEnhetForEksternBehandling(UUID eksternUuid) {
        return hentEnhetFraEksternBehandling(fagsystemKlient.hentBehandling(eksternUuid));
    }

    private OrganisasjonsEnhet hentEnhetFraEksternBehandling(EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        return new OrganisasjonsEnhet(eksternBehandlingsinfoDto.getBehandlendeEnhetId(), eksternBehandlingsinfoDto.getBehandlendeEnhetNavn());
    }


    private void validateHarIkkeÅpenTilbakekrevingBehandling(Saksnummer saksnummer, UUID eksternUuid) {
        if (harÅpenBehandling(saksnummer)) {
            throw BehandlingFeil.kanIkkeOppretteTilbakekrevingBehandling(saksnummer);
        }
        if (finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(eksternUuid)) {
            throw BehandlingFeil.kanIkkeOppretteTilbakekrevingBehandling(eksternUuid);
        }
    }

    private boolean harÅpenBehandling(Saksnummer saksnummer) {
        List<Behandling> behandlinger = hentBehandlinger(saksnummer);
        if (behandlinger.isEmpty()) {
            return false;
        }
        return behandlinger.stream()
                .anyMatch(behandling -> !behandling.erAvsluttet() && BehandlingType.TILBAKEKREVING.equals(behandling.getType()));
    }

    private boolean finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(UUID eksternUuid) {
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.finnForSisteAvsluttetTbkBehandling(eksternUuid);
        if (eksternBehandling.isPresent()) {
            Behandling behandling = behandlingRepository.hentBehandling(eksternBehandling.get().getInternId());
            return !erBehandlingHenlagt(behandling); //hvis behandlingen er henlagt,kan opprettes nye behandling
        }
        return false;
    }

    private Henvisning hentHenvisningHvisIkkeFinnes(Henvisning henvisning, EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        if (henvisning == null) {
            henvisning = eksternBehandlingsinfoDto.getHenvisning();
            if (henvisning == null) {
                throw new NullPointerException("Henvisning fra saksbehandlingsklienten var null");
            }
        }
        return henvisning;
    }

    //TODO verge bør flyttes til egen tjeneste, aller helst i eget 'hent fra saksbehandlingssystemet-steg'
    private void hentVergeInformasjonFraFpsak(long behandlingId) {
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        SamletEksternBehandlingInfo eksternBehandlingInfo = fagsystemKlient.hentBehandlingsinfo(eksternBehandling.getEksternUuid(), Tillegsinformasjon.VERGE);
        if (eksternBehandlingInfo.getVerge() != null) {
            lagreVergeInformasjon(behandlingId, eksternBehandlingInfo.getVerge());
        }
    }

    //TODO verge bør flyttes til egen tjeneste, aller helst i eget 'hent fra saksbehandlingssystemet-steg'
    private void lagreVergeInformasjon(long behandlingId, VergeDto vergeDto) {
        if (vergeDto.getGyldigTom().isBefore(LocalDate.now())) {
            logger.info("Verge informasjon er utløpt.Så kopierer ikke fra fpsak");
        } else {
            VergeEntitet.Builder builder = VergeEntitet.builder().medVergeType(vergeDto.getVergeType())
                    .medKilde(KildeType.FPSAK.name())
                    .medGyldigPeriode(vergeDto.getGyldigFom(), vergeDto.getGyldigTom())
                    .medNavn(vergeDto.getNavn())
                    .medBegrunnelse("");
            if (vergeDto.getOrganisasjonsnummer() != null && !vergeDto.getOrganisasjonsnummer().isEmpty()) {
                builder.medOrganisasjonnummer(vergeDto.getOrganisasjonsnummer());
            } else if (vergeDto.getFnr() != null && !vergeDto.getFnr().isEmpty()) {
                builder.medVergeAktørId(fagsakTjeneste.hentAktørForFnr(vergeDto.getFnr()));
            } else if (vergeDto.getAktoerId() != null && !vergeDto.getAktoerId().isEmpty()) {
                var aktørId = new AktørId(vergeDto.getAktoerId());
                builder.medVergeAktørId(aktørId);
                builder.medNavn(fagsakTjeneste.hentNavnForAktør(aktørId));
            }
            vergeRepository.lagreVergeInformasjon(behandlingId, builder.build());
        }
    }

}
