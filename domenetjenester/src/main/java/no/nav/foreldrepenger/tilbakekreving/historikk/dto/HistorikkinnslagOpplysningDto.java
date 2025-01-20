package no.nav.foreldrepenger.tilbakekreving.historikk.dto;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldFelt;

public class HistorikkinnslagOpplysningDto {

    private HistorikkOpplysningType opplysningType;
    private String tilVerdi;

    public HistorikkOpplysningType getOpplysningType() {
        return opplysningType;
    }

    public void setOpplysningType(HistorikkOpplysningType opplysningType) {
        this.opplysningType = opplysningType;
    }

    public String getTilVerdi() {
        return tilVerdi;
    }

    public void setTilVerdi(String tilVerdi) {
        this.tilVerdi = tilVerdi;
    }

    static List<HistorikkinnslagOpplysningDto> mapFra(List<HistorikkinnslagOldFelt> opplysninger) {
        return opplysninger.stream().map(HistorikkinnslagOpplysningDto::mapFra).collect(Collectors.toList());
    }

    private static HistorikkinnslagOpplysningDto mapFra(HistorikkinnslagOldFelt opplysning) {
        HistorikkinnslagOpplysningDto dto = new HistorikkinnslagOpplysningDto();
        HistorikkOpplysningType opplysningType = HistorikkOpplysningType.fraKode(opplysning.getNavn());
        dto.setOpplysningType(opplysningType);
        dto.setTilVerdi(opplysning.getTilVerdi());
        return dto;
    }
}
