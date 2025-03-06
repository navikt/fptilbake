package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

/**
 * Dummy enum som implementerer Kodeverdi.
 * <p>
 * For å teste serialisering+deserialisering av @JsonValue annotert Kodeverdi enum med KodelisteDeserializerModifier, og overstyring av serialisering til kode string og som json objekt.
 * <p>
 * Viser vegen til å skrive om gamle Kodeverdi enums og samtidig behalde kompatibilitet med frontend som bruker gammal serialisering.
 */
public enum TestKodeverdiEnum implements Kodeverdi {
    VALUE_ONE("ONE"),
    VALUE_TWO("TWO");

    private String kode;

    private TestKodeverdiEnum(final String kode) {
        this.kode = kode;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this.kode;
    }

    @Override
    public String getKodeverk() {
        return "TestKodeverdiEnum";
    }

    @Override
    public String getNavn() {
        return "TestKodeverdi: "+this.getKode();
    }
}
