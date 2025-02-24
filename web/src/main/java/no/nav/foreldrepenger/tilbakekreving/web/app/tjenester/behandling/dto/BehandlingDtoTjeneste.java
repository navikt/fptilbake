package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.*;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.TotrinnskontrollAksjonspunkterTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDtoMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.beregningsresultat.TilbakekrevingResultatRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument.DokumentRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling.FeilutbetalingÅrsakRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeBehandlingsmenyEnum;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.dto.NyVergeDto;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

import static no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLinks.get;
import static no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLinks.post;


/**
 * Bygger BehandlingDto og UtvidetBehandlingDto
 * Samler sammen informasjon fra ulike tjenester.
 */
@ApplicationScoped
public class BehandlingDtoTjeneste {

    private static final String DATO_PATTERN = "yyyy-MM-dd";
    private static final String FORELDELSE = "perioderForeldelse";
    private static final long OPPRETTELSE_DAGER_BEGRENSNING = 6L;

    private static final Fagsystem FAGSYSTEM = ApplicationName.hvilkenTilbake();

    private BehandlingTjeneste behandlingTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private VergeRepository vergeRepository;
    private BehandlingresultatRepository behandlingresultatRepository;

    private BehandlingModellRepository behandlingModellRepository;

    public BehandlingDtoTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingDtoTjeneste(BehandlingTjeneste behandlingTjeneste,
                                 TotrinnTjeneste totrinnTjeneste,
                                 TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste,
                                 HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                 VurdertForeldelseTjeneste vurdertForeldelseTjeneste,
                                 BeregningsresultatTjeneste beregningsresultatTjeneste,
                                 BehandlingRepositoryProvider repositoryProvider,
                                 BehandlingModellRepository behandlingModellRepository) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.behandlingresultatRepository = repositoryProvider.getBehandlingresultatRepository();
        this.behandlingModellRepository = behandlingModellRepository;
        this.totrinnTjeneste = totrinnTjeneste;
        this.totrinnskontrollTjeneste = totrinnskontrollTjeneste;
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
    }

    public List<BehandlingDto> hentAlleBehandlinger(Saksnummer saksnummer) {
        var behandlinger = behandlingTjeneste.hentBehandlinger(saksnummer);

        return behandlinger.stream()
                .map(this::lagBehandlingDto)
                .toList();
    }

    private BehandlingDto lagBehandlingDto(Behandling behandling) {
        var uuid = behandling.getUuid();
        var uuidDto = new UuidDto(uuid);
        var dto = new BehandlingDto();
        settStandardFelter(behandling, dto);
        dto.setBehandlingsresultat(lagBehandlingsresultat(behandling));

        // Behandlingsmeny-operasjoner
        dto.leggTil(get(BehandlingRestTjeneste.BASE_PATH + "/handling-rettigheter", "handling-rettigheter", uuidDto));
        dto.leggTil(get(BehandlingRestTjeneste.BEHANDLING_RETTIGHETER_PATH, "behandling-rettigheter", uuidDto));
        // Denne håndteres litt spesielt i frontend, så må gjøres på denne måten
        dto.leggTil(get("/verge/behandlingsmeny?uuid=" + uuid, "finn-menyvalg-for-verge"));

        // Totrinnsbehandling
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.leggTil(get(TotrinnskontrollRestTjeneste.BASE_PATH + "/arsaker", "totrinnskontroll-arsaker", uuidDto));
            dto.leggTil(post(AksjonspunktRestTjeneste.AKSJONSPUNKT_BESLUTT_PATH, "bekreft-totrinnsaksjonspunkt"));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.leggTil(get(TotrinnskontrollRestTjeneste.BASE_PATH + "/arsaker_read_only", "totrinnskontroll-arsaker-readOnly", uuidDto));
        }

        dto.leggTil(get(BrevRestTjeneste.BASE_PATH + "/maler", "brev-maler", uuidDto));
        dto.leggTil(post(BrevRestTjeneste.BASE_PATH + "/bestill", "brev-bestill"));
        dto.leggTil(post(BrevRestTjeneste.BASE_PATH + "/forhandsvis", "brev-forhandvis"));
        return dto;
    }

    private BehandlingsresultatDto lagBehandlingsresultat(Behandling behandling) {
        var dto = new BehandlingsresultatDto();
        if (behandling.erAvsluttet()) {
            var behandlingResultatData = behandlingresultatRepository.hent(behandling);
            behandlingResultatData.ifPresent(value -> {
                if (value.erBehandlingHenlagt()) {
                    dto.setType(BehandlingResultatType.HENLAGT);
                } else {
                    dto.setType(value.getBehandlingResultatType());
                }
            });
        }
        return dto;
    }

    private BehandlingsresultatDto lagUtledetBehandlingsresultat(Behandling behandling) {
        if (behandling.erAvsluttet()) {
            return lagBehandlingsresultat(behandling);
        }
        var dto = new BehandlingsresultatDto();
        var type = behandlingresultatRepository.hent(behandling)
                .map(Behandlingsresultat::getBehandlingResultatType)
                .or(() -> Optional.ofNullable(beregningsresultatTjeneste.finnEllerBeregn(behandling.getId()))
                        .map(BeregningResultat::getVedtakResultatType)
                        .map(BehandlingResultatType::fraVedtakResultatType))
                .orElse(BehandlingResultatType.IKKE_FASTSATT);
        dto.setType(type);
        return dto;
    }

    public UtvidetBehandlingDto hentUtvidetBehandlingResultat(long behandlingId, AsyncPollingStatus taskStatus) {
        var behandling = behandlingTjeneste.hentBehandling(behandlingId);

        var dto = new UtvidetBehandlingDto();
        settStandardFelter(behandling, dto);
        dto.setBehandlingsresultat(lagBehandlingsresultat(behandling));
        var behandlingHenlagt = behandlingTjeneste.erBehandlingHenlagt(behandling);
        dto.setBehandlingHenlagt(behandlingHenlagt);
        var aksjonspunkt = AksjonspunktDtoMapper.lagAksjonspunktDto(behandling, totrinnTjeneste.hentTotrinnsvurderinger(behandling));
        dto.setAksjonspunktene(aksjonspunkt);
        dto.setAksjonspunkt(aksjonspunkt);

        settResourceLinks(behandling, dto, behandlingHenlagt);

        if (taskStatus != null && !taskStatus.isPending()) {
            dto.setTaskStatus(taskStatus);
        }
        return dto;
    }

    private void settStandardFelter(Behandling behandling, BehandlingDto dto) {
        dto.setFagsakId(behandling.getFagsakId());
        dto.setId(behandling.getId());
        dto.setUuid(behandling.getUuid());
        dto.setVersjon(behandling.getVersjon());
        dto.setType(behandling.getType());
        dto.setOpprettet(behandling.getOpprettetTidspunkt());
        dto.setEndret(behandling.getEndretTidspunkt());
        dto.setAvsluttet(behandling.getAvsluttetDato());
        dto.setStatus(behandling.getStatus());
        dto.setToTrinnsBehandling(true);
        dto.setBehandlendeEnhetId(behandling.getBehandlendeEnhetId());
        dto.setBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn());
        dto.setBehandlingPåVent(behandling.isBehandlingPåVent());
        getFristDatoBehandlingPåVent(behandling).ifPresent(dto::setFristBehandlingPåVent);
        getVenteÅrsak(behandling).ifPresent(dto::setVenteÅrsakKode);
        dto.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        dto.setSpråkkode(behandling.getFagsak().getNavBruker().getSpråkkode());

        dto.setFørsteÅrsak(førsteÅrsak(behandling).orElse(null));
        dto.setBehandlingÅrsaker(lagBehandlingÅrsakDto(behandling));
        dto.setKanHenleggeBehandling(kanHenleggeBehandling(behandling));
        var vergeInfo = vergeRepository.finnVergeInformasjon(behandling.getId());
        vergeInfo.ifPresent(verge -> dto.setHarVerge(true));
        dto.setBehandlingTillatteOperasjoner(lovligeOperasjoner(behandling, vergeInfo.isPresent()));
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.setTotrinnskontrollReadonly(false);
            dto.setTotrinnskontrollÅrsaker(totrinnskontrollTjeneste.hentTotrinnsSkjermlenkeContext(behandling));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.setTotrinnskontrollReadonly(true);
            dto.setTotrinnskontrollÅrsaker(totrinnskontrollTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling));
        }
    }

    private BehandlingOperasjonerDto lovligeOperasjoner(Behandling b, boolean finnesVerge) {
        if (b.erSaksbehandlingAvsluttet()) {
            return BehandlingOperasjonerDto.builder(b.getUuid()).build(); // Skal ikke foreta menyvalg lenger
        } else if (BehandlingStatus.FATTER_VEDTAK.equals(b.getStatus())) {
            var tilgokjenning = b.getAnsvarligSaksbehandler() != null &&
                    !b.getAnsvarligSaksbehandler().equalsIgnoreCase(KontekstHolder.getKontekst().getUid());
            return BehandlingOperasjonerDto.builder(b.getUuid()).medTilGodkjenning(tilgokjenning).build();
        } else {
            var totrinnRetur = totrinnTjeneste.hentTotrinnsvurderinger(b).stream().anyMatch(tt -> !tt.isGodkjent());
            return BehandlingOperasjonerDto.builder(b.getUuid())
                    .medTilGodkjenning(false)
                    .medFraBeslutter(!b.isBehandlingPåVent() && totrinnRetur)
                    .medKanBytteEnhet(true)
                    .medKanHenlegges(henleggBehandlingTjeneste.kanHenleggeBehandlingManuelt(b))
                    .medKanSettesPaVent(!b.isBehandlingPåVent())
                    .medKanGjenopptas(b.isBehandlingPåVent())
                    .medKanOpnesForEndringer(false)
                    .medKanSendeMelding(Fagsystem.FPTILBAKE.equals(FAGSYSTEM) || !b.isBehandlingPåVent())
                    .medVergemeny(viseVerge(b, finnesVerge))
                    .build();
        }
    }

    private VergeBehandlingsmenyEnum viseVerge(Behandling behandling, boolean finnesVerge) {
        var kanBehandlingEndres = !behandling.erSaksbehandlingAvsluttet() && !behandling.isBehandlingPåVent();
        if (kanBehandlingEndres) {
            return finnesVerge ? VergeBehandlingsmenyEnum.FJERN : VergeBehandlingsmenyEnum.OPPRETT;
        }
        return VergeBehandlingsmenyEnum.SKJUL;
    }

    private List<BehandlingÅrsakDto> lagBehandlingÅrsakDto(Behandling behandling) {
        if (!behandling.getBehandlingÅrsaker().isEmpty()) {
            return behandling.getBehandlingÅrsaker().stream().map(this::lagBehandlingÅrsakDto).toList();
        }
        return Collections.emptyList();
    }

    private Optional<BehandlingÅrsakDto> førsteÅrsak(Behandling behandling) {
        return behandling.getBehandlingÅrsaker().stream()
                .sorted(Comparator.comparing(BaseEntitet::getOpprettetTidspunkt))
                .map(this::lagBehandlingÅrsakDto)
                .findFirst();
    }

    private BehandlingÅrsakDto lagBehandlingÅrsakDto(BehandlingÅrsak behandlingÅrsak) {
        var dto = new BehandlingÅrsakDto();
        dto.setBehandlingÅrsakType(behandlingÅrsak.getBehandlingÅrsakType());
        return dto;
    }

    private boolean kanHenleggeBehandling(Behandling behandling) {
        return !behandling.erAvsluttet() && (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType()) ||
                (erBehandlingOpprettetAutomatiskFørBestemteDager(behandling) && !grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())));
    }

    private boolean erBehandlingOpprettetAutomatiskFørBestemteDager(Behandling behandling) {
        return !behandling.isManueltOpprettet() && behandling.getOpprettetTidspunkt().isBefore(
                LocalDate.now().atStartOfDay().minusDays(OPPRETTELSE_DAGER_BEGRENSNING));
    }

    private void leggTilLenkerForBehandlingsoperasjoner(BehandlingDto dto) {
        dto.leggTil(post(BehandlingRestTjeneste.BASE_PATH + "/bytt-enhet", "bytt-behandlende-enhet"));
        dto.leggTil(post(BehandlingRestTjeneste.BASE_PATH + "/henlegg", "henlegg-behandling"));
        dto.leggTil(post(BehandlingRestTjeneste.BASE_PATH + "/gjenoppta", "gjenoppta-behandling"));
        dto.leggTil(post(BehandlingRestTjeneste.BASE_PATH + "/sett-pa-vent", "sett-behandling-pa-vent"));
        dto.leggTil(post(BehandlingRestTjeneste.BASE_PATH + "/endre-pa-vent", "endre-pa-vent"));

        dto.leggTil(post(AksjonspunktRestTjeneste.AKSJONSPUNKT_PATH, "lagre-aksjonspunkter"));

        dto.leggTil(post(ForeldelseRestTjeneste.BASE_PATH + "/belop", "beregne-feilutbetalt-belop"));
        if (!BehandlingStatus.AVSLUTTET.equals(dto.getStatus()) && !dto.isBehandlingPåVent()) {
            dto.leggTil(post(VergeRestTjeneste.BASE_PATH + "/opprett", "opprett-verge"));
            dto.leggTil(post(VergeRestTjeneste.BASE_PATH + "/fjern", "fjern-verge"));
        }

        if (ApplicationName.hvilkenTilbake().equals(Fagsystem.FPTILBAKE)){
            var uuidDto = new UuidDto(dto.getUuid());
            if (vergeRepository.finnesVerge(dto.getId())) {
                dto.leggTil(get(VergeRestTjeneste.BASE_PATH, "verge-hent", uuidDto));
                dto.leggTil(post(VergeRestTjeneste.VERGE_FJERN_PATH, "verge-fjern", null, uuidDto));
            } else {
                dto.leggTil(post(VergeRestTjeneste.VERGE_OPPRETT_PATH, "verge-opprett", new NyVergeDto(), uuidDto));
            }
        }
    }

    private void settResourceLinks(Behandling behandling, UtvidetBehandlingDto dto, boolean behandlingHenlagt) {
        var behandlingId = behandling.getId();
        var uuidDto = new UuidDto(behandling.getUuid());
        var behandlingModell = behandlingModellRepository.getModell(behandling.getType());
        var bst = behandling.getAktivtBehandlingSteg();

        var iEllerEtterForeslåVedtakSteg = bst == null || !behandlingModell.erStegAFørStegB(bst, BehandlingStegType.FORESLÅ_VEDTAK);
        var iVilkårSteg = BehandlingStegType.VTILBSTEG.equals(bst);
        var harDataForFaktaFeilutbetaling = faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(behandlingId);
        var harVurdertForeldelse = vurdertForeldelseTjeneste.harVurdertForeldelse(behandlingId);
        var harDataForVilkårsvurdering = vilkårsvurderingRepository.harDataForVilkårsvurdering(behandlingId);
        var harVergeAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_VERGE).isPresent();
        leggTilLenkerForBehandlingsoperasjoner(dto);

        dto.leggTil(get(AksjonspunktRestTjeneste.AKSJONSPUNKT_PATH, "aksjonspunkter", uuidDto));
        if (BehandlingStegType.FAKTA_FEILUTBETALING.equals(bst) || harDataForFaktaFeilutbetaling) {
            dto.leggTil(get(FeilutbetalingÅrsakRestTjeneste.BASE_PATH, "feilutbetalingAarsak"));
            dto.leggTil(get(BehandlingFaktaRestTjeneste.BASE_PATH + "/hent-fakta/feilutbetaling", "feilutbetalingFakta", uuidDto));
        }

        //FIXME det er i beste fall forvirrende å returnere både resultat og perioder som skal vurderes på samme navn "perioderForeldelse". Bør splittes tilsvarende hvordan det er for vilkårsvurdering
        if (harVurdertForeldelse) {
            dto.leggTil(get(ForeldelseRestTjeneste.BASE_PATH + "/vurdert", FORELDELSE, uuidDto));
        } else if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(get(ForeldelseRestTjeneste.BASE_PATH, FORELDELSE, uuidDto));
        }
        if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(get(VilkårsvurderingRestTjeneste.BASE_PATH + "/perioder", "vilkarvurderingsperioder", uuidDto));
        }
        if (iVilkårSteg || harDataForVilkårsvurdering) {
            dto.leggTil(get(VilkårsvurderingRestTjeneste.BASE_PATH + "/vurdert", "vilkarvurdering", uuidDto));
        }
        if (iEllerEtterForeslåVedtakSteg && !behandlingHenlagt) {
            dto.setBehandlingsresultat(lagUtledetBehandlingsresultat(behandling));
            dto.leggTil(get(TilbakekrevingResultatRestTjeneste.BASE_PATH + "/resultat", "beregningsresultat", uuidDto));
            dto.leggTil(get(DokumentRestTjeneste.BASE_PATH + "/hent-vedtaksbrev", "vedtaksbrev", uuidDto));
        }
        if (harVergeAksjonspunkt) {
            dto.leggTil(get(VergeRestTjeneste.BASE_PATH, "soeker-verge", uuidDto));
        }

    }

    private Optional<String> getVenteÅrsak(Behandling behandling) {
        var venteårsak = behandling.getVenteårsak();
        if (venteårsak != null) {
            return Optional.of(venteårsak.getKode());
        }
        return Optional.empty();
    }

    private Optional<String> getFristDatoBehandlingPåVent(Behandling behandling) {
        var frist = behandling.getFristDatoBehandlingPåVent();
        if (frist != null) {
            return Optional.of(frist.format(DateTimeFormatter.ofPattern(DATO_PATTERN)));
        }
        return Optional.empty();
    }
}
