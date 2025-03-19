package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn.TotrinnskontrollSkjermlenkeContextDto;

public class BehandlingDto {

    private Long id;
    private UUID uuid;
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

    private boolean behandlingPåVent;
    private String fristBehandlingPåVent;
    private String venteÅrsakKode;
    private Språkkode språkkode;
    private boolean behandlingKøet;
    private String ansvarligSaksbehandler;

    private BehandlingÅrsakDto førsteÅrsak;
    private List<BehandlingÅrsakDto> behandlingÅrsaker;
    private boolean kanHenleggeBehandling;
    private boolean harVerge;
    private BehandlingsresultatDto behandlingsresultat;

    private BehandlingOperasjonerDto behandlingTillatteOperasjoner;
    private List<TotrinnskontrollSkjermlenkeContextDto> totrinnskontrollÅrsaker;
    private boolean totrinnskontrollReadonly = true;
    private List<BrevmalDto> brevmaler;

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

    public UUID getUuid() {
        return uuid;
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

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    @Deprecated(forRemoval = true)
    @JsonProperty("sprakkode")
    public Språkkode getSprakkode() {
        return språkkode;
    }

    public boolean isBehandlingKøet() {
        return behandlingKøet;
    }

    @Deprecated(forRemoval = true)
    @JsonProperty("behandlingKoet")
    public boolean isBehandlingKoet() {
        return behandlingKøet;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    void setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
    }

    void setId(Long id) {
        this.id = id;
    }

    void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public boolean isBehandlingPåVent() {
        return behandlingPåVent;
    }

    @Deprecated(forRemoval = true)
    @JsonProperty("behandlingPaaVent")
    public boolean isBehandlingPaaVent() {
        return behandlingPåVent;
    }

    public String getFristBehandlingPåVent() {
        return fristBehandlingPåVent;
    }

    @Deprecated(forRemoval = true)
    @JsonProperty("fristBehandlingPaaVent")
    public String getFristBehandlingPaaVent() {
        return fristBehandlingPåVent;
    }

    public String getVenteÅrsakKode() {
        return venteÅrsakKode;
    }

    @Deprecated(forRemoval = true)
    @JsonProperty("venteArsakKode")
    public String getVenteArsakKode() {
        return venteÅrsakKode;
    }

    void setBehandlingPåVent(boolean behandlingPåVent) {
        this.behandlingPåVent = behandlingPåVent;
    }

    void setFristBehandlingPåVent(String fristBehandlingPåVent) {
        this.fristBehandlingPåVent = fristBehandlingPåVent;
    }

    void setVenteÅrsakKode(String venteÅrsakKode) {
        this.venteÅrsakKode = venteÅrsakKode;
    }

    void setSpråkkode(Språkkode språkkode) {
        this.språkkode = språkkode;
    }

    void setBehandlingKøet(boolean behandlingKøet) {
        this.behandlingKøet = behandlingKøet;
    }

    void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public BehandlingÅrsakDto getFørsteÅrsak() {
        return førsteÅrsak;
    }

    public void setFørsteÅrsak(BehandlingÅrsakDto førsteÅrsak) {
        this.førsteÅrsak = førsteÅrsak;
    }

    public List<BehandlingÅrsakDto> getBehandlingÅrsaker() {
        return behandlingÅrsaker;
    }

    public void setBehandlingÅrsaker(List<BehandlingÅrsakDto> behandlingÅrsaker) {
        this.behandlingÅrsaker = behandlingÅrsaker;
    }

    public boolean isKanHenleggeBehandling() {
        return kanHenleggeBehandling;
    }

    public void setKanHenleggeBehandling(boolean kanHenleggeBehandling) {
        this.kanHenleggeBehandling = kanHenleggeBehandling;
    }

    public boolean isHarVerge() {
        return harVerge;
    }

    public void setHarVerge(boolean harVerge) {
        this.harVerge = harVerge;
    }

    public BehandlingsresultatDto getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public void setBehandlingsresultat(BehandlingsresultatDto behandlingsresultat) {
        this.behandlingsresultat = behandlingsresultat;
    }

    public BehandlingOperasjonerDto getBehandlingTillatteOperasjoner() {
        return behandlingTillatteOperasjoner;
    }

    public void setBehandlingTillatteOperasjoner(BehandlingOperasjonerDto behandlingTillatteOperasjoner) {
        this.behandlingTillatteOperasjoner = behandlingTillatteOperasjoner;
    }

    public List<TotrinnskontrollSkjermlenkeContextDto> getTotrinnskontrollÅrsaker() {
        return totrinnskontrollÅrsaker;
    }

    public void setTotrinnskontrollÅrsaker(List<TotrinnskontrollSkjermlenkeContextDto> totrinnskontrollÅrsaker) {
        this.totrinnskontrollÅrsaker = totrinnskontrollÅrsaker;
    }

    public boolean isTotrinnskontrollReadonly() {
        return totrinnskontrollReadonly;
    }

    public void setTotrinnskontrollReadonly(boolean totrinnskontrollReadonly) {
        this.totrinnskontrollReadonly = totrinnskontrollReadonly;
    }

    public List<BrevmalDto> getBrevmaler() {
        return brevmaler;
    }

    public void setBrevmaler(List<BrevmalDto> brevmaler) {
        this.brevmaler = brevmaler;
    }
}
