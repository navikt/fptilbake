package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(AksjonspunktKodeDefinisjon.FORESLÅ_VEDTAK)
public class ForeslåVedtakDto extends BekreftetAksjonspunktDto {

    @Valid
    @Size(max = 100)
    private List<PeriodeMedTekstDto> perioderMedTekst;

    @Size(max = 10000, message = "Oppsummeringstekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String oppsummeringstekst;

    public ForeslåVedtakDto() {
        super();
    }

    public List<PeriodeMedTekstDto> getPerioderMedTekst() {
        return perioderMedTekst;
    }

    public void setPerioderMedTekst(List<PeriodeMedTekstDto> perioderMedTekst) {
        this.perioderMedTekst = perioderMedTekst;
    }

    public String getOppsummeringstekst() {
        return oppsummeringstekst;
    }

    public void setOppsummeringstekst(String oppsummeringstekst) {
        this.oppsummeringstekst = oppsummeringstekst;
    }
}
