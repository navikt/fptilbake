package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum KlasseKode implements Kodeverdi {

    FPATAL("FPATAL"),
    FPATFER("FPATFER"),
    FPATFRI("FPATFRI"),
    FPATORD("FPATORD"),
    FPREFAGFER_IOP("FPREFAGFER-IOP"),
    FPREFAG_IOP("FPREFAG-IOP"),
    FPSNDDM_OP("FPSNDDM-OP"),
    FPSNDFI("FPSNDFI"),
    FPSNDJB_OP("FPSNDJB-OP"),
    FPSND_OP("FPSND-OP"),
    FSKTSKAT("FSKTSKAT"),
    KL_KODE_FEIL_KORTTID("KL_KODE_FEIL_KORTTID"),
    TBMOTOBS("TBMOTOBS"),
    SPSND100D1DAGPFI("SPSND100D1DAGPFI"),
    SPSND100D1DTRPFI("SPSND100D1DTRPFI"),
    FPADATORD("FPADATORD"),
    FPADATFRI("FPADATFRI"),
    FPADSND_OP("FPADSND-OP"),
    FPADATAL("FPADATAL"),
    FPADATSJO("FPADATSJO"),
    FPADSNDDM_OP("FPADSNDDM-OP"),
    FPADSNDJB_OP("FPADSNDJB-OP"),
    FPADSNDFI("FPADSNDFI"),
    FPATSJO("FPATSJO"),
    FPADREFAG_IOP("FPADREFAG-IOP"),
    FPADREFAGFER_IOP("FPADREFAGFER-IOP"),
    FPATFER_SSKT("FPATFER-SSKT"),

    //ES Klassekoder
    FPENAD_OP("FPENAD-OP"),
    FPENFOD_OP("FPENFOD-OP"),
    KL_KODE_FEIL_REFUTG("KL_KODE_FEIL_REFUTG"),

    //SVP Klassekoder
    FPSVATORD("FPSVATORD"),
    FPSVATFRI("FPSVATFRI"),
    FPSVSND_OP("FPSVSND-OP"),
    FPSVATAL("FPSVATAL"),
    FPSVATSJO("FPSVATSJO"),
    FPSVSNDDM_OP("FPSVSNDDM-OP"),
    FPSVSNDJB_OP("FPSVSNDJB-OP"),
    FPSVSNDFI("FPSVSNDFI"),
    FPSVREFAG_IOP("FPSVREFAG-IOP"),
    FPSVREFAGFER_IOP("FPSVREFAGFER-IOP"),

    //JUST klassekoder
    KL_KODE_JUST_KORTTID("KL_KODE_JUST_KORTTID"),
    KL_KODE_JUST_REFUTG("KL_KODE_JUST_REFUTG"),

    //FRISINN klassekoder
    FRISINN_FRILANS("FRISINN-FRILANS"),
    FRISINN_SELVST_OP("FRISINN-SELVST-OP"),
    KL_KODE_FEIL_KORONA("KL_KODE_FEIL_KORONA"),

    //K9 klassekoder
    OMATAL("OMATAL"),
    OMATFRI("OMATFRI"),
    OMATORD("OMATORD"),
    OMATSJO("OMATSJO"),
    OMREFAG_IOP("OMREFAG-IOP"),
    OMSND_OP("OMSND-OP"),
    OMSNDDM_OP("OMSNDDM-OP"),
    OMSNDFI("OMSNDFI"),
    OMSNDJB_OP("OMSNDJB-OP"),
    OPPATAL("OPPATAL"),
    OPPATFRI("OPPATFRI"),
    OPPATORD("OPPATORD"),
    OPPATSJO("OPPATSJO"),
    OPPREFAG_IOP("OPPREFAG-IOP"),
    OPPSND_OP("OPPSND-OP"),
    OPPSNDDM_OP("OPPSNDDM-OP"),
    OPPSNDFI("OPPSNDFI"),
    OPPSNDJB_OP("OPPSNDJB-OP"),
    PNBSATAL("PNBSATAL"),
    PNBSATFRI("PNBSATFRI"),
    PNBSATORD("PNBSATORD"),
    PNBSATSJO("PNBSATSJO"),
    PNBSREFAG_IOP("PNBSREFAG-IOP"),
    PNBSSND_OP("PNBSSND-OP"),
    PNBSSNDDM_OP("PNBSSNDDM-OP"),
    PNBSSNDFI("PNBSSNDFI"),
    PNBSSNDJB_OP("PNBSSNDJB-OP"),
    PPNPATAL("PPNPATAL"),
    PPNPATFRI("PPNPATFRI"),
    PPNPATORD("PPNPATORD"),
    PPNPATSJO("PPNPATSJO"),
    PPNPREFAG_IOP("PPNPREFAG-IOP"),
    PPNPSND_OP("PPNPSND-OP"),
    PPNPSNDDM_OP("PPNPSNDDM-OP"),
    PPNPSNDFI("PPNPSNDFI"),
    PPNPSNDJB_OP("PPNPSNDJB-OP"),
    SPATFER("SPATFER"),
    SPREFAGFERPP_IOP("SPREFAGFERPP-IOP"),

    UDEFINERT("-");

    public static final String KODEVERK = "KLASSE_KODE";

    private static final Map<String, KlasseKode> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static KlasseKode fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(KlasseKode.class, node, "kode");
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
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return null;
    }
}
