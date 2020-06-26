package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class BekreftedeAksjonspunkterDto implements AbacDto {

    @Valid
    @NotNull
    private BehandlingReferanse behandlingId;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingVersjon;

    @Size(min = 1, max = 10)
    @Valid
    private Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer;

    public static BekreftedeAksjonspunkterDto lagDto(Long behandlingId,
                                                     Long behandlingVersjon,
                                                     Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer) {
        BekreftedeAksjonspunkterDto dto = new BekreftedeAksjonspunkterDto();
        dto.behandlingId = new BehandlingReferanse(behandlingId);
        dto.behandlingVersjon = behandlingVersjon;
        dto.bekreftedeAksjonspunktDtoer = bekreftedeAksjonspunktDtoer;
        return dto;
    }

    public BehandlingReferanse getBehandlingId() {
        return behandlingId;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
    }

    public Collection<BekreftetAksjonspunktDto> getBekreftedeAksjonspunktDtoer() {
        return bekreftedeAksjonspunktDtoer;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abac = AbacDataAttributter.opprett().leggTil(
            StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId().getBehandlingId());
        bekreftedeAksjonspunktDtoer.forEach(apDto -> abac.leggTil(apDto.abacAttributter()));
        return abac;
    }
}
