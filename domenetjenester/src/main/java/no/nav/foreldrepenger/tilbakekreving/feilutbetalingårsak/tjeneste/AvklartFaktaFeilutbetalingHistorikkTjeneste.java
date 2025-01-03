package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType.FAKTA_OM_FEILUTBETALING;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.LINJESKIFT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;
import static no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.AvklartFaktaFeilutbetalingTjeneste.harBegrunnelseEndret;

@ApplicationScoped
public class AvklartFaktaFeilutbetalingHistorikkTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(AvklartFaktaFeilutbetalingHistorikkTjeneste.class);
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private HistorikkRepositoryTeamAware historikkRepository;

    AvklartFaktaFeilutbetalingHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public AvklartFaktaFeilutbetalingHistorikkTjeneste(FaktaFeilutbetalingRepository faktaFeilutbetalingRepository, HistorikkRepositoryTeamAware historikkRepository) {
        this.faktaFeilutbetalingRepository = faktaFeilutbetalingRepository;
        this.historikkRepository = historikkRepository;
    }


    public void lagHistorikkinnslagForAvklartFaktaFeilutbetaling(Behandling behandling, List<FaktaFeilutbetalingDto> faktaFeilutbetalinger, String begrunnelse, Historikkinnslag gammeltHistorikkinnslag) {
        var forrigeFakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandling.getId());
        var nyttHistorikkinnslag = lagHistorikkinnslag2(behandling, faktaFeilutbetalinger, begrunnelse, forrigeFakta);
        if (nyttHistorikkinnslag.isEmpty()) {
            // TODO: Fjern denne ved overgang
            LOG.warn("HistorikkV2: Fant ingen endringer ved genering av nytt histoikkinnslag, men gammel historikkinnslag gjorde det. Lagrer ned gammelt historikkinnslag.");
            historikkRepository.lagre(gammeltHistorikkinnslag);
        } else {
            historikkRepository.lagre(gammeltHistorikkinnslag, nyttHistorikkinnslag.get());
        }

    }

    private static Optional<Historikkinnslag2> lagHistorikkinnslag2(Behandling behandling, List<FaktaFeilutbetalingDto> faktaFeilutbetalinger, String begrunnelse, Optional<FaktaFeilutbetaling> forrigeFakta) {
        var tekstlinjerMedEndringer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        for (var nyFakta : faktaFeilutbetalinger) {
            var tidligereVurdering = forrigeFaktaFeilutbetalingMedSammePeriode(nyFakta, forrigeFakta);
            var hendelseTekst = fraTilEquals("Hendelse", tidligereVurdering.map(FaktaFeilutbetalingPeriode::getHendelseType).orElse(null), nyFakta.getHendelseType());
            var hendelseUnderÅrsakTekst = fraTilEquals("Hendelse Under Årsak", tidligereVurdering.map(FaktaFeilutbetalingPeriode::getHendelseUndertype).orElse(null), nyFakta.getHendelseUndertype());
            var endretBegrunnelseTekst = harBegrunnelseEndret(forrigeFakta.map(FaktaFeilutbetaling::getBegrunnelse).orElse(null), begrunnelse) ? plainTekstLinje(begrunnelse) : null;
            if (hendelseTekst != null || hendelseUnderÅrsakTekst != null || endretBegrunnelseTekst != null) {
                tekstlinjerMedEndringer.add(hendelseTekst);
                tekstlinjerMedEndringer.add(hendelseUnderÅrsakTekst);
                tekstlinjerMedEndringer.add(endretBegrunnelseTekst);
                tekstlinjerMedEndringer.add(LINJESKIFT);
            }
        }

        // Ingen endringer
        if (tekstlinjerMedEndringer.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Historikkinnslag2.Builder()
            .medAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel(FAKTA_OM_FEILUTBETALING)
            .medLinjer(tekstlinjerMedEndringer)
            .build());
    }

    private static Optional<FaktaFeilutbetalingPeriode> forrigeFaktaFeilutbetalingMedSammePeriode(FaktaFeilutbetalingDto nyFakta, Optional<FaktaFeilutbetaling> forrigeFakta) {
        return forrigeFakta.flatMap(faktaFeilutbetaling -> faktaFeilutbetaling.getFeilutbetaltPerioder().stream()
            .filter(fpå -> fpå.getPeriode().equals(nyFakta.tilPeriode()))
            .findFirst());
    }
}
