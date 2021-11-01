package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;

@JsonTypeName(AksjonspunktKodeDefinisjon.VURDER_FORELDELSE)
public class VurderForeldelseDto extends BekreftetAksjonspunktDto {

    @Valid
    @Size(min = 1)
    private List<ForeldelsePeriodeDto> foreldelsePerioder = new ArrayList<>();

    public VurderForeldelseDto() {
        super();
    }


    public List<ForeldelsePeriodeDto> getForeldelsePerioder() {
        return foreldelsePerioder;
    }

    public void setForeldelsePerioder(List<ForeldelsePeriodeDto> foreldelsePerioder) {
        this.foreldelsePerioder = foreldelsePerioder;
    }
}
