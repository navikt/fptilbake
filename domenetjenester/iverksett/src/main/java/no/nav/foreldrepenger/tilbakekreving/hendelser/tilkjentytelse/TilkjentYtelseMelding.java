package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TilkjentYtelseMelding {

    private Long fagsakId;
    private long behandlingId;
    @JsonProperty("aktoerId")
    private String aktørId;
    @JsonProperty("ivSystem")
    private String iverksettingSystem = "fpsak";

    public Long getFagsakId() {
        return fagsakId;
    }

    public TilkjentYtelseMelding setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
        return this;
    }

    public long getBehandlingId() {
        return behandlingId;
    }

    public TilkjentYtelseMelding setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
        return this;
    }

    public String getAktørId() {
        return aktørId;
    }

    public TilkjentYtelseMelding setAktørId(String aktørId) {
        this.aktørId = aktørId;
        return this;
    }

    public String getIverksettingSystem() {
        return iverksettingSystem;
    }

    public TilkjentYtelseMelding setIverksettingSystem(String iverksettingSystem) {
        this.iverksettingSystem = iverksettingSystem;
        return this;
    }
}
