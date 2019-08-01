package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn;

import java.util.List;

public class TotrinnskontrollSkjermlenkeContextDto {

    private String skjermlenkeType;
    private List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter;

    public TotrinnskontrollSkjermlenkeContextDto(String skjermlenkeType, List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter) {
        this.totrinnskontrollAksjonspunkter = totrinnskontrollAksjonspunkter;
        this.skjermlenkeType = skjermlenkeType;
    }

    public String getSkjermlenkeType() {
        return skjermlenkeType;
    }

    public List<TotrinnskontrollAksjonspunkterDto> getTotrinnskontrollAksjonspunkter() {
        return totrinnskontrollAksjonspunkter;
    }
}
