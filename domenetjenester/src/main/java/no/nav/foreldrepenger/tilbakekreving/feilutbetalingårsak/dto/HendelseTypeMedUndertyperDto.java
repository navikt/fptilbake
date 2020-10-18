package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public class HendelseTypeMedUndertyperDto {

    private HendelseType hendelseType;

    private List<HendelseUnderType> hendelseUndertyper;

    public HendelseTypeMedUndertyperDto(HendelseType hendelseType, List<HendelseUnderType> undertyper) {
        this.hendelseType = hendelseType;
        this.hendelseUndertyper = undertyper;
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public List<HendelseUnderType> getHendelseUndertyper() {
        return hendelseUndertyper;
    }
}
