package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;

public class HbVedtaksbrevDatoer {

    private LocalDate opphørsdatoDødSøker;
    private LocalDate opphørsdatoDødtBarn;
    private LocalDate opphørsdatoIkkeGravid;
    private LocalDate opphørsdatoIkkeOmsorg;

    private HbVedtaksbrevDatoer() {

    }

    public LocalDate getOpphørsdatoDødSøker() {
        return opphørsdatoDødSøker;
    }

    public LocalDate getOpphørsdatoDødtBarn() {
        return opphørsdatoDødtBarn;
    }

    public LocalDate getOpphørsdatoIkkeGravid() {
        return opphørsdatoIkkeGravid;
    }

    public LocalDate getOpphørsdatoIkkeOmsorg() {
        return opphørsdatoIkkeOmsorg;
    }

    public static HbVedtaksbrevDatoer.Builder builder() {
        return new HbVedtaksbrevDatoer.Builder();
    }

    public static class Builder {
        private HbVedtaksbrevDatoer kladd = new HbVedtaksbrevDatoer();

        private List<HbVedtaksbrevPeriode> perioder;

        private Builder() {
        }

        public HbVedtaksbrevDatoer.Builder medPerioder(List<HbVedtaksbrevPeriode> perioder) {
            this.perioder = perioder;

            kladd.opphørsdatoDødSøker = getFørsteDagForHendelseUnderType(SvpHendelseUnderTyper.MOTTAKER_DØD, FpHendelseUnderTyper.OPPHOR_MOTTAKER_DOD);
            kladd.opphørsdatoDødtBarn = getFørsteDagForHendelseUnderType(FpHendelseUnderTyper.OPPHOR_BARN_DOD);
            kladd.opphørsdatoIkkeGravid = getFørsteDagForHendelseUnderType(SvpHendelseUnderTyper.MOTTAKER_IKKE_GRAVID);
            kladd.opphørsdatoIkkeOmsorg = getFørsteDagForHendelseUnderType(FpHendelseUnderTyper.IKKE_OMSORG);
            return this;
        }

        public  HbVedtaksbrevDatoer.Builder medDatoer(LocalDate opphørsdatoDødSøker, LocalDate opphørsdatoDødtBarn, LocalDate opphørsdatoIkkeGravid, LocalDate opphørsdatoIkkeOmsorg) {
            kladd.opphørsdatoDødSøker = opphørsdatoDødSøker;
            kladd.opphørsdatoDødtBarn = opphørsdatoDødtBarn;
            kladd.opphørsdatoIkkeGravid = opphørsdatoIkkeGravid;
            kladd.opphørsdatoIkkeOmsorg = opphørsdatoIkkeOmsorg;
            return this;
        }


        private LocalDate getFørsteDagForHendelseUnderType(HendelseUnderType... hendelseUnderTyper) {
            return getFørsteDagForHendelseUnderType(Arrays.asList(hendelseUnderTyper));
        }

        private LocalDate getFørsteDagForHendelseUnderType(List<HendelseUnderType> hendelseUnderTyper) {
            return perioder.stream()
                .filter(per -> hendelseUnderTyper.contains(per.getFakta().getHendelseundertype()))
                .findFirst()
                .map(per -> per.getPeriode().getFom())
                .orElse(null);
        }

        public HbVedtaksbrevDatoer build() {
            return kladd;
        }
    }
}
