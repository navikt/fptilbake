package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;

public class BehandlingDto {

    private Long id;
    private Long versjon;
    private BehandlingType type;
    private BehandlingStatus status;
    private Long fagsakId;
    private LocalDateTime opprettet;
    private LocalDateTime avsluttet;
    private LocalDateTime endret;
    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;
    private boolean toTrinnsBehandling;


    /**
     * REST HATEOAS - pekere på data innhold som hentes fra andre url'er, eller handlinger som er tilgjengelig på behandling.
     *
     * @see https://restfulapi.net/hateoas/
     */
    private List<ResourceLink> links = new ArrayList<>();

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getId() {
        return id;
    }

    public Long getVersjon() {
        return versjon;
    }

    public BehandlingType getType() {
        return type;
    }

    public LocalDateTime getOpprettet() {
        return opprettet;
    }

    public LocalDateTime getAvsluttet() {
        return avsluttet;
    }

    public BehandlingStatus getStatus() {
        return status;
    }

    public LocalDateTime getEndret() {
        return endret;
    }

    public String getBehandlendeEnhetId() {
        return behandlendeEnhetId;
    }

    public String getBehandlendeEnhetNavn() {
        return behandlendeEnhetNavn;
    }

    public List<ResourceLink> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public boolean getToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    void setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setVersjon(Long versjon) {
        this.versjon = versjon;
    }

    void setType(BehandlingType type) {
        this.type = type;
    }

    void setOpprettet(LocalDateTime opprettet) {
        this.opprettet = opprettet;
    }

    void setEndret(LocalDateTime endret) {
        this.endret = endret;
    }

    void setAvsluttet(LocalDateTime avsluttet) {
        this.avsluttet = avsluttet;
    }

    void setStatus(BehandlingStatus status) {
        this.status = status;
    }

    void setBehandlendeEnhetId(String behandlendeEnhetId) {
        this.behandlendeEnhetId = behandlendeEnhetId;
    }

    void setBehandlendeEnhetNavn(String behandlendeEnhetNavn) {
        this.behandlendeEnhetNavn = behandlendeEnhetNavn;
    }

    void leggTil(ResourceLink link) {
        links.add(link);
    }

    void setToTrinnsBehandling(boolean toTrinnsBehandling) {
        this.toTrinnsBehandling = toTrinnsBehandling;
    }
}
