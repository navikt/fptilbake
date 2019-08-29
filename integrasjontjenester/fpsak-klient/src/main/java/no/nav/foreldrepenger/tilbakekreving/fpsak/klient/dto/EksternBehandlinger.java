package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.ArrayList;
import java.util.List;

public class EksternBehandlinger {
    private List<EksternBehandlingsinfoDto> eksternBehandlingerInfo = new ArrayList<>();

    public List<EksternBehandlingsinfoDto> getEksternBehandlingerInfo() {
        return eksternBehandlingerInfo;
    }

    public void setEksternBehandlingerInfo(List<EksternBehandlingsinfoDto> eksternBehandlingerInfo) {
        this.eksternBehandlingerInfo = eksternBehandlingerInfo;
    }
}
