package no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingMetode;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.RevurderingOpprettetÅrsak;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BehandlingTilstand {
    private OffsetDateTime funksjonellTid;
    private OffsetDateTime tekniskTid;
    private String saksnummer;
    private YtelseType ytelseType;
    private UUID behandlingUuid;
    private UUID referertFagsakBehandlingUuid;
    private BehandlingType behandlingType;
    private BehandlingStatus behandlingStatus;
    @JsonProperty(value = "venterPaaBruker")
    private boolean venterPåBruker;
    @JsonProperty(value = "venterPaaOekonomi")
    private boolean venterPåØkonomi;
    private BehandlingResultat behandlingResultat;
    private BehandlingMetode behandlingMetode;
    private OffsetDateTime registrertTid;
    private OffsetDateTime ferdigBehandletTid;
    private boolean erBehandlingManueltOpprettet;
    private String opprettetAv;
    private String behandlendeEnhetKode;
    private String ansvarligSaksbehandler;
    private String ansvarligBeslutter;

    private UUID forrigeBehandling;
    @JsonProperty(value = "revurderingOpprettetAarsak")
    private RevurderingOpprettetÅrsak revurderingOpprettetÅrsak;

    @JsonProperty(value = "totalFeilutbetaltPeriode")
    private Periode totalFeilutbetaltPeriode;

    @JsonProperty(value = "totalFeilutbetaltBeloep")
    private BigDecimal totalFeilutbetaltBeløp;

    public OffsetDateTime getFunksjonellTid() {
        return funksjonellTid;
    }

    public void setFunksjonellTid(OffsetDateTime funksjonellTid) {
        this.funksjonellTid = funksjonellTid;
    }

    public OffsetDateTime getTekniskTid() {
        return tekniskTid;
    }

    public void setTekniskTid(OffsetDateTime tekniskTid) {
        this.tekniskTid = tekniskTid;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    public UUID getReferertFagsakBehandlingUuid() {
        return referertFagsakBehandlingUuid;
    }

    public void setReferertFagsakBehandlingUuid(UUID referertFagsakBehandlingUuid) {
        this.referertFagsakBehandlingUuid = referertFagsakBehandlingUuid;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public void setBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
    }

    public BehandlingStatus getBehandlingStatus() {
        return behandlingStatus;
    }

    public void setBehandlingStatus(BehandlingStatus behandlingStatus) {
        this.behandlingStatus = behandlingStatus;
    }

    public BehandlingResultat getBehandlingResultat() {
        return behandlingResultat;
    }

    public void setBehandlingResultat(BehandlingResultat behandlingResultat) {
        this.behandlingResultat = behandlingResultat;
    }

    public BehandlingMetode getBehandlingMetode() {
        return behandlingMetode;
    }

    public void setBehandlingMetode(BehandlingMetode behandlingMetode) {
        this.behandlingMetode = behandlingMetode;
    }

    public OffsetDateTime getRegistrertTid() {
        return registrertTid;
    }

    public void setRegistrertTid(OffsetDateTime registrertTid) {
        this.registrertTid = registrertTid;
    }

    public OffsetDateTime getFerdigBehandletTid() {
        return ferdigBehandletTid;
    }

    public void setFerdigBehandletTid(OffsetDateTime ferdigBehandletTid) {
        this.ferdigBehandletTid = ferdigBehandletTid;
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public void setOpprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
    }

    public boolean erBehandlingManueltOpprettet() {
        return erBehandlingManueltOpprettet;
    }

    public void setErBehandlingManueltOpprettet(boolean erBehandlingManueltOpprettet) {
        this.erBehandlingManueltOpprettet = erBehandlingManueltOpprettet;
    }

    public boolean venterPåBruker() {
        return venterPåBruker;
    }

    public void setVenterPåBruker(boolean venterPåBruker) {
        this.venterPåBruker = venterPåBruker;
    }

    public boolean venterPåØkonomi() {
        return venterPåØkonomi;
    }

    public void setVenterPåØkonomi(boolean venterPåØkonomi) {
        this.venterPåØkonomi = venterPåØkonomi;
    }

    public String getBehandlendeEnhetKode() {
        return behandlendeEnhetKode;
    }

    public void setBehandlendeEnhetKode(String behandlendeEnhetKode) {
        this.behandlendeEnhetKode = behandlendeEnhetKode;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public void setAnsvarligBeslutter(String ansvarligBeslutter) {
        this.ansvarligBeslutter = ansvarligBeslutter;
    }

    public UUID getForrigeBehandling() {
        return forrigeBehandling;
    }

    public void setForrigeBehandling(UUID forrigeBehandling) {
        this.forrigeBehandling = forrigeBehandling;
    }

    public RevurderingOpprettetÅrsak getRevurderingOpprettetÅrsak() {
        return revurderingOpprettetÅrsak;
    }

    public void setRevurderingOpprettetÅrsak(RevurderingOpprettetÅrsak revurderingOpprettetÅrsak) {
        this.revurderingOpprettetÅrsak = revurderingOpprettetÅrsak;
    }

    public Periode getTotalFeilutbetaltPeriode() {
        return totalFeilutbetaltPeriode;
    }

    public void setTotalFeilutbetaltPeriode(Periode totalFeilutbetaltPeriode) {
        this.totalFeilutbetaltPeriode = totalFeilutbetaltPeriode;
    }

    public BigDecimal getTotalFeilutbetaltBeløp() {
        return totalFeilutbetaltBeløp;
    }

    public void setTotalFeilutbetaltBeløp(BigDecimal totalFeilutbetaltBeløp) {
        this.totalFeilutbetaltBeløp = totalFeilutbetaltBeløp;
    }
}
