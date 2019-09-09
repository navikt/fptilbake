package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EksternBehandlingsinfoDto {

    private Long id;
    private UUID uuid;
    private String saksnummer;
    private Long fagsakId;
    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;
    private String ansvarligSaksbehandler;
    private Språkkode sprakkode;
    private List<BehandlingResourceLinkDto> links = new ArrayList<>();
    private KodeDto fagsaktype;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public void setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
    }

    public String getBehandlendeEnhetId() {
        return behandlendeEnhetId;
    }

    public void setBehandlendeEnhetId(String behandlendeEnhetId) {
        this.behandlendeEnhetId = behandlendeEnhetId;
    }

    public String getBehandlendeEnhetNavn() {
        return behandlendeEnhetNavn;
    }

    public void setBehandlendeEnhetNavn(String behandlendeEnhetNavn) {
        this.behandlendeEnhetNavn = behandlendeEnhetNavn;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public Språkkode getSprakkode() {
        return sprakkode;
    }

    public void setSprakkode(Språkkode sprakkode) {
        this.sprakkode = sprakkode;
    }

    public List<BehandlingResourceLinkDto> getLinks() {
        return links;
    }

    public void setLinks(List<BehandlingResourceLinkDto> links) {
        this.links = links;
    }

    public KodeDto getFagsaktype() {
        return fagsaktype;
    }

    public void setFagsaktype(KodeDto fagsaktype) {
        this.fagsaktype = fagsaktype;
    }
}
