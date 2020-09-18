package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(ForeslåVedtakDto.AKSJONSPUNKT_KODE)
public class ForeslåVedtakDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5004";

    @Valid
    @Size(max = 100)
    private List<PeriodeMedTekstDto> perioderMedTekst;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String oppsummeringstekst;

    public ForeslåVedtakDto() {
        super();
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
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
