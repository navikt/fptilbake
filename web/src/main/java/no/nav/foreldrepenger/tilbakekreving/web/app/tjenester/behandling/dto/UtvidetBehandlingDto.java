package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class UtvidetBehandlingDto extends BehandlingDto {

    @JsonProperty("behandlingKoet")
    private boolean behandlingKøet;

    @JsonProperty("ansvarligSaksbehandler")
    private String ansvarligSaksbehandler;

    @JsonProperty("sprakkode")
    private Språkkode språkkode;

    @JsonProperty("behandlingHenlagt")
    private boolean behandlingHenlagt;

    @JsonProperty("originalBehandlingId")
    private Long originalBehandlingId;

    /** Eventuelt async status på tasks. */
    @JsonProperty("taskStatus")
    private AsyncPollingStatus taskStatus;

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
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

    public AsyncPollingStatus getTaskStatus() {
        return taskStatus;
    }

    void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
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

    public void setTaskStatus(AsyncPollingStatus taskStatus) {
        this.taskStatus = taskStatus;
    }
}
