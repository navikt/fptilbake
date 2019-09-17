package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingUtil.sjekkAvvikHvisSisteDagIHelgen;
import static no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil.beregnBelop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.threeten.extra.Days;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingPeriodeÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeilutbetalingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;

@ApplicationScoped
public class FeilutbetalingTjeneste {

    private FeilutbetalingRepository feilutbetalingRepository;

    FeilutbetalingTjeneste(){
        // for CDI proxy
    }

    @Inject
    public FeilutbetalingTjeneste(BehandlingRepositoryProvider repositoryProvider){
        this.feilutbetalingRepository = repositoryProvider.getFeilutbetalingRepository();
    }

    public void formFeilutbetalingÅrsak(Long behandlingId, UtbetaltPeriode utbetaltPeriode) {
        Optional<FeilutbetalingAggregate> feilutbetalingAggregate = feilutbetalingRepository.finnFeilutbetaling(behandlingId);
        if (feilutbetalingAggregate.isPresent()) {
            Optional<FeilutbetalingPeriodeÅrsak> feilutbetalingPeriodeÅrsak = feilutbetalingAggregate.get()
                .getFeilutbetaling()
                .getFeilutbetaltPerioder()
                .stream()
                .filter(periodeÅrsak -> utbetaltPeriode.tilPeriode().equals(periodeÅrsak.getPeriode()))
                .findFirst();
            utbetaltPeriode.setFeilutbetalingÅrsakDto(mapFra(feilutbetalingPeriodeÅrsak));
        }
    }

    public BehandlingFeilutbetalingFakta lagBehandlingFeilUtbetalingFakta(SimuleringResultatDto simuleringResultat, BigDecimal aktuellFeilUtbetaltBeløp,
                                                                           List<UtbetaltPeriode> utbetaltPerioder,
                                                                           Periode totalPeriode) {
        return BehandlingFeilutbetalingFakta.builder()
            .medPerioder(utbetaltPerioder)
            .medAktuellFeilUtbetaltBeløp(aktuellFeilUtbetaltBeløp)
            .medTidligereVarsletBeløp(new BigDecimal(simuleringResultat.getSumFeilutbetaling()).abs())
            .medTotalPeriodeFom(totalPeriode.getFom())
            .medTotalPeriodeTom(totalPeriode.getTom())
            // FIXME:må hente ekte data fra repository.Dette bør fikse med en annen brukerstorien
            .medDatoForRevurderingsvedtak(LocalDate.of(2019, 1, 4))
            .medDatoForVarselSendt(LocalDate.of(2019, 1, 4))
            // FIXME: Der teksten kommer fra, er ikke ferdiggjort. Hardcoded for nå
            .medÅrsakRevurdering("Endring fra bruker Inntekstmelding Nyeregisteropplysninger")
            .medResultatFeilutbetaling("Innviliget:Endring i beregning og uttak.Feilutbetaling med tilbakekreving")
            .build();
    }

    private FeilutbetalingÅrsakDto mapFra(Optional<FeilutbetalingPeriodeÅrsak> årsak) {
        FeilutbetalingÅrsakDto feilutbetalingÅrsakDto = new FeilutbetalingÅrsakDto();
        if (årsak.isPresent()) {
            FeilutbetalingPeriodeÅrsak feilutbetalingPeriodeÅrsak = årsak.get();
            feilutbetalingÅrsakDto.setÅrsakKode(feilutbetalingPeriodeÅrsak.getÅrsak());
            if (StringUtils.isNotEmpty(feilutbetalingPeriodeÅrsak.getUnderÅrsak()))
                feilutbetalingÅrsakDto.leggTilUnderÅrsaker(new UnderÅrsakDto(null, feilutbetalingPeriodeÅrsak.getUnderÅrsak(),
                    feilutbetalingPeriodeÅrsak.getUnderÅrsakKodeverk()));
        }
        return feilutbetalingÅrsakDto;
    }

    public List<KravgrunnlagPeriode432> finnPerioderMedFeilutbetaltPosteringer(List<KravgrunnlagPeriode432> allePerioder) {
        List<KravgrunnlagPeriode432> feilutbetaltPerioder = new ArrayList<>();
        for (KravgrunnlagPeriode432 kravgrunnlagPeriode432 : allePerioder) {
            List<KravgrunnlagBelop433> posteringer = kravgrunnlagPeriode432.getKravgrunnlagBeloper433().stream()
                .filter(belop433 -> belop433.getKlasseType().equals(KlasseType.FEIL)).collect(Collectors.toList());
            if (!posteringer.isEmpty()) {
                kravgrunnlagPeriode432.setKravgrunnlagBeloper433(posteringer);
                feilutbetaltPerioder.add(kravgrunnlagPeriode432);
            }
        }
        return feilutbetaltPerioder;
    }

    public List<UtbetaltPeriode> finnesLogiskPeriode(List<KravgrunnlagPeriode432> feilutbetaltPerioder) {
        LocalDate førsteDag = null;
        LocalDate sisteDag = null;
        BigDecimal belopPerPeriode = BigDecimal.ZERO;
        feilutbetaltPerioder.sort(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        List<UtbetaltPeriode> beregnetPerioider = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : feilutbetaltPerioder) {
            // for første gang
            Periode periode = kgPeriode.getPeriode();
            if (førsteDag == null && sisteDag == null) {
                førsteDag = periode.getFom();
                sisteDag = periode.getTom();
            } else {
                // beregn forskjellen mellom to perioder
                int antallDager = Days.between(sisteDag, periode.getFom()).getAmount();
                // hvis forskjellen er mer enn 1 dager eller siste dag er i helgen
                if (antallDager > 1 && sjekkAvvikHvisSisteDagIHelgen(sisteDag, antallDager)) {
                    // lag ny perioder hvis forskjellen er mer enn 1 dager
                    beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
                    førsteDag = periode.getFom();
                    belopPerPeriode = BigDecimal.ZERO;
                }
                sisteDag = periode.getTom();
            }
            belopPerPeriode = belopPerPeriode.add(beregnBelop(kgPeriode.getKravgrunnlagBeloper433()));
        }
        if (belopPerPeriode != BigDecimal.ZERO) {
            beregnetPerioider.add(UtbetaltPeriode.lagPeriode(førsteDag, sisteDag, belopPerPeriode));
        }
        return beregnetPerioider;
    }

}
