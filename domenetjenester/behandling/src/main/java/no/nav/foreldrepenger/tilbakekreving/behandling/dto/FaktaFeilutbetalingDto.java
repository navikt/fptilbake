package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.time.LocalDate;

import javax.validation.Valid;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class FaktaFeilutbetalingDto {

    private LocalDate fom;

    private LocalDate tom;

    @Valid
    private HendelseTypeMedUndertypeDto årsak;

    FaktaFeilutbetalingDto() {
        // For CDI
    }

    public FaktaFeilutbetalingDto(LocalDate fom, LocalDate tom, HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto) {
        this.fom = fom;
        this.tom = tom;
        this.årsak = feilutbetalingÅrsakDto;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Periode tilPeriode() {
        return Periode.of(fom, tom);
    }

    public HendelseType getHendelseType() {
        return årsak.getHendelseType();
    }

    public HendelseUnderType getHendelseUndertype() {
        return årsak.getHendelseUndertype();
    }

}
