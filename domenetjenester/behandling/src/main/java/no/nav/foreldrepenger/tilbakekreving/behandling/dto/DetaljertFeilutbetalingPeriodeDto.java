package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class DetaljertFeilutbetalingPeriodeDto extends FaktaFeilutbetalingDto {

    private BigDecimal feilutbetaling;

    private List<YtelseDto> ytelser = new ArrayList<>();

    private List<RedusertBeløpDto> redusertBeloper = new ArrayList<>();

    private VilkårResultat oppfyltValg = VilkårResultat.UDEFINERT;

    private boolean foreldet;

    DetaljertFeilutbetalingPeriodeDto() {
        // for CDI
    }

    public DetaljertFeilutbetalingPeriodeDto(LocalDate fom, LocalDate tom, HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto,
                                             BigDecimal feilutbetaling) {
        super(fom, tom, feilutbetalingÅrsakDto);
        this.feilutbetaling = feilutbetaling;
    }

    public DetaljertFeilutbetalingPeriodeDto(Periode periode, HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto,
                                             BigDecimal feilutbetaling) {
        super(periode.getFom(), periode.getTom(), feilutbetalingÅrsakDto);
        this.feilutbetaling = feilutbetaling;
    }

    public BigDecimal getFeilutbetaling() {
        return feilutbetaling;
    }

    public List<YtelseDto> getYtelser() {
        return ytelser;
    }

    public VilkårResultat getOppfyltValg() {
        return oppfyltValg;
    }

    public List<RedusertBeløpDto> getRedusertBeloper() {
        return redusertBeloper;
    }

    public void setYtelser(List<YtelseDto> ytelser) {
        this.ytelser = ytelser;
    }

    public boolean isForeldet() {
        return foreldet;
    }

    public void setForeldet(boolean foreldet) {
        this.foreldet = foreldet;
    }

    public void setRedusertBeloper(List<RedusertBeløpDto> redusertBeloper) {
        this.redusertBeloper = redusertBeloper;
    }

}
