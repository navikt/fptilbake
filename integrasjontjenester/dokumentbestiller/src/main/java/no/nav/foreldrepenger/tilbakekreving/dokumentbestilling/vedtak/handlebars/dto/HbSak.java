package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;

public class HbSak {
    @JsonProperty("ytelsetype")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private FagsakYtelseType ytelsetype;
    @JsonProperty("er-fødsel")
    private boolean erFødsel;
    @JsonProperty("er-adopsjon")
    private boolean erAdopsjon;
    @JsonProperty("antall-barn")
    private Integer antallBarn;
    @JsonProperty("dato-fagsakvedtak")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate datoFagsakvedtak;

    private HbSak() {
    }

    public FagsakYtelseType getYtelsetype() {
        return ytelsetype;
    }

    public static Builder build() {
        return new Builder();
    }

    public boolean harDatoForFagsakvedtak() {
        return datoFagsakvedtak != null;
    }

    public static class Builder {
        private HbSak kladd = new HbSak();

        public HbSak.Builder medYtelsetype(FagsakYtelseType ytelsetype) {
            kladd.ytelsetype = ytelsetype;
            return this;
        }

        public HbSak.Builder medErFødsel(boolean erFødsel) {
            kladd.erFødsel = erFødsel;
            return this;
        }

        public HbSak.Builder medErAdopsjon(boolean erAdopsjon) {
            kladd.erAdopsjon = erAdopsjon;
            return this;
        }

        public HbSak.Builder medAntallBarn(int antallBarn) {
            kladd.antallBarn = antallBarn;
            return this;
        }

        public HbSak.Builder medDatoFagsakvedtak(LocalDate datoFagsakvedtak) {
            kladd.datoFagsakvedtak = datoFagsakvedtak;
            return this;
        }

        public HbSak build() {
            Objects.requireNonNull(kladd.ytelsetype, "Ytelse type er ikke satt");
            if (!FagsakYtelseType.FRISINN.equals(kladd.ytelsetype)) {
                Objects.requireNonNull(kladd.antallBarn, "antallBarn er ikke satt");
            }
            if (trengerSkilleFødselOgAdopsjon(kladd.ytelsetype)) {
                if (kladd.erAdopsjon == kladd.erFødsel) {
                    throw new IllegalArgumentException("En og bare en av fødsel og adopsjon skal være satt");
                }
            }
            return kladd;
        }
    }

    private static boolean trengerSkilleFødselOgAdopsjon(FagsakYtelseType fagsakYtelseType) {
        return Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER, FagsakYtelseType.ENGANGSTØNAD)
                .contains(fagsakYtelseType);
    }
}
