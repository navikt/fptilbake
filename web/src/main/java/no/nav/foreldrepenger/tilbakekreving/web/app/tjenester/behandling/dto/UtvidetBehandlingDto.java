package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDto;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class UtvidetBehandlingDto extends BehandlingDto {

    @JsonProperty("behandlingHenlagt")
    private boolean behandlingHenlagt;

    @JsonProperty("originalBehandlingId")
    private Long originalBehandlingId;

    @JsonProperty("aksjonspunktene")
    private Set<AksjonspunktDto> aksjonspunktene;

    /**
     * Eventuelt async status på tasks.
     */
    @JsonProperty("taskStatus")
    private AsyncPollingStatus taskStatus;

    public boolean isBehandlingHenlagt() {
        return behandlingHenlagt;
    }

    public Long getOriginalBehandlingId() {
        return originalBehandlingId;
    }

    public Set<AksjonspunktDto> getAksjonspunktene() {
        return aksjonspunktene;
    }

    public AsyncPollingStatus getTaskStatus() {
        return taskStatus;
    }

    void setBehandlingHenlagt(boolean behandlingHenlagt) {
        this.behandlingHenlagt = behandlingHenlagt;
    }

    public void setOriginalBehandlingId(Long originalBehandlingId) {
        this.originalBehandlingId = originalBehandlingId;
    }

    public void setTaskStatus(AsyncPollingStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setAksjonspunktene(Set<AksjonspunktDto> aksjonspunktene) {
        this.aksjonspunktene = aksjonspunktene;
    }
}
