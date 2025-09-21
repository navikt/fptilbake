package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FpsakBehandlingFullDto(@NotNull FpsakBehandlingDto behandling,
                                     @NotNull FpsakFagsakDto fagsak,
                                     @NotNull FamilieHendelseDto familieHendelse,
                                     String varseltekst,
                                     FeilutbetalingValg feilutbetalingValg,
                                     Boolean sendtoppdrag,
                                     FpsakVergeDto verge) {

    public enum YtelseType { FORELDREPENGER, SVANGERSKAPSPENGER, ENGANGSSTØNAD }

    public enum FamilieHendelseType { FØDSEL, ADOPSJON }

    public enum VergeType { BARN, FORELDRELØS, VOKSEN, ADVOKAT, FULLMEKTIG }

    public enum FeilutbetalingValg { OPPRETT, OPPDATER, IGNORER, INNTREKK }

    public enum Språkkode { NB, NN, EN }

    public record FpsakBehandlingDto(@NotNull UUID uuid, @NotNull HenvisningDto henvisning,
                                     String behandlendeEnhetId, String behandlendeEnhetNavn,
                                     Språkkode språkkode, LocalDate vedtaksdato) {}

    public record FamilieHendelseDto(@NotNull FamilieHendelseType familieHendelseType, @NotNull Integer antallBarn) {}

    public record FpsakFagsakDto(@NotNull String aktørId, @NotNull String saksnummer, @NotNull YtelseType fagsakYtelseType) { }

    public record HenvisningDto(@NotNull @Min(0) @Max(Long.MAX_VALUE)Long henvisning) { }

    public record FpsakVergeDto(@NotNull VergeType vergeType, String aktørId, String navn, String fnr,
                                @NotNull LocalDate gyldigFom, @NotNull LocalDate gyldigTom, String organisasjonsnummer) { }

}
