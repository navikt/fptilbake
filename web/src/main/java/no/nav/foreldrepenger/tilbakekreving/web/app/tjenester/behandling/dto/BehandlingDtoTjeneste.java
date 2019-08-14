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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
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

    public BehandlingDtoTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingDtoTjeneste(BehandlingTjeneste behandlingTjeneste, VurdertForeldelseTjeneste vurdertForeldelseTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
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
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/bytt-enhet", "bytt-behandlende-enhet", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/opne-for-endringer", "opne-for-endringer", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/henlegg", "henlegg-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/gjenoppta", "gjenoppta-behandling", ResourceLink.HttpMethod.POST));
        dto.leggTil(new ResourceLink("/fptilbake/api/behandlinger/sett-pa-vent", "sett-behandling-pa-vent", ResourceLink.HttpMethod.POST));

        // Totrinnsbehandling
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/totrinnskontroll/arsaker", "totrinnskontroll-arsaker", ResourceLink.HttpMethod.POST));
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/aksjonspunkt", "bekreft-totrinnsaksjonspunkt", ResourceLink.HttpMethod.POST));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.leggTil(new ResourceLink("/fptilbake/api/behandling/totrinnskontroll/arsaker_read_only", "totrinnskontroll-arsaker-readOnly", ResourceLink.HttpMethod.POST));
        }

        return dto;
    }

    public UtvidetBehandlingDto hentUtvidetBehandlingResultat(long behandlingId, AsyncPollingStatus taskStatus) {
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);

        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();

        settStandardFelter(behandling, dto);
        dto.setBehandlingPåVent(behandling.isBehandlingPåVent());
        getFristDatoBehandlingPåVent(behandling).ifPresent(dto::setFristBehandlingPåVent);
        getVenteÅrsak(behandling).ifPresent(dto::setVenteÅrsakKode);
        dto.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());

        settResourceLinks(behandling, dto);

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
        //FIXME fjern hardkoding
        dto.setBehandlendeEnhetId("4833");
        dto.setBehandlendeEnhetNavn("NAV Familie- og pensjonsytelser Oslo 1");
    }

    private void settResourceLinks(Behandling behandling, UtvidetBehandlingDto dto) {
        dto.leggTil(ResourceLink.get("/fptilbake/api/behandling/aksjonspunkt?behandlingId=" + behandling.getId(), "aksjonspunkter", null));
        BehandlingStegType bst = behandling.getAktivtBehandlingSteg();

        if (BehandlingStegType.FAKTA_FEILUTBETALING.equals(bst)) {
            settBehandlingFaktaLink(behandling.getId(), dto);
            settGrunnlagLink(dto);
        } else if (BehandlingStegType.FORELDELSEVURDERINGSTEG.equals(bst)) {
            settBehandlingFaktaLink(behandling.getId(), dto);
            settGrunnlagLink(dto);
            settForeldelseLink(behandling.getId(), dto);
        } else if (BehandlingStegType.VTILBSTEG.equals(bst)) {
            settBehandlingFaktaLink(behandling.getId(), dto);
            settGrunnlagLink(dto);
            settForeldelseLink(behandling.getId(), dto);
            settVilkårsvurderingsperioderLinks(behandling.getId(), dto);
        } else if (BehandlingStegType.FORESLÅ_VEDTAK.equals(bst) || BehandlingStegType.FATTE_VEDTAK.equals(bst) || bst == null) {
            settBehandlingFaktaLink(behandling.getId(), dto);
            settGrunnlagLink(dto);
            settForeldelseLink(behandling.getId(), dto);
            settVilkårsvurderingsperioderLinks(behandling.getId(), dto);
            settBehandlingsresultatLink(behandling.getId(), dto);
            settVedtaksbrevdataLink(behandling.getId(), dto);
        }

        settTotrinnskontrollLinks(behandling, dto);
    }

    private void settBehandlingFaktaLink(long behandlingId, UtvidetBehandlingDto dto) {
        dto.leggTil(ResourceLink.get("/fptilbake/api/behandlingfakta/hent-fakta/feilutbetaling?behandlingId=" + behandlingId, "feilutbetalingFakta", null));
    }

    private void settForeldelseLink(long behandlingId, UtvidetBehandlingDto dto) {
        if (vurdertForeldelseTjeneste.harForeldetPeriodeForBehandlingId(behandlingId)) {
            dto.leggTil(ResourceLink.get("/fptilbake/api/foreldelse/vurdert?behandlingId=" + behandlingId, FORELDELSE, null));
        } else {
            dto.leggTil(ResourceLink.get("/fptilbake/api/foreldelse?behandlingId=" + behandlingId, FORELDELSE, null));
        }
    }

    private void settGrunnlagLink(UtvidetBehandlingDto dto) {
        dto.leggTil(ResourceLink.get("/fptilbake/api/feilutbetalingaarsak", "feilutbetalingAarsak", null));
    }

    private void settVilkårsvurderingsperioderLinks(long behandlingId, UtvidetBehandlingDto dto) {
        dto.leggTil(ResourceLink.get("/fptilbake/api/vilkarsvurdering/perioder?behandlingId=" + behandlingId, "vilkarvurderingsperioder", null));
        dto.leggTil(ResourceLink.get("/fptilbake/api/vilkarsvurdering/vurdert?behandlingId=" + behandlingId, "vilkarvurdering", null));
    }

    private void settBehandlingsresultatLink(long behandlingId, UtvidetBehandlingDto dto) {
        dto.leggTil(ResourceLink.get("/fptilbake/api/beregning/resultat?behandlingId=" + behandlingId, "beregningsresultat", null));
    }

    // TODO (TOR) Fjern dette når GUI er oppdatert
    private void settTotrinnskontrollLinks(Behandling behandling, UtvidetBehandlingDto dto) {
        BehandlingIdDto idDto = new BehandlingIdDto(behandling.getId());
        if (BehandlingStatus.FATTER_VEDTAK.equals(behandling.getStatus())) {
            dto.leggTil(ResourceLink.post("/fptilbake/api/behandling/totrinnskontroll/arsaker", "totrinnskontroll-arsaker", idDto));
        } else if (BehandlingStatus.UTREDES.equals(behandling.getStatus())) {
            dto.leggTil(ResourceLink.post("/fptilbake/api/behandling/totrinnskontroll/arsaker_read_only", "totrinnskontroll-arsaker-readOnly", idDto));
        }
    }

    private void settVedtaksbrevdataLink(Long idDto, UtvidetBehandlingDto dto) {
        dto.leggTil(ResourceLink.post("/fptilbake/api/dokument/hent-vedtaksbrev", "vedtaksbrev", idDto));
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
