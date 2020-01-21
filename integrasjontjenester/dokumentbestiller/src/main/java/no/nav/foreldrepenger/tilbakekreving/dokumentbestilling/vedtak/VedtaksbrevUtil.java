package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class VedtaksbrevUtil {

    private VedtaksbrevUtil() {
        //for static access
    }

    public static List<PeriodeMedTekstDto> mapFritekstFraDb(List<VedtaksbrevFritekstPeriode> fritekstPerioder) {
        List<PeriodeMedTekstDto> resultat = new ArrayList<>();
        for (VedtaksbrevFritekstPeriode fritekstPeriode : fritekstPerioder) {
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

            VedtaksbrevFritekstType fritekstType = fritekstPeriode.getFritekstType();
            String fritekst = fritekstPeriode.getFritekst();
            if (VedtaksbrevFritekstType.FAKTA_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setFaktaAvsnitt(fritekst);
            } else if (VedtaksbrevFritekstType.VILKAAR_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setVilkårAvsnitt(fritekst);
            } else if (VedtaksbrevFritekstType.SAERLIGE_GRUNNER_AVSNITT.equals(fritekstType)) {
                periodeMedTekst.setSærligeGrunnerAvsnitt(fritekst);
            } else if (VedtaksbrevFritekstType.SAERLIGE_GRUNNER_ANNET_AVSNITT.equals(fritekstType)) {
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
