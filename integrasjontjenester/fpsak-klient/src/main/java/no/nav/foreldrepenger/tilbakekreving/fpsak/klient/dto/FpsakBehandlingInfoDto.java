package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FpsakBehandlingInfoDto extends EksternBehandlingsinfoDto {


    public static FpsakBehandlingInfoDto fraFullDto(FpsakTilbakeDto dto) {
        var info = new FpsakBehandlingInfoDto();
        info.setHenvisning(Henvisning.fraEksternBehandlingId(dto.behandling().henvisning().henvisning()));
        info.setUuid(dto.behandling().uuid());
        info.setBehandlendeEnhetId(dto.behandling().behandlendeEnhetId());
        info.setBehandlendeEnhetNavn(dto.behandling().behandlendeEnhetNavn());
        info.setVedtakDato(dto.behandling().vedtaksdato());
        info.setSprakkode(switch (dto.behandling().språkkode()) {
            case NB -> no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode.NB;
            case NN -> no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode.NN;
            case EN -> no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode.EN;
            case null -> no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode.NB;
        });
        return info;
    }

}
