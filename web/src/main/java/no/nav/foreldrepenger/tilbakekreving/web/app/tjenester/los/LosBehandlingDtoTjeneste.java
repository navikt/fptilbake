package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.los;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.hendelser.behandling.Aksjonspunktstatus;
import no.nav.vedtak.hendelser.behandling.AktørId;
import no.nav.vedtak.hendelser.behandling.Behandlingsstatus;
import no.nav.vedtak.hendelser.behandling.Behandlingstype;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.hendelser.behandling.Ytelse;
import no.nav.vedtak.hendelser.behandling.los.LosBehandlingDto;


/**
 * Returnerer behandlingsinformasjon tilpasset behov i FP-LOS
 *
 */

@ApplicationScoped
public class LosBehandlingDtoTjeneste {

    private KravgrunnlagRepository grunnlagRepository;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;

    @Inject
    public LosBehandlingDtoTjeneste(KravgrunnlagRepository grunnlagRepository, FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste) {
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.grunnlagRepository = grunnlagRepository;
    }

    LosBehandlingDtoTjeneste() {
        //for CDI proxy
    }

    public LosBehandlingDto lagLosBehandlingDto(Behandling behandling) {
        var frist = hentFrist(behandling);
        var kravgrunnlag431 = grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId()) ? grunnlagRepository.finnKravgrunnlag(behandling.getId()) : null;
        return new LosBehandlingDto(behandling.getUuid(),
            Kildesystem.FPTILBAKE,
            behandling.getFagsak().getSaksnummer().getVerdi(),
            mapYtelse(behandling),
            new AktørId(behandling.getAktørId().getId()),
            mapBehandlingstype(behandling),
            mapBehandlingsstatus(behandling),
            behandling.getOpprettetTidspunkt(),
            behandling.getBehandlendeEnhetId(),
            null,
            behandling.getAnsvarligSaksbehandler(),
            mapAksjonspunkter(behandling, kravgrunnlag431, frist),
            List.of(),
            false,
            false,
            null,
            mapTilbake(behandling, kravgrunnlag431, frist));
    }

    private static Ytelse mapYtelse(Behandling behandling) {
        return switch (behandling.getFagsak().getFagsakYtelseType()) {
            case FORELDREPENGER -> Ytelse.FORELDREPENGER;
            case ENGANGSTØNAD -> Ytelse.ENGANGSTØNAD;
            case SVANGERSKAPSPENGER -> Ytelse.SVANGERSKAPSPENGER;
            default -> throw new IllegalStateException("Sak uten kjent ytelse");
        };
    }

    private static Behandlingstype mapBehandlingstype(Behandling behandling) {
        return switch (behandling.getType()) {
            case TILBAKEKREVING -> Behandlingstype.TILBAKEBETALING;
            case REVURDERING_TILBAKEKREVING -> Behandlingstype.TILBAKEBETALING_REVURDERING;
            default-> throw new IllegalStateException("Behandling uten kjent type");
        };
    }

    private static Behandlingsstatus mapBehandlingsstatus(Behandling behandling) {
        return switch (behandling.getStatus()) {
            case OPPRETTET -> Behandlingsstatus.OPPRETTET;
            case UTREDES -> Behandlingsstatus.UTREDES;
            case FATTER_VEDTAK -> Behandlingsstatus.FATTER_VEDTAK;
            case IVERKSETTER_VEDTAK -> Behandlingsstatus.IVERKSETTER_VEDTAK;
            case AVSLUTTET -> Behandlingsstatus.AVSLUTTET;
        };
    }

    private static List<LosBehandlingDto.LosAksjonspunktDto> mapAksjonspunkter(Behandling behandling, Kravgrunnlag431 kravgrunnlag431, LocalDateTime kravgrunnlagManglerFrist) {
        var vanlige = behandling.getAksjonspunkter().stream()
            .map(LosBehandlingDtoTjeneste::mapTilLosAksjonspunkt)
            .collect(Collectors.toList());
        List<LosBehandlingDto.LosAksjonspunktDto> aksjonspunkter = new ArrayList<>(vanlige);
        if (kravgrunnlag431 == null && kravgrunnlagManglerFrist != null) {
            aksjonspunkter.add(new LosBehandlingDto.LosAksjonspunktDto(AksjonspunktKodeDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG,
                Aksjonspunktstatus.OPPRETTET, null, kravgrunnlagManglerFrist));
        }
        return aksjonspunkter;
    }

    private static LosBehandlingDto.LosAksjonspunktDto mapTilLosAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        return new LosBehandlingDto.LosAksjonspunktDto(aksjonspunkt.getAksjonspunktDefinisjon().getKode(),
            mapAksjonspunktstatus(aksjonspunkt),
            null,
            aksjonspunkt.getFristTid());
    }

    private static Aksjonspunktstatus mapAksjonspunktstatus(Aksjonspunkt aksjonspunkt) {
        return switch (aksjonspunkt.getStatus()) {
            case OPPRETTET -> Aksjonspunktstatus.OPPRETTET;
            case UTFØRT -> Aksjonspunktstatus.UTFØRT;
            case AVBRUTT -> Aksjonspunktstatus.AVBRUTT;
        };
    }

    private LosBehandlingDto.LosTilbakeDto mapTilbake(Behandling behandling, Kravgrunnlag431 kravgrunnlag431, LocalDateTime kravgrunnlagManglerFrist) {
        return new LosBehandlingDto.LosTilbakeDto(kravgrunnlag431 != null ? hentFeilutbetaltBeløp(behandling.getId()) : BigDecimal.ZERO,
            hentFørsteFeilutbetalingDato(kravgrunnlag431, kravgrunnlagManglerFrist));
    }

    private static LocalDate hentFørsteFeilutbetalingDato(Kravgrunnlag431 kravgrunnlag431, LocalDateTime kravgrunnlagManglerFrist) {
        if (kravgrunnlag431 == null) {
            return kravgrunnlagManglerFrist != null ? kravgrunnlagManglerFrist.toLocalDate() : null;
        }
        return kravgrunnlag431.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getFom)
            .min(LocalDate::compareTo)
            .orElse(null);
    }

    private BigDecimal hentFeilutbetaltBeløp(Long behandlingId) {
        return faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId).getAktuellFeilUtbetaltBeløp();
    }

    private static LocalDateTime hentFrist(Behandling behandling) {
        var erPåVentAnnenÅrsak = behandling.getAksjonspunkter().stream()
            .filter(o -> !AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(o.getAksjonspunktDefinisjon()))
            .anyMatch(Aksjonspunkt::erOpprettet);
        if (erPåVentAnnenÅrsak) {
            return null;
        }
        return behandling.getAksjonspunkter().stream()
            .filter(o -> AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(o.getAksjonspunktDefinisjon()))
            .map(Aksjonspunkt::getFristTid)
            .filter(Objects::nonNull)
            .filter(LocalDateTime.now()::isAfter)
            .findFirst().orElse(null);
    }

}
