package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.vedtak.util.FPDateUtil;

public class VedtaksbrevUtil {

    private VedtaksbrevUtil() {
        //for static access
    }

    public static LocalDateTime finnNyesteVarselbrevTidspunkt(List<VarselbrevSporing> utsendteVarselbrev) {
        if(utsendteVarselbrev.isEmpty()){
            return FPDateUtil.nå();
        }
        LocalDateTime nyesteVarselSendt = utsendteVarselbrev.get(0).getOpprettetTidspunkt();
        for (VarselbrevSporing varselbrevData : utsendteVarselbrev) {
            if (varselbrevData.getOpprettetTidspunkt().isAfter(nyesteVarselSendt)) {
                nyesteVarselSendt = varselbrevData.getOpprettetTidspunkt();
            }
        }
        return nyesteVarselSendt;
    }

    public static Long finnTotaltTilbakekrevingsbeløp(List<BeregningResultatPeriode> beregningResultatPerioder) {
        BigDecimal totalTilbakekrevingBeløp = beregningResultatPerioder.stream().map(BeregningResultatPeriode::getTilbakekrevingBeløp)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalTilbakekrevingBeløp.longValue();
    }

    public static List<PeriodeMedTekstDto> mapFritekstFraDb(List<VedtaksbrevPeriode> eksisterendePerioderForBrev) {
        List<PeriodeMedTekstDto> perioderMedTekster = new ArrayList<>();
        for (VedtaksbrevPeriode eksisterendePeriode : eksisterendePerioderForBrev) {
            Optional<PeriodeMedTekstDto> periodeMedTekstOptional = finnOpprettetPeriode(perioderMedTekster, eksisterendePeriode);
            PeriodeMedTekstDto periodeMedTekst;
            if (periodeMedTekstOptional.isPresent()) {
                periodeMedTekst = periodeMedTekstOptional.get();
            } else {
                periodeMedTekst = new PeriodeMedTekstDto();
                periodeMedTekst.setFom(eksisterendePeriode.getPeriode().getFom());
                periodeMedTekst.setTom(eksisterendePeriode.getPeriode().getTom());
                perioderMedTekster.add(periodeMedTekst);
            }

            if (eksisterendePeriode.getFritekstType() == FritekstType.FAKTA_AVSNITT) {
                periodeMedTekst.setFaktaAvsnitt(eksisterendePeriode.getFritekst());
            } else if (eksisterendePeriode.getFritekstType() == FritekstType.VILKAAR_AVSNITT) {
                periodeMedTekst.setVilkårAvsnitt(eksisterendePeriode.getFritekst());
            } else if (eksisterendePeriode.getFritekstType() == FritekstType.SAERLIGE_GRUNNER_AVSNITT) {
                periodeMedTekst.setSærligeGrunnerAvsnitt(eksisterendePeriode.getFritekst());
            }
        }
        return perioderMedTekster;
    }

    private static Optional<PeriodeMedTekstDto> finnOpprettetPeriode(List<PeriodeMedTekstDto> perioderMedTekster, VedtaksbrevPeriode lagretPeriodeMedFritekst) {
        for (PeriodeMedTekstDto periode : perioderMedTekster) {
            if (periode != null && periode.getFom().equals(lagretPeriodeMedFritekst.getPeriode().getFom())) {
                return Optional.of(periode);
            }
        }
        return Optional.empty();
    }
}
