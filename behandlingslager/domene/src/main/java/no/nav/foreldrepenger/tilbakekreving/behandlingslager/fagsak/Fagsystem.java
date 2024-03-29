package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

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
public enum Fagsystem implements Kodeverdi {


    FPSAK("FPSAK", "FS36"),
    K9SAK("K9", "K9"),
    TPS("TPS", "FS03"),
    JOARK("JOARK", "AS36"),
    INFOTRYGD("INFOTRYGD", "IT01"),
    ARENA("ARENA", "AO01"),
    INNTEKT("INNTEKT", "FS28"),
    MEDL("MEDL", "FS18"),
    GOSYS("GOSYS", "FS22"),
    ENHETSREGISTERET("ENHETSREGISTERET", "ER01"),
    AAREGISTERET("AAREGISTERET", "AR01"),
    FPTILBAKE("FPTILBAKE", ""),
    K9TILBAKE("K9TILBAKE", "");

    public static final String KODEVERK = "FAGSYSTEM";

    private static final Map<String, Fagsystem> KODER = new LinkedHashMap<>();

    private String kode;

    @JsonIgnore
    private String offisiellKode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    Fagsystem(String kode, String offisiellKode) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Fagsystem fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(Fagsystem.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static Map<String, Fagsystem> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return null;
    }
}
