package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagDokumentLinkDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record HistorikkinnslagDtoV2(UUID behandlingUuid,
                                    HistorikkAktørDto aktør,
                                    SkjermlenkeType skjermlenke,
                                    LocalDateTime opprettetTidspunkt,
                                    List<HistorikkInnslagDokumentLinkDto> dokumenter,
                                    String tittel,
                                    List<String> body) {

    public record HistorikkAktørDto(HistorikkAktør type, String ident) {
        public static HistorikkAktørDto fra(HistorikkAktør aktør, String opprettetAv) {
            if (Set.of(HistorikkAktør.SAKSBEHANDLER, HistorikkAktør.BESLUTTER).contains(aktør)) {
                return new HistorikkAktørDto(aktør, opprettetAv);
            }
            return new HistorikkAktørDto(aktør, null);
        }
    }

}
