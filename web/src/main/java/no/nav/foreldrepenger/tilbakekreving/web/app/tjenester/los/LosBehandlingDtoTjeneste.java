package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.los;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.hendelser.behandling.Aksjonspunktstatus;
import no.nav.vedtak.hendelser.behandling.Aksjonspunkttype;
import no.nav.vedtak.hendelser.behandling.AktørId;
import no.nav.vedtak.hendelser.behandling.Behandlingsstatus;
import no.nav.vedtak.hendelser.behandling.Behandlingstype;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.hendelser.behandling.Ytelse;
import no.nav.vedtak.hendelser.behandling.los.LosBehandlingDto;


/**
 * Returnerer behandlingsinformasjon tilpasset behov i FP-LOS
 */

@ApplicationScoped
public class LosBehandlingDtoTjeneste {

    private KravgrunnlagRepository grunnlagRepository;
    private VarselRepository varselRepository;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;

    @Inject
    public LosBehandlingDtoTjeneste(KravgrunnlagRepository grunnlagRepository,
                                    VarselRepository varselRepository,
                                    FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste) {
        this.varselRepository = varselRepository;
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.grunnlagRepository = grunnlagRepository;
    }

    LosBehandlingDtoTjeneste() {
        //for CDI proxy
    }

    public LosBehandlingDto lagLosBehandlingDto(Behandling behandling) {
        var frist = hentFrist(behandling);
        var kravgrunnlag431 = grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId()) ? grunnlagRepository.finnKravgrunnlag(behandling.getId()) : null;
        var tilbakeDto = mapTilbake(behandling, kravgrunnlag431, frist);
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
            mapAksjonspunkter(behandling),
            List.of(),
            false,
            false,
            List.of(),
            null,
            utledBehandlingEgenskaper(behandling, kravgrunnlag431, tilbakeDto).stream().map(LokalBehandlingEgenskap::name).toList(),
            tilbakeDto);
    }

    public enum LokalBehandlingEgenskap {
        VARSLET, OVER_FIRE_RETTSGEBYR
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

    private static List<LosBehandlingDto.LosAksjonspunktDto> mapAksjonspunkter(Behandling behandling) {
        return behandling.getAksjonspunkter().stream()
            .map(LosBehandlingDtoTjeneste::mapTilLosAksjonspunkt)
            .toList();
    }

    private static LosBehandlingDto.LosAksjonspunktDto mapTilLosAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        return new LosBehandlingDto.LosAksjonspunktDto(aksjonspunkt.getAksjonspunktDefinisjon().getKode(),
            mapAksjonspunkttype(aksjonspunkt),
            mapAksjonspunktstatus(aksjonspunkt),
            aksjonspunkt.getFristTid());
    }

    private static Aksjonspunkttype mapAksjonspunkttype(Aksjonspunkt aksjonspunkt) {
        return switch (aksjonspunkt.getAksjonspunktDefinisjon()) {
            case VENT_PÅ_BRUKERTILBAKEMELDING, VENT_PÅ_TILBAKEKREVINGSGRUNNLAG -> Aksjonspunkttype.VENT;
            case FATTE_VEDTAK -> Aksjonspunkttype.BESLUTTER;
            default -> Aksjonspunkttype.AKSJONSPUNKT;
        };
    }

    private static Aksjonspunktstatus mapAksjonspunktstatus(Aksjonspunkt aksjonspunkt) {
        return switch (aksjonspunkt.getStatus()) {
            case OPPRETTET -> Aksjonspunktstatus.OPPRETTET;
            case UTFØRT -> Aksjonspunktstatus.UTFØRT;
            case AVBRUTT -> Aksjonspunktstatus.AVBRUTT;
        };
    }

    private LosBehandlingDto.LosTilbakeDto mapTilbake(Behandling behandling, Kravgrunnlag431 kravgrunnlag431, LocalDate kravgrunnlagManglerFrist) {
        try {
            return new LosBehandlingDto.LosTilbakeDto(kravgrunnlag431 != null ? hentFeilutbetaltBeløp(behandling.getId()) : BigDecimal.ZERO,
                hentFørsteFeilutbetalingDato(kravgrunnlag431, kravgrunnlagManglerFrist));
        } catch (Exception e) {
            if (behandling.erAvsluttet()) {
                return new LosBehandlingDto.LosTilbakeDto(BigDecimal.ZERO, kravgrunnlagManglerFrist);
            } else {
                throw e;
            }
        }
    }

    private static LocalDate hentFørsteFeilutbetalingDato(Kravgrunnlag431 kravgrunnlag431, LocalDate kravgrunnlagManglerFrist) {
        if (kravgrunnlag431 == null) {
            return kravgrunnlagManglerFrist;
        }
        return kravgrunnlag431.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getFom)
            .min(LocalDate::compareTo)
            .orElseGet(() -> kravgrunnlagManglerFrist);
    }

    private BigDecimal hentFeilutbetaltBeløp(Long behandlingId) {
        return faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId).getAktuellFeilUtbetaltBeløp();
    }
    private List<LokalBehandlingEgenskap> utledBehandlingEgenskaper(Behandling behandling, Kravgrunnlag431 kravgrunnlag, LosBehandlingDto.LosTilbakeDto tilbakeDto) {
        List<LokalBehandlingEgenskap> egenskaper = new ArrayList<>();
        try {
            varselRepository.finnVarsel(behandling.getId()).ifPresent(v -> egenskaper.add(LokalBehandlingEgenskap.VARSLET));
            var fireRettsgebyr = KravgrunnlagBeregningTjeneste.heltRettsgebyrFor(kravgrunnlag, behandling.getOpprettetTidspunkt()).multiply(BigDecimal.valueOf(4));
            if (tilbakeDto.feilutbetaltBeløp().compareTo(fireRettsgebyr) >= 0) {
                egenskaper.add(LokalBehandlingEgenskap.OVER_FIRE_RETTSGEBYR);
            }
        } catch (Exception e) {
            // Do nothing.
        }
        return egenskaper;
    }

    private static LocalDate hentFrist(Behandling behandling) {
        var nå = LocalDateTime.now();
        var erPåVentAnnenÅrsak = behandling.getAksjonspunkter().stream()
            .filter(o -> !AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(o.getAksjonspunktDefinisjon()))
            .anyMatch(a -> a.erOpprettet() && a.erAutopunkt() && (a.getFristTid() != null && a.getFristTid().isAfter(nå)));
        if (erPåVentAnnenÅrsak) {
            return null;
        }
        return behandling.getAksjonspunkter().stream()
            .filter(o -> AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(o.getAksjonspunktDefinisjon()))
            .filter(Aksjonspunkt::erOpprettet)
            .map(Aksjonspunkt::getFristTid)
            .filter(Objects::nonNull)
            .map(LocalDateTime::toLocalDate)
            .findFirst().orElse(null);
    }

}
