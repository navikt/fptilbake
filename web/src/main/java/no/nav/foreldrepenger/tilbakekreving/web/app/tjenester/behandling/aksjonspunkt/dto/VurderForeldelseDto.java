package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;

@JsonTypeName(VurderForeldelseDto.AKSJONSPUNKT_KODE)
public class VurderForeldelseDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5003";

    @Valid
    @Size(min = 1)
    private List<ForeldelsePeriodeDto> foreldelsePerioder = new ArrayList<>();

    public VurderForeldelseDto() {
        super();
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public List<ForeldelsePeriodeDto> getForeldelsePerioder() {
        return foreldelsePerioder;
    }

    public void setForeldelsePerioder(List<ForeldelsePeriodeDto> foreldelsePerioder) {
        this.foreldelsePerioder = foreldelsePerioder;
    }
}
