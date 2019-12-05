package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ProsessTaskGruppeIdDto implements AbacDto {

    @Size(min = 1, max = 250)
    @Pattern(regexp = "[a-zA-Z0-9-.]+")
    private String gruppe;

    public ProsessTaskGruppeIdDto() {
        gruppe = null; // NOSONAR
    }

    public ProsessTaskGruppeIdDto(String gruppe) {
        this.gruppe = gruppe;
    }

    public String getGruppe() {
        return gruppe;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); // Har ikke noe å bidra med her
    }

    @Override
    public String toString() {
        return "BehandlingIdDto{" +
            "behandlingId=" + gruppe +
            '}';
    }
}
