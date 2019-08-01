package no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({
        @Type(value = VilkårResultatGodTroDto.class, name = "godTro"),
        @Type(value = VilkårResultatAnnetDto.class, name = "annet")
})
public abstract class VilkårResultatInfoDto {

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @NotNull
    private String begrunnelse;

    protected VilkårResultatInfoDto() {
        // for Jackson
    }

    protected VilkårResultatInfoDto(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

}
