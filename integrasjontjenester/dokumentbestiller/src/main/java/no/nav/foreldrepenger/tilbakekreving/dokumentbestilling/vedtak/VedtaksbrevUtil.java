package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VarselbrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.FPDateUtil;

public class VedtaksbrevUtil {

    private VedtaksbrevUtil() {
        //for static access
    }

    public static LocalDateTime finnNyesteVarselbrevTidspunkt(List<VarselbrevSporing> utsendteVarselbrev) {
        if (utsendteVarselbrev.isEmpty()) {
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

    public static List<PeriodeMedTekstDto> mapFritekstFraDb(List<VedtaksbrevPeriode> eksisterendePerioderForBrev) {
        List<PeriodeMedTekstDto> perioderMedTekster = new ArrayList<>();
        for (VedtaksbrevPeriode eksisterendePeriode : eksisterendePerioderForBrev) {
            Optional<PeriodeMedTekstDto> periodeMedTekstOptional = finnOpprettetPeriode(perioderMedTekster, eksisterendePeriode.getPeriode());
            PeriodeMedTekstDto periodeMedTekst;
            if (periodeMedTekstOptional.isPresent()) {
                periodeMedTekst = periodeMedTekstOptional.get();
            } else {
                periodeMedTekst = new PeriodeMedTekstDto();
                periodeMedTekst.setFom(eksisterendePeriode.getPeriode().getFom());
                periodeMedTekst.setTom(eksisterendePeriode.getPeriode().getTom());
                perioderMedTekster.add(periodeMedTekst);
            }

            if (FritekstType.FAKTA_AVSNITT.equals(eksisterendePeriode.getFritekstType())) {
                periodeMedTekst.setFaktaAvsnitt(eksisterendePeriode.getFritekst());
            } else if (FritekstType.VILKAAR_AVSNITT.equals(eksisterendePeriode.getFritekstType())) {
                periodeMedTekst.setVilkårAvsnitt(eksisterendePeriode.getFritekst());
            } else if (FritekstType.SAERLIGE_GRUNNER_AVSNITT.equals(eksisterendePeriode.getFritekstType())) {
                periodeMedTekst.setSærligeGrunnerAvsnitt(eksisterendePeriode.getFritekst());
            }
        }
        return perioderMedTekster;
    }

    private static Optional<PeriodeMedTekstDto> finnOpprettetPeriode(List<PeriodeMedTekstDto> perioderMedTekster, Periode periode) {
        return perioderMedTekster.stream()
            .filter(p -> p.getFom().equals(periode.getFom()) && p.getTom().equals(periode.getTom()))
            .findAny();
    }
}
