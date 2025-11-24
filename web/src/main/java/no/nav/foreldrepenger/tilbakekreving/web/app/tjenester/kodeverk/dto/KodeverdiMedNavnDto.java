package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.dto;

import jakarta.validation.constraints.NotNull;

public record KodeverdiMedNavnDto(@NotNull String kode, @NotNull String navn) { }
