package no.nav.foreldrepenger.tilbakekreving.pip;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public class PipBehandlingData {

    private Long behandlingId;
    private String statusForBehandling;
    private String ansvarligSaksbehandler;
    private String saksnummer;
    private String fagsakstatus;
    private Set<AktørId> aktørId = new HashSet<>();

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getStatusForBehandling() {
        return statusForBehandling;
    }

    public void setStatusForBehandling(String statusForBehandling) {
        this.statusForBehandling = statusForBehandling;
    }

    public Optional<String> getAnsvarligSaksbehandler() {
        return Optional.ofNullable(ansvarligSaksbehandler);
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getFagsakstatus() {
        return fagsakstatus;
    }

    public void setFagsakstatus(String fagsakstatus) {
        this.fagsakstatus = fagsakstatus;
    }

    public Set<AktørId> getAktørId() {
        return aktørId;
    }

    public Set<String> getAktørIdSomStrenger() {
        return aktørId.stream()
            .map(AktørId::getId)
            .collect(Collectors.toSet());
    }

    public void leggTilAktørId(Set<AktørId> aktørId) {
        this.aktørId.addAll(aktørId);
    }

    public void leggTilAktørId(AktørId aktørId) {
        this.aktørId.add(aktørId);
    }

    public Boolean isEmpty() {
        return Objects.isNull(behandlingId) &&
            Objects.isNull(statusForBehandling) &&
            Objects.isNull(ansvarligSaksbehandler) &&
            Objects.isNull(saksnummer) &&
            Objects.isNull(fagsakstatus) &&
            aktørId.isEmpty();
    }

}
