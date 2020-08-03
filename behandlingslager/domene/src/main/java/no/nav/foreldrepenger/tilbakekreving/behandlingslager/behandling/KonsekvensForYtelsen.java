package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum KonsekvensForYtelsen implements Kodeverdi {

    FORELDREPENGER_OPPHØRER("FORELDREPENGER_OPPHØRER","Foreldrepenger opphører"),
    ENDRING_I_BEREGNING("ENDRING_I_BEREGNING","Endring i beregning"),
    ENDRING_I_UTTAK("ENDRING_I_UTTAK","Endring i uttak"),
    ENDRING_I_BEREGNING_OG_UTTAK("ENDRING_I_BEREGNING_OG_UTTAK","Endring i beregning og uttak"),
    ENDRING_I_FORDELING_AV_YTELSEN("ENDRING_I_FORDELING_AV_YTELSEN","Endring i fordeling av ytelsen"),
    INGEN_ENDRING("INGEN_ENDRING","Ingen endring"),
    UDEFINERT("-","Udefinert"),
    //k9
    YTELSE_OPPHØRER("YTELSE_OPPHØRER", "Ytelsen opphører");

    public static final String KODEVERK = "KONSEKVENS_FOR_YTELSEN";

    private static final Map<String, KonsekvensForYtelsen> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

    @JsonIgnore
    private String navn;

    private KonsekvensForYtelsen(String kode){ //NOSONAR
        this.kode = kode;
    }

    private KonsekvensForYtelsen(String kode, String navn){
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static KonsekvensForYtelsen fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent YtelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, KonsekvensForYtelsen> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public java.lang.String getKode() {
        return kode;
    }

    @Override
    public java.lang.String getOffisiellKode() {
        return getKode();
    }

    @Override
    public java.lang.String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public java.lang.String getNavn() {
        return navn;
    }
}
