package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;

public class HbVedtaksbrevDatoer {

    private LocalDate fpOpphørsdatoDødSøker;
    private LocalDate svpOpphørsdatoDødSøker;

    private HbVedtaksbrevDatoer() {

    }

    public LocalDate getFpOpphørsdatoDødSøker() {
        return fpOpphørsdatoDødSøker;
    }

    public LocalDate getSvpOpphørsdatoDødSøker() {
        return svpOpphørsdatoDødSøker;
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

            kladd.fpOpphørsdatoDødSøker = getOpphørsdato(FpHendelseUnderTyper.OPPHOR_MOTTAKER_DOD);
            kladd.svpOpphørsdatoDødSøker = getOpphørsdato(SvpHendelseUnderTyper.MOTTAKER_DØD);

            return this;
        }

        private LocalDate getOpphørsdato(HendelseUnderType hendelseUnderType) {
            LocalDate førsteDagForType = getFørsteDagForHendelseUnderType(hendelseUnderType);
            if (førsteDagForType != null) {
                return førsteDagForType.minusDays(1);
            }
            return null;
        }

        private LocalDate getFørsteDagForHendelseUnderType(HendelseUnderType hendelseUnderType) {
            return perioder.stream()
                .filter((per) -> per.getFakta().getHendelseundertype().equals(hendelseUnderType))
                .findFirst()
                .map(per -> per.getPeriode().getFom())
                .orElse(null);
        }

        public HbVedtaksbrevDatoer build() {
            return kladd;
        }
    }
}
