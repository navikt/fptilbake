package no.nav.foreldrepenger.tilbakekreving.los.klient.k9.kontrakt;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;



@JsonTypeInfo(use = NAME, include = PROPERTY, property = "fagsystem", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TilbakebetalingBehandlingProsessEventDto.class, name = "K9TILBAKE"),
        @JsonSubTypes.Type(value = BehandlingProsessEventDto.class, name = "K9")
})
public abstract class BehandlingProsessEventMixin {
    public BehandlingProsessEventMixin() {
    }
}


