package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.TotrinnskontrollAksjonspunkterTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDtoMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeBehandlingsmenyEnum;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * Bygger BehandlingDto og UtvidetBehandlingDto
 * Samler sammen informasjon fra ulike tjenester.
 */
@ApplicationScoped
public class BehandlingDtoTjeneste {

    private static final String DATO_PATTERN = "yyyy-MM-dd";
    private static final String FORELDELSE = "perioderForeldelse";
    private static final String AKSJONSPUNKT_API = "/api/behandling/aksjonspunkt";
    private static final long OPPRETTELSE_DAGER_BEGRENSNING = 6L;

    private BehandlingTjeneste behandlingTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste;
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private VergeRepository vergeRepository;
    private BehandlingresultatRepository behandlingresultatRepository;

    private BehandlingModellRepository behandlingModellRepository;

    private String kontekstPath;

    public BehandlingDtoTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingDtoTjeneste(BehandlingTjeneste behandlingTjeneste,
                                 TotrinnTjeneste totrinnTjeneste,
                                 TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste,
                                 HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                 VurdertForeldelseTjeneste vurdertForeldelseTjeneste,
                                 TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                                 BehandlingRepositoryProvider repositoryProvider,
                                 BehandlingModellRepository behandlingModellRepository) {
        this(behandlingTjeneste, totrinnTjeneste, totrinnskontrollTjeneste, henleggBehandlingTjeneste, vurdertForeldelseTjeneste,
            tilbakekrevingBeregningTjeneste, repositoryProvider, behandlingModellRepository, ApplicationName.hvilkenTilbake());
    }

    public BehandlingDtoTjeneste(BehandlingTjeneste behandlingTjeneste,
                                 TotrinnTjeneste totrinnTjeneste,
                                 TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste,
                                 HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                 VurdertForeldelseTjeneste vurdertForeldelseTjeneste,
                                 TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                                 BehandlingRepositoryProvider repositoryProvider,
                                 BehandlingModellRepository behandlingModellRepository,
                                 Fagsystem applikasjon) {
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
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;

        kontekstPath = switch (applikasjon) {
            case FPTILBAKE -> "/fptilbake";
            case K9TILBAKE -> "/k9/tilbake";
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        };
    }

    public List<BehandlingDto> hentAlleBehandlinger(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(saksnummer);

        return behandlinger.stream()
                .map(this::lagBehandlingDto)
                .collect(Collectors.toList());
    }

    private BehandlingDto lagBehandlingDto(Behandling behandling) {
        UUID uuid = behandling.getUuid();
        UuidDto uuidDto = new UuidDto(uuid);
        BehandlingDto dto = new BehandlingDto();
        settStandardFelter(behandling, dto);
        dto.setBehandlingsresultat(lagBehandlingsresultat(behandling));

        // Behandlingsmeny-operasjoner
        dto.leggTil(get(kontekstPath + "/api/behandlinger/handling-rettigheter", "handling-rettigheter", uuidDto));
        dto.leggTil(get(kontekstPath + "/api" + BehandlingRestTjeneste.BEHANDLING_RETTIGHETER_PATH, "behandling-rettigheter", uuidDto));
        // Denne håndteres litt spesielt i frontend, så må gjøres på denne måten
        dto.leggTil(get(kontekstPath + "/api/verge/behandlingsmeny?uuid=" + uuid, "finn-menyvalg-for-verge"));

        // Totrinnsbehandling
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.leggTil(get(kontekstPath + "/api/behandling/totrinnskontroll/arsaker", "totrinnskontroll-arsaker", uuidDto));
            dto.leggTil(new ResourceLink(kontekstPath + AKSJONSPUNKT_API, "bekreft-totrinnsaksjonspunkt", ResourceLink.HttpMethod.POST));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.leggTil(get(kontekstPath + "/api/behandling/totrinnskontroll/arsaker_read_only", "totrinnskontroll-arsaker-readOnly", uuidDto));
        }

        dto.leggTil(get(kontekstPath + "/api/brev/maler", "brev-maler", uuidDto));
        dto.leggTil(get(kontekstPath + "/api/brev/maler", "fpsak-brev-maler", uuidDto));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/brev/bestill", "brev-bestill", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/brev/forhandsvis", "brev-forhandvis", ResourceLink.HttpMethod.POST));
        return dto;
    }

    private BehandlingsresultatDto lagBehandlingsresultat(Behandling behandling) {
        BehandlingsresultatDto dto = new BehandlingsresultatDto();
        if (behandling.erAvsluttet()) {
            Optional<Behandlingsresultat> behandlingResultatData = behandlingresultatRepository.hent(behandling);
            behandlingResultatData.ifPresent(value -> {
                Behandlingsresultat behandlingsresultat = value;
                if (behandlingsresultat.erBehandlingHenlagt()) {
                    dto.setType(BehandlingResultatType.HENLAGT);
                } else {
                    dto.setType(behandlingsresultat.getBehandlingResultatType());
                }
            });
        }
        return dto;
    }

    private BehandlingsresultatDto lagUtledetBehandlingsresultat(Behandling behandling) {
        if (behandling.erAvsluttet()) {
            return lagBehandlingsresultat(behandling);
        }
        BehandlingsresultatDto dto = new BehandlingsresultatDto();
        var type = behandlingresultatRepository.hent(behandling)
            .map(Behandlingsresultat::getBehandlingResultatType)
            .orElseGet(() -> BehandlingResultatType.fraVedtakResultatType(tilbakekrevingBeregningTjeneste.beregn(behandling.getId()).getVedtakResultatType()));
        dto.setType(type);
        return dto;
    }

    public UtvidetBehandlingDto hentUtvidetBehandlingResultat(long behandlingId, AsyncPollingStatus taskStatus) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();
        settStandardFelter(behandling, dto);
        dto.setBehandlingsresultat(lagBehandlingsresultat(behandling));
        boolean behandlingHenlagt = behandlingTjeneste.erBehandlingHenlagt(behandling);
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
            boolean tilgokjenning = b.getAnsvarligSaksbehandler() != null && !b.getAnsvarligSaksbehandler().equalsIgnoreCase(SubjectHandler.getSubjectHandler().getUid());
            return BehandlingOperasjonerDto.builder(b.getUuid()).medTilGodkjenning(tilgokjenning).build();
        } else {
            boolean totrinnRetur = totrinnTjeneste.hentTotrinnsvurderinger(b).stream().anyMatch(tt -> !tt.isGodkjent());
            return BehandlingOperasjonerDto.builder(b.getUuid())
                .medTilGodkjenning(false)
                .medFraBeslutter(!b.isBehandlingPåVent() && totrinnRetur)
                .medKanBytteEnhet(true)
                .medKanHenlegges(henleggBehandlingTjeneste.kanHenleggeBehandlingManuelt(b))
                .medKanSettesPaVent(!b.isBehandlingPåVent())
                .medKanGjenopptas(b.isBehandlingPåVent())
                .medKanOpnesForEndringer(false)
                .medKanSendeMelding(!b.isBehandlingPåVent())
                .medVergemeny(viseVerge(b, finnesVerge))
                .build();
        }
    }

    private VergeBehandlingsmenyEnum viseVerge(Behandling behandling, boolean finnesVerge) {
        boolean kanBehandlingEndres = !behandling.erSaksbehandlingAvsluttet() && !behandling.isBehandlingPåVent();
        if (kanBehandlingEndres) {
            return finnesVerge ? VergeBehandlingsmenyEnum.FJERN : VergeBehandlingsmenyEnum.OPPRETT;
        }
        return VergeBehandlingsmenyEnum.SKJUL;
    }

    private List<BehandlingÅrsakDto> lagBehandlingÅrsakDto(Behandling behandling) {
        if (!behandling.getBehandlingÅrsaker().isEmpty()) {
            return behandling.getBehandlingÅrsaker().stream().map(this::lagBehandlingÅrsakDto).collect(Collectors.toList());
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
        BehandlingÅrsakDto dto = new BehandlingÅrsakDto();
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
        dto.leggTil(new ResourceLink(kontekstPath + "/api/behandlinger/bytt-enhet", "bytt-behandlende-enhet", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/behandlinger/opne-for-endringer", "opne-for-endringer", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/behandlinger/henlegg", "henlegg-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/behandlinger/gjenoppta", "gjenoppta-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/behandlinger/sett-pa-vent", "sett-behandling-pa-vent", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink(kontekstPath + "/api/behandlinger/endre-pa-vent", "endre-pa-vent", ResourceLink.HttpMethod.POST));

        dto.leggTil(new ResourceLink(kontekstPath + AKSJONSPUNKT_API, "lagre-aksjonspunkter", ResourceLink.HttpMethod.POST));

        dto.leggTil(new ResourceLink(kontekstPath + "/api/foreldelse/belop", "beregne-feilutbetalt-belop", ResourceLink.HttpMethod.POST));
        if (!BehandlingStatus.AVSLUTTET.equals(dto.getStatus()) && !dto.isBehandlingPåVent()) {
            dto.leggTil(new ResourceLink(kontekstPath + "/api/verge/opprett", "opprett-verge", ResourceLink.HttpMethod.POST));
            dto.leggTil(new ResourceLink(kontekstPath + "/api/verge/fjern", "fjern-verge", ResourceLink.HttpMethod.POST));
        }
    }

    private void settResourceLinks(Behandling behandling, UtvidetBehandlingDto dto, boolean behandlingHenlagt) {
        Long behandlingId = behandling.getId();
        UuidDto uuidDto = new UuidDto(behandling.getUuid());
        BehandlingModell behandlingModell = behandlingModellRepository.getModell(behandling.getType());
        BehandlingStegType bst = behandling.getAktivtBehandlingSteg();

        boolean iEllerEtterForeslåVedtakSteg = bst == null || !behandlingModell.erStegAFørStegB(bst, BehandlingStegType.FORESLÅ_VEDTAK);
        boolean iVilkårSteg = BehandlingStegType.VTILBSTEG.equals(bst);
        boolean harDataForFaktaFeilutbetaling = faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(behandlingId);
        boolean harVurdertForeldelse = vurdertForeldelseTjeneste.harVurdertForeldelse(behandlingId);
        boolean harDataForVilkårsvurdering = vilkårsvurderingRepository.harDataForVilkårsvurdering(behandlingId);
        boolean harVergeAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_VERGE).isPresent();
        leggTilLenkerForBehandlingsoperasjoner(dto);

        dto.leggTil(get(kontekstPath + AKSJONSPUNKT_API, "aksjonspunkter", uuidDto));
        if (BehandlingStegType.FAKTA_FEILUTBETALING.equals(bst) || harDataForFaktaFeilutbetaling) {
            dto.leggTil(get(kontekstPath + "/api/feilutbetalingaarsak", "feilutbetalingAarsak"));
            dto.leggTil(get(kontekstPath + "/api/behandlingfakta/hent-fakta/feilutbetaling", "feilutbetalingFakta", uuidDto));
        }

        //FIXME det er i beste fall forvirrende å returnere både resultat og perioder som skal vurderes på samme navn "perioderForeldelse". Bør splittes tilsvarende hvordan det er for vilkårsvurdering
        if (harVurdertForeldelse) {
            dto.leggTil(get(kontekstPath + "/api/foreldelse/vurdert", FORELDELSE, uuidDto));
            dto.leggTil(get(kontekstPath + "/api/foreldelse/vurdert", "perioderForeldelseVurdert", uuidDto));
        } else if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(get(kontekstPath + "/api/foreldelse", FORELDELSE, uuidDto));
        }
        if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(get(kontekstPath + "/api/vilkarsvurdering/perioder", "vilkarvurderingsperioder", uuidDto));
        }
        if (iVilkårSteg || harDataForVilkårsvurdering) {
            dto.leggTil(get(kontekstPath + "/api/vilkarsvurdering/vurdert", "vilkarvurdering", uuidDto));
        }
        if (iEllerEtterForeslåVedtakSteg && !behandlingHenlagt) {
            dto.setBehandlingsresultat(lagUtledetBehandlingsresultat(behandling));
            dto.leggTil(get(kontekstPath + "/api/beregning/resultat", "beregningsresultat", uuidDto));
            dto.leggTil(get(kontekstPath + "/api/dokument/hent-vedtaksbrev", "vedtaksbrev", uuidDto));
        }
        if (harVergeAksjonspunkt) {
            dto.leggTil(get(kontekstPath + "/api/verge", "soeker-verge", uuidDto));
        }

    }

    private Optional<String> getVenteÅrsak(Behandling behandling) {
        Venteårsak venteårsak = behandling.getVenteårsak();
        if (venteårsak != null) {
            return Optional.of(venteårsak.getKode());
        }
        return Optional.empty();
    }

    private Optional<String> getFristDatoBehandlingPåVent(Behandling behandling) {
        LocalDate frist = behandling.getFristDatoBehandlingPåVent();
        if (frist != null) {
            return Optional.of(frist.format(DateTimeFormatter.ofPattern(DATO_PATTERN)));
        }
        return Optional.empty();
    }

    private ResourceLink get(String url, String relasjon) {
        return get(url, relasjon, null);
    }

    private ResourceLink get(String url, String relasjon, Object requestPayload) {
        return ResourceLink.get(url, relasjon, requestPayload);
    }

}
