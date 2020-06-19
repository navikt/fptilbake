package no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;

public class K9sakBehandlingInfoDto extends EksternBehandlingsinfoDto {

    private List<BehandlingResourceLinkDto> links = new ArrayList<>();

    public List<BehandlingResourceLinkDto> getLinks() {
        return links;
    }

    public void setLinks(List<BehandlingResourceLinkDto> links) {
        this.links = links;
    }
}
