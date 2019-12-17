package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
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

    private BehandlingModellRepository behandlingModellRepository;

    public BehandlingDtoTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingDtoTjeneste(BehandlingTjeneste behandlingTjeneste,
                                 VurdertForeldelseTjeneste vurdertForeldelseTjeneste,
                                 FaktaFeilutbetalingRepository faktaFeilutbetalingRepository,
                                 VilkårsvurderingRepository vilkårsvurderingRepository,
                                 BehandlingModellRepository behandlingModellRepository) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.vilkårsvurderingRepository = vilkårsvurderingRepository;
        this.behandlingModellRepository = behandlingModellRepository;
    }

    public List<BehandlingDto> hentAlleBehandlinger(Saksnummer saksnummer) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(saksnummer);

        return behandlinger.stream()
            .map(this::lagBehandlingDto)
            .collect(Collectors.toList());
    }

    private BehandlingDto lagBehandlingDto(Behandling behandling) {
        BehandlingDto dto = new BehandlingDto();
        settStandardFelter(behandling, dto);

        // Behandlingsmeny-operasjoner
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/handling-rettigheter?behandlingId=" + behandling.getId(), "handling-rettigheter", ResourceLink.HttpMethod.GET));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/bytt-enhet", "bytt-behandlende-enhet", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/opne-for-endringer", "opne-for-endringer", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/henlegg", "henlegg-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/gjenoppta", "gjenoppta-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/sett-pa-vent", "sett-behandling-pa-vent", ResourceLink.HttpMethod.POST));

        // Totrinnsbehandling
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/totrinnskontroll/arsaker?behandlingId=" + behandling.getId(), "totrinnskontroll-arsaker", ResourceLink.HttpMethod.GET));
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/aksjonspunkt", "bekreft-totrinnsaksjonspunkt", ResourceLink.HttpMethod.POST));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/totrinnskontroll/arsaker_read_only?behandlingId=" + behandling.getId(), "totrinnskontroll-arsaker-readOnly", ResourceLink.HttpMethod.GET));
        }

        dto.leggTil(new ResourceLink("/fptilbake/api/brev/maler?behandlingId=" + behandling.getId(), "brev-maler", ResourceLink.HttpMethod.GET));
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
    }

    private void settResourceLinks(Behandling behandling, UtvidetBehandlingDto dto, boolean behandlingHenlagt) {
        Long behandlingId = behandling.getId();
        BehandlingModell behandlingModell = behandlingModellRepository.getModell(behandling.getType());
        BehandlingStegType bst = behandling.getAktivtBehandlingSteg();

        boolean iEllerEtterForeslåVedtakSteg = bst == null || !behandlingModell.erStegAFørStegB(bst, BehandlingStegType.FORESLÅ_VEDTAK);
        boolean iVilkårSteg = BehandlingStegType.VTILBSTEG.equals(bst);
        boolean harDataForFaktaFeilutbetaling = faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(behandlingId);
        boolean harVurdertForeldelse = vurdertForeldelseTjeneste.harVurdertForeldelse(behandlingId);
        boolean harDataForVilkårsvurdering = vilkårsvurderingRepository.harDataForVilkårsvurdering(behandlingId);

        dto.leggTil(ResourceLink.get("/fptilbake/api/behandling/aksjonspunkt?behandlingId=" + behandlingId, "aksjonspunkter", null));
        if (BehandlingStegType.FAKTA_FEILUTBETALING.equals(bst) || harDataForFaktaFeilutbetaling) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/feilutbetalingaarsak", "feilutbetalingAarsak", null));
            dto.leggTil(ResourceLink.get("/fptilbake/api/behandlingfakta/hent-fakta/feilutbetaling?behandlingId=" + (long) behandlingId, "feilutbetalingFakta", null));
        }

        //FIXME det er i beste fall forvirrende å returnere både resultat og perioder som skal vurderes på samme navn "perioderForeldelse". Bør splittes tilsvarende hvordan det er for vilkårsvurdering
        if (harVurdertForeldelse) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/foreldelse/vurdert?behandlingId=" + behandlingId, FORELDELSE, null));
        } else if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/foreldelse?behandlingId=" + behandlingId, FORELDELSE, null));
        }
        if (harDataForFaktaFeilutbetaling) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/vilkarsvurdering/perioder?behandlingId=" + behandlingId, "vilkarvurderingsperioder", null));
        }
        if (iVilkårSteg || harDataForVilkårsvurdering) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/vilkarsvurdering/vurdert?behandlingId=" + behandlingId, "vilkarvurdering", null));
        }
        if (iEllerEtterForeslåVedtakSteg && !behandlingHenlagt) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/beregning/resultat?behandlingId=" + behandlingId, "beregningsresultat", null));
            dto.leggTil(ResourceLink.post("/fptilbake/api/dokument/hent-vedtaksbrev?behandlingId=" + behandlingId, "vedtaksbrev", behandlingId));
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
