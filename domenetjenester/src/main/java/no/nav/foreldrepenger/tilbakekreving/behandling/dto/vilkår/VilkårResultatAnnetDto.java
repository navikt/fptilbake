package no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;

@JsonTypeName(VilkårResultatAnnetDto.TYPE)
public class VilkårResultatAnnetDto extends VilkårResultatInfoDto {

    static final String TYPE = "annet";

    @NotNull
    @Valid
    private Aktsomhet aktsomhet;

    @Valid
    private VilkårResultatAktsomhetDto aktsomhetInfo;

    protected VilkårResultatAnnetDto() {
        // for jackson
    }

    public VilkårResultatAnnetDto(String begrunnelse, Aktsomhet aktsomhet, VilkårResultatAktsomhetDto aktsomhetInfo) {
        super(begrunnelse);
        this.aktsomhet = aktsomhet;
        this.aktsomhetInfo = aktsomhetInfo;
    }

    public Aktsomhet getAktsomhet() {
        return aktsomhet;
    }

    public VilkårResultatAktsomhetDto getAktsomhetInfo() {
        return aktsomhetInfo;
    }

    @AssertTrue(message = "aktsomhetInfo kan bare være null når aktsomhet er FORSETT")
    private boolean isAktsomhetInfo() {
        return this.aktsomhetInfo != null || Aktsomhet.FORSETT.equals(this.aktsomhet);
    }
}
