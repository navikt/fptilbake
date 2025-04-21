package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
/** Husk @JsonTypeName på alle sublasser!! */
public abstract class BekreftetAksjonspunktDto implements AksjonspunktKode, AbacDto {


    protected BekreftetAksjonspunktDto() {
        // For Jackson
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

    @Override
    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        if (this.getClass().isAnnotationPresent(JsonTypeName.class)) {
            var kode = this.getClass().getDeclaredAnnotation(JsonTypeName.class).value();
            return AksjonspunktDefinisjon.fraKode(kode);
        }
        throw new IllegalStateException("Utvikler-feil:" + this.getClass().getSimpleName() + " er uten JsonTypeName annotation.");
    }
}
