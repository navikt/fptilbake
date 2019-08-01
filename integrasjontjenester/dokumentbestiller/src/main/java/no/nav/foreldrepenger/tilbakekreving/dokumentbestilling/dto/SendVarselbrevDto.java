package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SendVarselbrevDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @JsonProperty("aktoerId")
    @NotNull
    @Digits(integer = 50, fraction = 0)
    private String aktørId;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long fagsakId;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getAktørId() {
        return aktørId;
    }

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public void setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId())
                .leggTil(StandardAbacAttributtType.AKTØR_ID, getAktørId())
                .leggTil(StandardAbacAttributtType.FAGSAK_ID, getFagsakId());
    }
}
