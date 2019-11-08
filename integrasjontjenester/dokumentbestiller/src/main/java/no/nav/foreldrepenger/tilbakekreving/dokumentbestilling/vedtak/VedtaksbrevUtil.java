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

    public static List<PeriodeMedTekstDto> mapFritekstFraDb(List<VedtaksbrevPeriode> fritekstPerioder) {
        List<PeriodeMedTekstDto> resultat = new ArrayList<>();
        for (VedtaksbrevPeriode fritekstPeriode : fritekstPerioder) {
            Optional<PeriodeMedTekstDto> periodeMedTekstOptional = finnOpprettetPeriode(resultat, fritekstPeriode.getPeriode());
            PeriodeMedTekstDto periodeMedTekst;
            if (periodeMedTekstOptional.isPresent()) {
                periodeMedTekst = periodeMedTekstOptional.get();
            } else {
                periodeMedTekst = new PeriodeMedTekstDto();
                periodeMedTekst.setFom(fritekstPeriode.getPeriode().getFom());
                periodeMedTekst.setTom(fritekstPeriode.getPeriode().getTom());
                resultat.add(periodeMedTekst);
            }

            FritekstType fritekstType = fritekstPeriode.getFritekstType();
            String fritekst = fritekstPeriode.getFritekst();
            if (FritekstType.FAKTA_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setFaktaAvsnitt(fritekst);
            } else if (FritekstType.VILKAAR_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setVilkårAvsnitt(fritekst);
            } else if (FritekstType.SAERLIGE_GRUNNER_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setSærligeGrunnerAvsnitt(fritekst);
            } else if (FritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setSærligeGrunnerAnnetAvsnitt(fritekst);
            } else {
                throw new IllegalArgumentException("Utvikler-feil: mangler håndtering for fritekstType:" + fritekstType);
            }
        }
        return resultat;
    }

    private static Optional<PeriodeMedTekstDto> finnOpprettetPeriode(List<PeriodeMedTekstDto> perioderMedTekster, Periode periode) {
        return perioderMedTekster.stream()
            .filter(p -> p.getFom().equals(periode.getFom()) && p.getTom().equals(periode.getTom()))
            .findAny();
    }
}
