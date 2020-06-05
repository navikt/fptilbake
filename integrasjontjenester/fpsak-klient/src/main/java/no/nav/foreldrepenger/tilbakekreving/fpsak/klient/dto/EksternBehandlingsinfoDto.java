package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EksternBehandlingsinfoDto {

    private Long id; //FIXME k9-tilbake fjern id fra felles EksternBehandlignsinfoDto
    private Henvisning henvisning;
    private UUID uuid;
    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;
    private Språkkode sprakkode;
    @JsonProperty("originalVedtaksDato")
    private LocalDate vedtakDato;
    private BehandlingsresultatDto behandlingsresultat;
    @JsonProperty("behandlingArsaker")
    private List<BehandlingÅrsakDto> behandlingÅrsaker = new ArrayList<>();
    private List<BehandlingResourceLinkDto> links = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Henvisning getHenvisning() {
        return henvisning;
    }

    public void setHenvisning(Henvisning henvisning) {
        this.henvisning = henvisning;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public Optional<Språkkode> getSpråkkode() {
        return Språkkode.UDEFINERT.equals(sprakkode)
            ? Optional.empty()
            : Optional.ofNullable(sprakkode);
    }

    public Språkkode getSpråkkodeEllerDefault() {
        return getSpråkkode().orElse(Språkkode.DEFAULT);
    }

    public void setSprakkode(Språkkode sprakkode) {
        this.sprakkode = sprakkode;
    }

    public LocalDate getVedtakDato() {
        return vedtakDato;
    }

    public void setVedtakDato(LocalDate vedtakDato) {
        this.vedtakDato = vedtakDato;
    }

    public BehandlingsresultatDto getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public void setBehandlingsresultat(BehandlingsresultatDto behandlingsresultat) {
        this.behandlingsresultat = behandlingsresultat;
    }

    public List<BehandlingÅrsakDto> getBehandlingÅrsaker() {
        return behandlingÅrsaker;
    }

    public void setBehandlingÅrsaker(List<BehandlingÅrsakDto> behandlingÅrsaker) {
        this.behandlingÅrsaker = behandlingÅrsaker;
    }

    public List<BehandlingResourceLinkDto> getLinks() {
        return links;
    }

    public void setLinks(List<BehandlingResourceLinkDto> links) {
        this.links = links;
    }

}
