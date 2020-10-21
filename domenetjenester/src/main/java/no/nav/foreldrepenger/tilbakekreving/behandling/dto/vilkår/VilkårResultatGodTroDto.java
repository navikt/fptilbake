package no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(VilkårResultatGodTroDto.TYPE)
public class VilkårResultatGodTroDto extends VilkårResultatInfoDto {

    static final String TYPE = "godTro";

    private boolean erBelopetIBehold;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @DecimalMin("-999999999.00")
    @DecimalMax("999999999.99")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal tilbakekrevesBelop;

    protected VilkårResultatGodTroDto() {
        // for jackson
    }

    public VilkårResultatGodTroDto(String begrunnelse, boolean erBelopetIBehold, BigDecimal tilbakekrevesBelop) {
        super(begrunnelse);
        this.erBelopetIBehold = erBelopetIBehold;
        this.tilbakekrevesBelop = tilbakekrevesBelop;
    }

    public boolean getErBelopetIBehold() {
        return erBelopetIBehold;
    }

    public BigDecimal getTilbakekrevesBelop() {
        return tilbakekrevesBelop;
    }
}
