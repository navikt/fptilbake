package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;

import java.time.LocalDate;

public record OpprettVerge(
        String navn,
        String fnr,
        LocalDate gyldigFom,
        LocalDate gyldigTom,
        VergeType vergeType,
        String organisasjonsnummer,
        String begrunnelse
) {
    public OpprettVerge {
        if ((organisasjonsnummer == null && fnr == null) || (organisasjonsnummer != null && fnr != null)) {
            throw new IllegalArgumentException("Verge må ha enten fnr eller organisasjonsnummer oppgitt.");
        }
        if (VergeType.ADVOKAT.equals(vergeType)) {
            if (organisasjonsnummer == null) {
                throw new IllegalArgumentException(String.format("Verge av type %s må ha organisasjonsnummer oppgitt.", vergeType.getKode()));
            }
        } else {
            if (fnr == null) {
                throw new IllegalArgumentException(String.format("Verge av type %s må ha fnr oppgitt.", vergeType.getKode()));
            }
        }
    }
}