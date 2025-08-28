package no.nav.foreldrepenger.tilbakekreving.historikk;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;

public record HistorikkinnslagDto(@NotNull UUID behandlingUuid,
                                  @NotNull HistorikkAktørDto aktør,
                                  SkjermlenkeType skjermlenke,
                                  @NotNull LocalDateTime opprettetTidspunkt,
                                  @NotNull List<HistorikkInnslagDokumentLinkDto> dokumenter,
                                  String tittel,
                                  @NotNull List<Linje> linjer) {

    public HistorikkinnslagDto {
        Objects.requireNonNull(behandlingUuid);
        Objects.requireNonNull(aktør);
        Objects.requireNonNull(opprettetTidspunkt);
        if(dokumenter == null) {
            dokumenter = List.of();
        }
        if(linjer == null) {
            linjer = List.of();
        }
    }

    public record HistorikkAktørDto(@NotNull HistorikkAktør type, String ident) {
        public HistorikkAktørDto {
            if(type == null) {
                type = HistorikkAktør.UDEFINERT;
            }
        }

        public static HistorikkAktørDto fra(HistorikkAktør aktør, String opprettetAv) {
            if (Set.of(HistorikkAktør.SAKSBEHANDLER, HistorikkAktør.BESLUTTER).contains(aktør)) {
                return new HistorikkAktørDto(aktør, opprettetAv);
            }
            return new HistorikkAktørDto(aktør, null);
        }
    }

    public record Linje(@NotNull Type type, String tekst) {
        public Linje {
            Objects.requireNonNull(type);
        }

        public static Linje tekstlinje(String tekst) {
            return new Linje(Type.TEKST, tekst);
        }

        public static Linje linjeskift() {
            return new Linje(Type.LINJESKIFT, null);
        }

        public boolean erLinjeskift() {
            return Type.LINJESKIFT.equals(type);
        }

        public enum Type {
            TEKST,
            LINJESKIFT
        }
    }

}
