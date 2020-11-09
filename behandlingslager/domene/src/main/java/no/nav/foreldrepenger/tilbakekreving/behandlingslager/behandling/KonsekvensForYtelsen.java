package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
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

    private String kode;

    @JsonIgnore
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    KonsekvensForYtelsen(String kode, String navn){
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static KonsekvensForYtelsen fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(KonsekvensForYtelsen.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent YtelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, KonsekvensForYtelsen> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return navn;
    }
}
