package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FpsakBehandlingInfoDto extends EksternBehandlingsinfoDto {

    private Long id;
    private List<BehandlingResourceLinkDto> links = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BehandlingResourceLinkDto> getLinks() {
        return links;
    }

    public void setLinks(List<BehandlingResourceLinkDto> links) {
        this.links = links;
    }

}
