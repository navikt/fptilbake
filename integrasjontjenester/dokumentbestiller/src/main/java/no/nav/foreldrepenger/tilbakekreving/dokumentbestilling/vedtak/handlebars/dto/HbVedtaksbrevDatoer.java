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

    private HbVedtaksbrevDatoer() {

    }

    public LocalDate getOpphørsdatoDødSøker() {
        return opphørsdatoDødSøker;
    }

    public LocalDate getOpphørsdatoDødtBarn() {
        return opphørsdatoDødtBarn;
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

            kladd.opphørsdatoDødSøker = getOpphørsdato(SvpHendelseUnderTyper.MOTTAKER_DØD, FpHendelseUnderTyper.OPPHOR_MOTTAKER_DOD);
            kladd.opphørsdatoDødtBarn = getOpphørsdato(FpHendelseUnderTyper.OPPHOR_BARN_DOD);

            return this;
        }

        private LocalDate getOpphørsdato(HendelseUnderType... hendelseUnderTyper) {
            LocalDate førsteDagForType = getFørsteDagForHendelseUnderType(Arrays.asList(hendelseUnderTyper));
            if (førsteDagForType != null) {
                return førsteDagForType.minusDays(1);
            }
            return null;
        }

        private LocalDate getFørsteDagForHendelseUnderType(List<HendelseUnderType> hendelseUnderTyper) {
            return perioder.stream()
                .filter((per) -> hendelseUnderTyper.contains(per.getFakta().getHendelseundertype()))
                .findFirst()
                .map(per -> per.getPeriode().getFom())
                .orElse(null);
        }

        public HbVedtaksbrevDatoer build() {
            return kladd;
        }
    }
}
