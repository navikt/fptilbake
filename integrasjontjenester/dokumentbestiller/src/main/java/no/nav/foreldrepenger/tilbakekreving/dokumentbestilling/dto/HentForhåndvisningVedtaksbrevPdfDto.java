package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndvisningVedtaksbrevPdfDto implements AbacDto {

    @Valid
    @NotNull
    private BehandlingReferanse behandlingId;

    @Size(max = 1500, message = "Oppsummeringstekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String oppsummeringstekst;

    @Size(max = 50, message = "For mange perioder")
    @NotNull
    @Valid
    private List<PeriodeMedTekstDto> perioderMedTekst;

    public BehandlingReferanse getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(BehandlingReferanse behandlingId) {
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
        return behandlingId.abacAttributter();
    }
}
