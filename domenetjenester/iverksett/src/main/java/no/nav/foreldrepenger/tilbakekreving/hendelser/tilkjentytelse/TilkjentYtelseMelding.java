package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.PeriodeDto;

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
    @JsonProperty("varselTekst")
    private String varselTekst;
    @JsonProperty("varselBeloep")
    private Long varselBeløp;
    @JsonProperty("tilbakekrevingValg")
    private String tilbakekrevingValg;
    @JsonProperty("simulertPeriode")
    private List<PeriodeDto> simulertPeriode;

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

    public String getVarselTekst() {
        return varselTekst;
    }

    public Long getVarselBeløp() {
        return varselBeløp;
    }

    public String getTilbakekrevingValg() {
        return tilbakekrevingValg;
    }

    public void setVarselTekst(String varselTekst) {
        this.varselTekst = varselTekst;
    }

    public void setVarselBeløp(Long varselBeløp) {
        this.varselBeløp = varselBeløp;
    }

    public void setTilbakekrevingValg(String tilbakekrevingValg) {
        this.tilbakekrevingValg = tilbakekrevingValg;
    }

    public List<PeriodeDto> getSimulertPeriode() {
        return simulertPeriode;
    }

    public void setSimulertPeriode(List<PeriodeDto> simulertPeriode) {
        this.simulertPeriode = simulertPeriode;
    }
}
