package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class TilkjentYtelseMelding {

    @JsonProperty("fagsakYtelseType")
    private String fagsakYtelseType;
    @JsonProperty("gsakSaksnummer")
    private String saksnummer;
    @JsonProperty("aktoerId")
    private String aktørId;
    @JsonProperty("ivSystem")
    private String iverksettingSystem = "fpsak";
    @JsonProperty("behandlingId")
    private Long behandlingId;
    @JsonProperty("behandlingUuid")
    private UUID behandlingUuid;

    public String getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public void setFagsakYtelseType(String fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

    public Saksnummer getSaksnummer() {
        return new Saksnummer(saksnummer);
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public AktørId getAktørId() {
        return new AktørId(aktørId);
    }

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    public String getIverksettingSystem() {
        return iverksettingSystem;
    }

    public void setIverksettingSystem(String iverksettingSystem) {
        this.iverksettingSystem = iverksettingSystem;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }
}
