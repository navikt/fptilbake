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

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.BehandlingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;

/**
 * Bygger BehandlingDto og UtvidetBehandlingDto
 * Samler sammen informasjon fra ulike tjenester.
 */
@ApplicationScoped
public class BehandlingDtoTjeneste {

    private static final String DATO_PATTERN = "yyyy-MM-dd";
    private static final String FORELDELSE = "perioderForeldelse";

    private BehandlingTjeneste behandlingTjeneste;

    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private VergeRepository vergeRepository;

    private BehandlingModellRepository behandlingModellRepository;

    public BehandlingDtoTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingDtoTjeneste(BehandlingTjeneste behandlingTjeneste,
                                 VurdertForeldelseTjeneste vurdertForeldelseTjeneste,
                                 BehandlingRepositoryProvider repositoryProvider,
                                 BehandlingModellRepository behandlingModellRepository) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.behandlingModellRepository = behandlingModellRepository;
    }

    public List<BehandlingDto> hentAlleBehandlinger(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(saksnummer);

        return behandlinger.stream()
            .map(this::lagBehandlingDto)
            .collect(Collectors.toList());
    }

    private BehandlingDto lagBehandlingDto(Behandling behandling) {
        UUID behandlingUuid = behandling.getUuid();
        BehandlingDto dto = new BehandlingDto();
        settStandardFelter(behandling, dto);

        // Behandlingsmeny-operasjoner
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/handling-rettigheter?behandlingUuid=" + behandlingUuid, "handling-rettigheter", ResourceLink.HttpMethod.GET));
        dto.leggTil(new ResourceLink("/fptilbake/api/verge/behandlingsmeny", "finn-menyvalg-for-verge", ResourceLink.HttpMethod.GET));

        // Totrinnsbehandling
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/totrinnskontroll/arsaker?behandlingUuid=" + behandling.getId(), "totrinnskontroll-arsaker", ResourceLink.HttpMethod.GET));
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/aksjonspunkt", "bekreft-totrinnsaksjonspunkt", ResourceLink.HttpMethod.POST));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/totrinnskontroll/arsaker_read_only?behandlingUuid=" + behandling.getId(), "totrinnskontroll-arsaker-readOnly", ResourceLink.HttpMethod.GET));
        }

        dto.leggTil(new ResourceLink("/fptilbake/api/brev/maler?behandlingUuid=" + behandlingUuid, "brev-maler", ResourceLink.HttpMethod.GET));
        dto.leggTil(new ResourceLink("/fptilbake/api/brev/bestill", "brev-bestill", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/brev/forhandsvis", "brev-forhandvis", ResourceLink.HttpMethod.POST));
        return dto;
    }

    public UtvidetBehandlingDto hentUtvidetBehandlingResultat(long behandlingId, AsyncPollingStatus taskStatus) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();
        settStandardFelter(behandling, dto);
        boolean behandlingHenlagt = behandlingTjeneste.erBehandlingHenlagt(behandling);
        dto.setBehandlingHenlagt(behandlingHenlagt);

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
        vergeRepository.finnVergeInformasjon(behandling.getId()).ifPresent(verge -> dto.setHarVerge(true));
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
        return !behandling.erAvsluttet() && !grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId());
    }

    private static void leggTilLenkerForBehandlingsoperasjoner(BehandlingDto dto) {
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/bytt-enhet", "bytt-behandlende-enhet", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/opne-for-endringer", "opne-for-endringer", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/henlegg", "henlegg-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/gjenoppta", "gjenoppta-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/sett-pa-vent", "sett-behandling-pa-vent", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/endre-pa-vent", "endre-pa-vent", ResourceLink.HttpMethod.POST));

        dto.leggTil(new ResourceLink("/fptilbake/api/behandling/aksjonspunkt", "lagre-aksjonspunkter", ResourceLink.HttpMethod.POST));

        dto.leggTil(new ResourceLink("/fptilbake/api/foreldelse/belop", "beregne-feilutbetalt-belop", ResourceLink.HttpMethod.POST));
        if (!BehandlingStatus.AVSLUTTET.equals(dto.getStatus()) && !dto.isBehandlingPåVent()) {
            dto.leggTil(new ResourceLink("/fptilbake/api/verge/opprett", "opprett-verge", ResourceLink.HttpMethod.POST));
            dto.leggTil(new ResourceLink("/fptilbake/api/verge/fjern", "fjern-verge", ResourceLink.HttpMethod.POST));
        }
    }

    private void settResourceLinks(Behandling behandling, UtvidetBehandlingDto dto, boolean behandlingHenlagt) {
        Long behandlingId = behandling.getId();
        UUID behandlingUuid = behandling.getUuid();
        BehandlingModell behandlingModell = behandlingModellRepository.getModell(behandling.getType());
        BehandlingStegType bst = behandling.getAktivtBehandlingSteg();

        boolean iEllerEtterForeslåVedtakSteg = bst == null || !behandlingModell.erStegAFørStegB(bst, BehandlingStegType.FORESLÅ_VEDTAK);
        boolean iVilkårSteg = BehandlingStegType.VTILBSTEG.equals(bst);
        boolean harDataForFaktaFeilutbetaling = faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(behandlingId);
        boolean harVurdertForeldelse = vurdertForeldelseTjeneste.harVurdertForeldelse(behandlingId);
        boolean harDataForVilkårsvurdering = vilkårsvurderingRepository.harDataForVilkårsvurdering(behandlingId);
        boolean harVergeAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_VERGE).isPresent();
        leggTilLenkerForBehandlingsoperasjoner(dto);

        dto.leggTil(ResourceLink.get("/fptilbake/api/behandling/aksjonspunkt?behandlingUuid=" + behandlingUuid, "aksjonspunkter", null));
        if (BehandlingStegType.FAKTA_FEILUTBETALING.equals(bst) || harDataForFaktaFeilutbetaling) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/feilutbetalingaarsak", "feilutbetalingAarsak", null));
            dto.leggTil(ResourceLink.get("/fptilbake/api/behandlingfakta/hent-fakta/feilutbetaling?behandlingUuid=" + behandlingUuid, "feilutbetalingFakta", null));
        }

        //FIXME det er i beste fall forvirrende å returnere både resultat og perioder som skal vurderes på samme navn "perioderForeldelse". Bør splittes tilsvarende hvordan det er for vilkårsvurdering
        if (harVurdertForeldelse) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/foreldelse/vurdert?behandlingUuid=" + behandlingUuid, FORELDELSE, null));
        } else if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/foreldelse?behandlingUuid=" + behandlingUuid, FORELDELSE, null));
        }
        if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/vilkarsvurdering/perioder?behandlingUuid=" + behandlingUuid, "vilkarvurderingsperioder", null));
        }
        if (iVilkårSteg || harDataForVilkårsvurdering) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/vilkarsvurdering/vurdert?behandlingUuid=" + behandlingUuid, "vilkarvurdering", null));
        }
        if (iEllerEtterForeslåVedtakSteg && !behandlingHenlagt) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/beregning/resultat?behandlingUuid=" + behandlingUuid, "beregningsresultat", null));
            dto.leggTil(ResourceLink.get("/fptilbake/api/dokument/hent-vedtaksbrev?behandlingUuid=" + behandlingUuid, "vedtaksbrev", null));
        }
        if (harVergeAksjonspunkt) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/verge?behandlingId=" + behandlingId, "soeker-verge", null));
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

}
