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
public enum KlasseKode implements Kodeverdi {

    FPATAL("FPATAL","FPATAL"),
    FPATFER("FPATFER","FPATFER"),
    FPATFRI("FPATFRI","FPATFRI"),
    FPATORD("FPATORD","FPATORD"),
    FPREFAGFER_IOP("FPREFAGFER-IOP","FPREFAGFER-IOP"),
    FPREFAG_IOP("FPREFAG-IOP","FPREFAG-IOP"),
    FPSNDDM_OP("FPSNDDM-OP","FPSNDDM-OP"),
    FPSNDFI("FPSNDFI","FPSNDFI"),
    FPSNDJB_OP("FPSNDJB-OP","FPSNDJB-OP"),
    FPSND_OP("FPSND-OP","FPSND-OP"),
    FSKTSKAT("FSKTSKAT","FSKTSKAT"),
    KL_KODE_FEIL_KORTTID("KL_KODE_FEIL_KORTTID","KL_KODE_FEIL_KORTTID"),
    TBMOTOBS("TBMOTOBS","TBMOTOBS"),
    SPSND100D1DAGPFI("SPSND100D1DAGPFI","SPSND100D1DAGPFI"),
    SPSND100D1DTRPFI("SPSND100D1DTRPFI","SPSND100D1DTRPFI"),
    FPADATORD("FPADATORD","FPADATORD"),
    FPADATFRI("FPADATFRI","FPADATFRI"),
    FPADSND_OP("FPADSND-OP","FPADSND-OP"),
    FPADATAL("FPADATAL","FPADATAL"),
    FPADATSJO("FPADATSJO","FPADATSJO"),
    FPADSNDDM_OP("FPADSNDDM-OP","FPADSNDDM-OP"),
    FPADSNDJB_OP("FPADSNDJB-OP","FPADSNDJB-OP"),
    FPADSNDFI("FPADSNDFI","FPADSNDFI"),
    FPATSJO("FPATSJO","FPATSJO"),

    //ES Klassekoder
    FPENAD_OP("FPENAD-OP","FPENAD-OP"),
    FPENFOD_OP("FPENFOD-OP","FPENFOD-OP"),
    KL_KODE_FEIL_REFUTG("KL_KODE_FEIL_REFUTG"),

    //SVP Klassekoder
    FPSVATORD("FPSVATORD","FPSVATORD"),
    FPSVATFRI("FPSVATFRI","FPSVATFRI"),
    FPSVSND_OP("FPSVSND-OP","FPSVSND-OP"),
    FPSVATAL("FPSVATAL","FPSVATAL"),
    FPSVATSJO("FPSVATSJO","FPSVATSJO"),
    FPSVSNDDM_OP("FPSVSNDDM-OP","FPSVSNDDM-OP"),
    FPSVSNDJB_OP("FPSVSNDJB-OP","FPSVSNDJB-OP"),
    FPSVSNDFI("FPSVSNDFI","FPSVSNDFI"),

    //JUST klassekoder
    KL_KODE_JUST_KORTTID("KL_KODE_JUST_KORTTID","KL_KODE_JUST_KORTTID"),

    UDEFINERT("-");

    public static final String KODEVERK = "KLASSE_KODE";

    private static final Map<String, KlasseKode> KODER = new LinkedHashMap<>();

    @JsonValue
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

    private KlasseKode(String kode) {
        this.kode = kode;
    }

    private KlasseKode(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static KlasseKode fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent KlasseKode: " + kode);
        }
        return ad;
    }

    public static Map<String, KlasseKode> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return null;
    }
}
