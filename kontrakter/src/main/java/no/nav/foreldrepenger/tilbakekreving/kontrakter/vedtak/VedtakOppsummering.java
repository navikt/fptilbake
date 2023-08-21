package no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;

public class VedtakOppsummering {
    @NotNull
    @Size(min = 1, max = 20)
    private String saksnummer;
    @NotNull
    private YtelseType ytelseType;
    @NotNull
    private UUID behandlingUuid;
    private UUID forrigeBehandling;
    @NotNull
    private UUID referertFagsakBehandlingUuid;
    @NotNull
    private BehandlingType behandlingType;
    private boolean erBehandlingManueltOpprettet;
    @NotNull
    private String behandlendeEnhetKode;
    @NotNull
    private String ansvarligSaksbehandler;
    @NotNull
    private String ansvarligBeslutter;
    @NotNull
    private OffsetDateTime behandlingOpprettetTid;
    @NotNull
    private OffsetDateTime vedtakFattetTid;
    @NotNull
    @Size(min = 1, max = 100)
    private List<VedtakPeriode> perioder;

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

    public UUID getForrigeBehandling() {
        return forrigeBehandling;
    }

    public void setForrigeBehandling(UUID forrigeBehandling) {
        this.forrigeBehandling = forrigeBehandling;
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

    public boolean isErBehandlingManueltOpprettet() {
        return erBehandlingManueltOpprettet;
    }

    public void setErBehandlingManueltOpprettet(boolean erBehandlingManueltOpprettet) {
        this.erBehandlingManueltOpprettet = erBehandlingManueltOpprettet;
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

    public OffsetDateTime getBehandlingOpprettetTid() {
        return behandlingOpprettetTid;
    }

    public void setBehandlingOpprettetTid(OffsetDateTime behandlingOpprettetTid) {
        this.behandlingOpprettetTid = behandlingOpprettetTid;
    }

    public OffsetDateTime getVedtakFattetTid() {
        return vedtakFattetTid;
    }

    public void setVedtakFattetTid(OffsetDateTime vedtakFattetTid) {
        this.vedtakFattetTid = vedtakFattetTid;
    }

    public List<VedtakPeriode> getPerioder() {
        return perioder;
    }

    public void setPerioder(List<VedtakPeriode> perioder) {
        this.perioder = perioder;
    }
}
