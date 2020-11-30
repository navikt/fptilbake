package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbBehandling;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbSærligeGrunner;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;

public class VedtaksbrevFritekst {

    private static String FRITEKST_MARKERING_START = "\\\\FRITEKST_START";
    private static String FRITEKST_PÅKREVET_MARKERING_START = "\\\\PÅKREVET_FRITEKST_START";
    private static String FRITEKST_MARKERING_SLUTT = "\\\\FRITEKST_SLUTT";

    public static void settInnMarkeringForFritekst(HbVedtaksbrevData vedtaksbrevData) {
        for (HbVedtaksbrevPeriode periode : vedtaksbrevData.getPerioder()) {
            FritekstType fritekstTypeForFakta = utledFritekstTypeFakta(periode.getFakta().getHendelseundertype());
            periode.getFakta().setFritekstFakta(markerFritekst(fritekstTypeForFakta, periode.getFakta().getFritekstFakta(), Underavsnitt.Underavsnittstype.FAKTA));
            HbVurderinger vurderinger = periode.getVurderinger();
            if (vurderinger != null) {
                vurderinger.setFritekstForeldelse(markerValgfriFritekst(vurderinger.getFritekstForeldelse(), Underavsnitt.Underavsnittstype.FORELDELSE));
                vurderinger.setFritekstVilkår(markerValgfriFritekst(vurderinger.getFritekstVilkår(), Underavsnitt.Underavsnittstype.VILKÅR));
            }
            HbSærligeGrunner sg = vurderinger.getSærligeGrunner();
            if (sg != null) {
                sg.setFritekstSærligeGrunner(markerValgfriFritekst(sg.getFritekstSærligeGrunner(), Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER));
                sg.setFritekstSærligeGrunnerAnnet(markerPåkrevetFritekst(sg.getFritekstSærligeGrunnerAnnet(), Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET));
            }
        }
        FritekstType fritekstType = utledFritekstTypeForOppsummering(vedtaksbrevData);
        vedtaksbrevData.getFelles().setFritekstOppsummering(markerFritekst(fritekstType, vedtaksbrevData.getFelles().getFritekstOppsummering(), null));
    }

    private static FritekstType utledFritekstTypeForOppsummering(HbVedtaksbrevData vedtaksbrevData) {
        HbBehandling hbBehandling = vedtaksbrevData.getFelles().getBehandling();
        return hbBehandling.erRevurdering() && !hbBehandling.erRevurderingEtterKlage() ? FritekstType.PÅKREVET : FritekstType.VALGFRI;
    }

    private static FritekstType utledFritekstTypeFakta(HendelseUnderType underType) {
        return VedtaksbrevFritekstKonfigurasjon.UNDERTYPER_MED_PÅKREVD_FRITEKST.contains(underType) ? FritekstType.PÅKREVET : FritekstType.VALGFRI;
    }

    static String markerValgfriFritekst(String fritekst) {
        return markerValgfriFritekst(fritekst, null);
    }

    static String markerValgfriFritekst(String fritekst, Underavsnitt.Underavsnittstype underavsnittstype) {
        return markerFritekst(FritekstType.VALGFRI, fritekst, underavsnittstype);
    }

    static String markerPåkrevetFritekst(String fritekst, Underavsnitt.Underavsnittstype underavsnittstype) {
        return markerFritekst(FritekstType.PÅKREVET, fritekst, underavsnittstype);
    }

    static String markerFritekst(FritekstType fritekstType, String fritekst, Underavsnitt.Underavsnittstype underavsnittstype) {
        String fritekstTypeMarkør = fritekstType == FritekstType.PÅKREVET
            ? FRITEKST_PÅKREVET_MARKERING_START
            : FRITEKST_MARKERING_START;
        String startmarkør = underavsnittstype == null
            ? fritekstTypeMarkør
            : fritekstTypeMarkør + underavsnittstype;
        String markertFritekst = fritekst == null
            ? startmarkør + "\n" + FRITEKST_MARKERING_SLUTT
            : startmarkør + "\n" + fritekst + "\n" + FRITEKST_MARKERING_SLUTT;
        return "\n" + markertFritekst;
    }

    static boolean erFritekstStart(String tekst) {
        return tekst.startsWith(FRITEKST_MARKERING_START) || tekst.startsWith(FRITEKST_PÅKREVET_MARKERING_START);
    }

    static boolean erFritekstPåkrevetStart(String tekst) {
        return tekst.startsWith(FRITEKST_PÅKREVET_MARKERING_START);
    }

    static String fjernFritekstmarkering(String tekst) {
        if (tekst.startsWith(FRITEKST_MARKERING_START)) {
            return tekst.substring(FRITEKST_MARKERING_START.length());
        } else if (tekst.startsWith(FRITEKST_PÅKREVET_MARKERING_START)) {
            return tekst.substring(FRITEKST_PÅKREVET_MARKERING_START.length());
        } else {
            throw new IllegalArgumentException("Utvikler-feil: denne metoden skal bare brukes på fritekstmarkering-start");
        }
    }

    static boolean erFritekstSlutt(String tekst) {
        return FRITEKST_MARKERING_SLUTT.equals(tekst);
    }

    enum FritekstType {
        VALGFRI,
        PÅKREVET
    }
}
