package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EksternBehandlingsinfoDto {

    private Long id;
    private String saksnummer;
    private Long fagsakId;
    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;
    private String ansvarligSaksbehandler;
    private Språkkode sprakkode;
    private List<BehandlingResourceLinkDto> links = new ArrayList<>();
    private PersonopplysningDto personopplysningDto;
    private KodeDto fagsaktype;
    private String varseltekst;

    public String getVarseltekst() {
        return varseltekst;
    }

    public void setVarseltekst(String varseltekst) {
        this.varseltekst = varseltekst;
    }

    public KodeDto getFagsaktype() {
        return fagsaktype;
    }

    public void setFagsaktype(KodeDto fagsaktype) {
        this.fagsaktype = fagsaktype;
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

    public PersonopplysningDto getPersonopplysningDto() {
        return personopplysningDto;
    }

    public void setPersonopplysningDto(PersonopplysningDto personopplysningDto) {
        this.personopplysningDto = personopplysningDto;
    }

    public Språkkode getSprakkode() {
        return sprakkode;
    }

    public void setSprakkode(Språkkode sprakkode) {
        this.sprakkode = sprakkode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<BehandlingResourceLinkDto> getLinks() {
        return links;
    }

    public void setLinks(List<BehandlingResourceLinkDto> links) {
        this.links = links;
    }
}
