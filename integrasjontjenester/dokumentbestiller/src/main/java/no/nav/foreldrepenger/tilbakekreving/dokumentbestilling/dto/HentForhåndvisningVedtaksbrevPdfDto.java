package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndvisningVedtaksbrevPdfDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Size(max = 1500, message = "Oppsummeringstekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String oppsummeringstekst;

    @Size(max = 50, message = "For mange perioder")
    @Valid
    private List<PeriodeMedTekstDto> perioderMedTekst;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getOppsummeringstekst() {
        return oppsummeringstekst;
    }

    public void setOppsummeringstekst(String oppsummeringstekst) {
        this.oppsummeringstekst = oppsummeringstekst;
    }

    public List<PeriodeMedTekstDto> getPerioderMedTekst() {
        return perioderMedTekst;
    }

    public void setPerioderMedTekst(List<PeriodeMedTekstDto> perioderMedTekst) {
        this.perioderMedTekst = perioderMedTekst;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
    }
}
