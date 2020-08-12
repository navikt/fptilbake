package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public class AvstemmingCsvFormatter {
    private static final String SKILLETEGN_KOLONNER = ";";

    private static final String SKILLETEGN_RADER = "\n";
    private static final DateTimeFormatter DATOFORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private StringBuilder data = new StringBuilder();
    private int antallRader;

    public AvstemmingCsvFormatter() {
        data.append(RadBuilder.buildeHeader());
    }

    public void leggTilRad(RadBuilder radBuilder) {
        data.append(SKILLETEGN_RADER);
        data.append(radBuilder.build());
        antallRader++;
    }

    public String getData() {
        return data.toString();
    }

    public int getAntallRader() {
        return antallRader;
    }

    public static RadBuilder radBuilder() {
        return new RadBuilder();
    }

    public static class RadBuilder {

        private String avsender;
        private String vedtakId;
        private String fnr;
        private LocalDate vedtaksdato;
        private FagsakYtelseType fagsakYtelseType;
        private BigDecimal tilbakekrevesBruttoUtenRenter;
        private BigDecimal skatt;
        private BigDecimal tilbakekrevesNettoUtenRenter;
        private BigDecimal renter;
        private boolean erOmgjøringTilIngenTilbakekreving;

        public RadBuilder medAvsender(String avsender) {
            this.avsender = avsender;
            return this;
        }

        public RadBuilder medVedtakId(String vedtakId) {
            this.vedtakId = vedtakId;
            return this;
        }

        public RadBuilder medFnr(String fnr) {
            this.fnr = fnr;
            return this;
        }

        public RadBuilder medVedtaksdato(LocalDate vedtaksdato) {
            this.vedtaksdato = vedtaksdato;
            return this;
        }

        public RadBuilder medFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
            this.fagsakYtelseType = fagsakYtelseType;
            return this;
        }

        public RadBuilder medTilbakekrevesBruttoUtenRenter(BigDecimal tilbakekrevesBruttoUtenRenter) {
            this.tilbakekrevesBruttoUtenRenter = tilbakekrevesBruttoUtenRenter;
            return this;
        }

        public RadBuilder medTilbakekrevesNettoUtenRenter(BigDecimal tilbakekrevesNettoUtenRenter) {
            this.tilbakekrevesNettoUtenRenter = tilbakekrevesNettoUtenRenter;
            return this;
        }

        public RadBuilder medRenter(BigDecimal renter) {
            this.renter = renter;
            return this;
        }

        public RadBuilder medSkatt(BigDecimal skatt) {
            this.skatt = skatt;
            return this;
        }

        public RadBuilder medErOmgjøringTilIngenTilbakekreving(boolean erOmgjøringTilIngenTilbakekreving) {
            this.erOmgjøringTilIngenTilbakekreving = erOmgjøringTilIngenTilbakekreving;
            return this;
        }

        public static String buildeHeader() {
            return "avsender" + SKILLETEGN_KOLONNER +
                "vedtakId" + SKILLETEGN_KOLONNER +
                "fnr" + SKILLETEGN_KOLONNER +
                "vedtaksdato" + SKILLETEGN_KOLONNER +
                "fagsakYtelseType" + SKILLETEGN_KOLONNER +
                "tilbakekrevesBruttoUtenRenter" + SKILLETEGN_KOLONNER +
                "skatt" + SKILLETEGN_KOLONNER +
                "tilbakekrevesNettoUtenRenter" + SKILLETEGN_KOLONNER +
                "renter" + SKILLETEGN_KOLONNER +
                "erOmgjøringTilIngenTilbakekreving";
        }

        public String build() {
            Objects.requireNonNull(avsender, "avsender mangler");
            Objects.requireNonNull(vedtakId, "vedtakId mangler");
            Objects.requireNonNull(fnr, "fnr mangler");
            Objects.requireNonNull(vedtaksdato, "vedtaksdato mangler");
            Objects.requireNonNull(fagsakYtelseType, "fagsakYtelseType mangler");
            Objects.requireNonNull(tilbakekrevesBruttoUtenRenter, "tilbakekrevesBruttoUtenRenter mangler");
            Objects.requireNonNull(tilbakekrevesNettoUtenRenter, "tilbakekrevesNettoUtenRenter mangler");
            Objects.requireNonNull(renter, "renter mangler");
            Objects.requireNonNull(skatt, "skatt mangler");

            return format(avsender)
                + SKILLETEGN_KOLONNER + format(vedtakId)
                + SKILLETEGN_KOLONNER + format(fnr)
                + SKILLETEGN_KOLONNER + format(vedtaksdato)
                + SKILLETEGN_KOLONNER + format(fagsakYtelseType)
                + SKILLETEGN_KOLONNER + format(tilbakekrevesBruttoUtenRenter)
                + SKILLETEGN_KOLONNER + format(skatt)
                + SKILLETEGN_KOLONNER + format(tilbakekrevesNettoUtenRenter)
                + SKILLETEGN_KOLONNER + format(renter)
                + SKILLETEGN_KOLONNER + formatOmgjøring(erOmgjøringTilIngenTilbakekreving)
                ;
        }

        private String format(String verdi) {
            return verdi;
        }

        private String format(LocalDate dato) {
            return dato.format(DATOFORMAT);
        }

        private String format(Kodeverdi kode) {
            return format(kode.getKode());
        }

        private String format(BigDecimal verdi) {
            return verdi.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        }

        private String formatOmgjøring(boolean verdi) {
            return verdi ? "Omgjoring0" : "";
        }

    }
}
