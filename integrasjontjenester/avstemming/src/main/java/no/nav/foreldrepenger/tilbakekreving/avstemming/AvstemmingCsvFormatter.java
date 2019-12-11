package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

public class AvstemmingCsvFormatter {

    private static final String SKILLETEGN_KOLONNER = ",";
    private static final String SKILLETEGN_RADER = "\n";
    private static final DateTimeFormatter TIDSFORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
    private static final DateTimeFormatter DATOFORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private StringBuilder data = new StringBuilder();
    private boolean erTom = true;

    public void leggTilRad(RadBuilder radBuilder) {
        String rad = radBuilder.build();
        if (!erTom) {
            data.append(SKILLETEGN_RADER);
        }
        data.append(rad);
        erTom = false;
    }

    public String getData() {
        return data.toString();
    }

    public static RadBuilder radBuilder() {
        return new RadBuilder();
    }

    public static class RadBuilder {
        private String avsender;
        private LocalDateTime tidspunktDannet;
        private String vedtakId;
        private String fnr;
        private LocalDate vedtaksdato;
        private FagsakYtelseType fagsakYtelseType;
        private BigDecimal tilbakekrevesBruttoUtenRenter;
        private BigDecimal tilbakekrevesNettoUtenRenter;
        private BigDecimal renter;
        private BigDecimal skatt;

        public RadBuilder medAvsender(String avsender) {
            this.avsender = avsender;
            return this;
        }

        public RadBuilder medTidspunktDannet(LocalDateTime tidspunktDannet) {
            this.tidspunktDannet = tidspunktDannet;
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

        public String build() {
            Objects.requireNonNull(avsender, "avsender mangler");
            Objects.requireNonNull(tidspunktDannet, "tidspunktDannet mangler");
            Objects.requireNonNull(vedtakId, "vedtakId mangler");
            Objects.requireNonNull(fnr, "fnr mangler");
            Objects.requireNonNull(vedtaksdato, "vedtaksdato mangler");
            Objects.requireNonNull(fagsakYtelseType, "fagsakYtelseType mangler");
            Objects.requireNonNull(tilbakekrevesBruttoUtenRenter, "tilbakekrevesBruttoUtenRenter mangler");
            Objects.requireNonNull(tilbakekrevesNettoUtenRenter, "tilbakekrevesNettoUtenRenter mangler");
            Objects.requireNonNull(renter, "renter mangler");
            Objects.requireNonNull(skatt, "skatt mangler");

            return format(avsender)
                + SKILLETEGN_KOLONNER + format(tidspunktDannet)
                + SKILLETEGN_KOLONNER + format(vedtakId)
                + SKILLETEGN_KOLONNER + format(fnr)
                + SKILLETEGN_KOLONNER + format(vedtaksdato)
                + SKILLETEGN_KOLONNER + format(fagsakYtelseType)
                + SKILLETEGN_KOLONNER + format(tilbakekrevesBruttoUtenRenter)
                + SKILLETEGN_KOLONNER + format(skatt)
                + SKILLETEGN_KOLONNER + format(tilbakekrevesNettoUtenRenter)
                + SKILLETEGN_KOLONNER + format(renter)
                ;
        }

        private String format(String verdi) {
            return verdi;
        }

        private String format(LocalDateTime tid) {
            return tid.format(TIDSFORMAT);
        }

        private String format(LocalDate dato) {
            return dato.format(DATOFORMAT);
        }

        private String format(Kodeliste kode) {
            return format(kode.getKode());
        }

        private String format(BigDecimal verdi) {
            //Rart, men multipliserer her med 100 for å sende samme data på samme format som brukes i andre enden av kjeden
            BigDecimal omskrevet = verdi.multiply(BigDecimal.valueOf(100));
            return omskrevet.toPlainString();
        }

    }
}
