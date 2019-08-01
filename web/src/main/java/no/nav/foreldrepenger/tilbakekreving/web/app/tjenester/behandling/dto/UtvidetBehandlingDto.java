package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class UtvidetBehandlingDto extends BehandlingDto {

    @JsonProperty("behandlingPaaVent")
    private boolean behandlingPåVent;

    @JsonProperty("behandlingKoet")
    private boolean behandlingKøet;

    @JsonProperty("ansvarligSaksbehandler")
    private String ansvarligSaksbehandler;

    @JsonProperty("fristBehandlingPaaVent")
    private String fristBehandlingPåVent;

    @JsonProperty("venteArsakKode")
    private String venteÅrsakKode;

    @JsonProperty("sprakkode")
    private Språkkode språkkode;

    @JsonProperty("behandlingHenlagt")
    private boolean behandlingHenlagt;

    @JsonProperty("originalBehandlingId")
    private Long originalBehandlingId;

    public boolean isBehandlingPåVent() {
        return behandlingPåVent;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getFristBehandlingPåVent() {
        return fristBehandlingPåVent;
    }

    public String getVenteÅrsakKode() {
        return venteÅrsakKode;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public boolean isBehandlingHenlagt() {
        return behandlingHenlagt;
    }

    public Long getOriginalBehandlingId() {
        return originalBehandlingId;
    }

    void setBehandlingPåVent(boolean behandlingPåVent) {
        this.behandlingPåVent = behandlingPåVent;
    }

    void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
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

    void setBehandlingHenlagt(boolean behandlingHenlagt) {
        this.behandlingHenlagt = behandlingHenlagt;
    }

    public boolean isBehandlingKoet() {
        return behandlingKøet;
    }

    public void setBehandlingKøet(boolean behandlingKøet) {
        this.behandlingKøet = behandlingKøet;
    }

    public void setOriginalBehandlingId(Long originalBehandlingId) {
        this.originalBehandlingId = originalBehandlingId;
    }
}
