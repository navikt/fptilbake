package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType.FAKTA_OM_FEILUTBETALING;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.DATE_FORMATTER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.LINJESKIFT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.fraTilEquals;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder.plainTekstLinje;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FaktaFeilutbetalingDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

@ApplicationScoped
public class AvklartFaktaFeilutbetalingHistorikkTjeneste {
    private HistorikkinnslagRepository historikkRepository;

    AvklartFaktaFeilutbetalingHistorikkTjeneste() {
        // CDI
    }

    @Inject
    public AvklartFaktaFeilutbetalingHistorikkTjeneste(HistorikkinnslagRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }


    public void lagHistorikkinnslagForAvklartFaktaFeilutbetaling(Behandling behandling,
                                                                 List<FaktaFeilutbetalingDto> faktaFeilutbetalinger,
                                                                 Optional<FaktaFeilutbetaling> forrigeFakta,
                                                                 String begrunnelse) {
        var nyttHistorikkinnslag = lagHistorikkinnslag2(behandling, faktaFeilutbetalinger, begrunnelse, forrigeFakta);
        nyttHistorikkinnslag.ifPresent(innslag -> historikkRepository.lagre(innslag));
    }

    private static Optional<Historikkinnslag> lagHistorikkinnslag2(Behandling behandling, List<FaktaFeilutbetalingDto> faktaFeilutbetalinger, String begrunnelse, Optional<FaktaFeilutbetaling> forrigeFakta) {
        var tekstlinjerMedEndringer = new ArrayList<HistorikkinnslagLinjeBuilder>();
        for (var nyFakta : faktaFeilutbetalinger) {
            var tidligereVurdering = forrigeFaktaFeilutbetalingMedSammePeriode(nyFakta, forrigeFakta);
            var tidligereÅrsak = årsakTilFeilutbetalingTekst(
                tidligereVurdering.map(FaktaFeilutbetalingPeriode::getHendelseType).orElse(null),
                tidligereVurdering.map(FaktaFeilutbetalingPeriode::getHendelseUndertype).orElse(null)
            );
            var årsakTilFeilutbetaling = fraTilEquals("Årsak til feilutbetaling", tidligereÅrsak, årsakTilFeilutbetalingTekst(nyFakta.getHendelseType(), nyFakta.getHendelseUndertype()));
            if (årsakTilFeilutbetaling != null) {
                tekstlinjerMedEndringer.add(plainTekstLinje(String.format("Vurdering av perioden %s-%s.", DATE_FORMATTER.format(nyFakta.getFom()), DATE_FORMATTER.format(nyFakta.getTom()))));
                tekstlinjerMedEndringer.add(årsakTilFeilutbetaling);
                tekstlinjerMedEndringer.add(LINJESKIFT);
            }
        }
        if (harBegrunnelseEndret(forrigeFakta.map(FaktaFeilutbetaling::getBegrunnelse), begrunnelse)) {
            tekstlinjerMedEndringer.add(plainTekstLinje(begrunnelse));
        }

        if (tekstlinjerMedEndringer.stream().filter(Objects::nonNull).toList().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Historikkinnslag.Builder()
            .medAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel(FAKTA_OM_FEILUTBETALING)
            .medLinjer(tekstlinjerMedEndringer)
            .build());
    }

    private static String årsakTilFeilutbetalingTekst(HendelseType hendelseType, HendelseUnderType hendelseUnderType) {
        if (hendelseType == null || hendelseUnderType == null) {
            return null;
        }

        return String.format("%s, %s", hendelseType.getNavn(), hendelseUnderType.getNavn());
    }

    private static Optional<FaktaFeilutbetalingPeriode> forrigeFaktaFeilutbetalingMedSammePeriode(FaktaFeilutbetalingDto nyFakta, Optional<FaktaFeilutbetaling> forrigeFakta) {
        return forrigeFakta.flatMap(faktaFeilutbetaling -> faktaFeilutbetaling.getFeilutbetaltPerioder().stream()
            .filter(fpå -> fpå.getPeriode().equals(nyFakta.tilPeriode()))
            .findFirst());
    }

    private static boolean harBegrunnelseEndret(Optional<String> forrigeBegrunnelse, String begrunnelse) {
        if (forrigeBegrunnelse.isEmpty()) {
            return true;
        }

        return !Objects.equals(forrigeBegrunnelse.get(), begrunnelse);
    }
}
