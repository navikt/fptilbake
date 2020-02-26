package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@Entity(name = "KlasseKode")
@DiscriminatorValue(KlasseKode.DISCRIMINATOR)
public class KlasseKode extends Kodeliste {

    public static final String DISCRIMINATOR = "KLASSE_KODE";

    public static final KlasseKode FPATAL = new KlasseKode("FPATAL");
    public static final KlasseKode FPATFER = new KlasseKode("FPATFER");
    public static final KlasseKode FPATFRI = new KlasseKode("FPATFRI");
    public static final KlasseKode FPATORD = new KlasseKode("FPATORD");
    public static final KlasseKode FPREFAGFER_IOP = new KlasseKode("FPREFAGFER-IOP");
    public static final KlasseKode FPREFAG_IOP = new KlasseKode("FPREFAG-IOP");
    public static final KlasseKode FPSNDDM_OP = new KlasseKode("FPSNDDM-OP");
    public static final KlasseKode FPSNDFI = new KlasseKode("FPSNDFI");
    public static final KlasseKode FPSNDJB_OP = new KlasseKode("FPSNDJB-OP");
    public static final KlasseKode FPSND_OP = new KlasseKode("FPSND-OP");
    public static final KlasseKode FSKTSKAT = new KlasseKode("FSKTSKAT");
    public static final KlasseKode KL_KODE_FEIL_KORTTID = new KlasseKode("KL_KODE_FEIL_KORTTID");
    public static final KlasseKode TBMOTOBS = new KlasseKode("TBMOTOBS");
    public static final KlasseKode SPSND100D1DAGPFI = new KlasseKode("SPSND100D1DAGPFI");
    public static final KlasseKode SPSND100D1DTRPFI = new KlasseKode("SPSND100D1DTRPFI");
    public static final KlasseKode FPADATORD = new KlasseKode("FPADATORD");
    public static final KlasseKode FPADATFRI = new KlasseKode("FPADATFRI");
    public static final KlasseKode FPADSND_OP = new KlasseKode("FPADSND-OP");
    public static final KlasseKode FPADATAL = new KlasseKode("FPADATAL");
    public static final KlasseKode FPADATSJO = new KlasseKode("FPADATSJO");
    public static final KlasseKode FPADSNDDM_OP = new KlasseKode("FPADSNDDM-OP");
    public static final KlasseKode FPADSNDJB_OP = new KlasseKode("FPADSNDJB-OP");
    public static final KlasseKode FPADSNDFI = new KlasseKode("FPADSNDFI");
    public static final KlasseKode FPATSJO = new KlasseKode("FPATSJO");

    //ES klassekoder
    public static final KlasseKode FPENAD_OP = new KlasseKode("FPENAD-OP");
    public static final KlasseKode FPENFOD_OP = new KlasseKode("FPENFOD-OP");
    public static final KlasseKode KL_KODE_FEIL_REFUTG = new KlasseKode("KL_KODE_FEIL_REFUTG");

    //SVP klassekoder
    public static final KlasseKode FPSVATORD = new KlasseKode("FPSVATORD");
    public static final KlasseKode FPSVATFRI = new KlasseKode("FPSVATFRI");
    public static final KlasseKode FPSVSND_OP = new KlasseKode("FPSVSND-OP");
    public static final KlasseKode FPSVATAL = new KlasseKode("FPSVATAL");
    public static final KlasseKode FPSVATSJO = new KlasseKode("FPSVATSJO");
    public static final KlasseKode FPSVSNDDM_OP = new KlasseKode("FPSVSNDDM-OP");
    public static final KlasseKode FPSVSNDJB_OP = new KlasseKode("FPSVSNDJB-OP");
    public static final KlasseKode FPSVSNDFI = new KlasseKode("FPSVSNDFI");


    public static final KlasseKode UDEFINERT = new KlasseKode("-");

    private static final Map<String, KlasseKode> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(FPATAL.getKode(), FPATAL);
        TILGJENGELIGE.put(FPATFER.getKode(), FPATFER);
        TILGJENGELIGE.put(FPATFRI.getKode(), FPATFRI);
        TILGJENGELIGE.put(FPATORD.getKode(), FPATORD);
        TILGJENGELIGE.put(FPENAD_OP.getKode(), FPENAD_OP);
        TILGJENGELIGE.put(FPENFOD_OP.getKode(), FPENFOD_OP);
        TILGJENGELIGE.put(KL_KODE_FEIL_REFUTG.getKode(), KL_KODE_FEIL_REFUTG);
        TILGJENGELIGE.put(FPREFAGFER_IOP.getKode(), FPREFAGFER_IOP);
        TILGJENGELIGE.put(FPREFAG_IOP.getKode(), FPREFAG_IOP);
        TILGJENGELIGE.put(FPSNDDM_OP.getKode(), FPSNDDM_OP);
        TILGJENGELIGE.put(FPSNDFI.getKode(), FPSNDFI);
        TILGJENGELIGE.put(FPSNDJB_OP.getKode(), FPSNDJB_OP);
        TILGJENGELIGE.put(FPSND_OP.getKode(), FPSND_OP);
        TILGJENGELIGE.put(FSKTSKAT.getKode(), FSKTSKAT);
        TILGJENGELIGE.put(KL_KODE_FEIL_KORTTID.getKode(), KL_KODE_FEIL_KORTTID);
        TILGJENGELIGE.put(TBMOTOBS.getKode(), TBMOTOBS);
        TILGJENGELIGE.put(SPSND100D1DAGPFI.getKode(), SPSND100D1DAGPFI);
        TILGJENGELIGE.put(SPSND100D1DTRPFI.getKode(), SPSND100D1DTRPFI);
        TILGJENGELIGE.put(FPADATORD.getKode(), FPADATORD);
        TILGJENGELIGE.put(FPADATFRI.getKode(), FPADATFRI);
        TILGJENGELIGE.put(FPADSND_OP.getKode(), FPADSND_OP);
        TILGJENGELIGE.put(FPADATAL.getKode(), FPADATAL);
        TILGJENGELIGE.put(FPADATSJO.getKode(), FPADATSJO);
        TILGJENGELIGE.put(FPADSNDDM_OP.getKode(), FPADSNDDM_OP);
        TILGJENGELIGE.put(FPADSNDJB_OP.getKode(), FPADSNDJB_OP);
        TILGJENGELIGE.put(FPADSNDFI.getKode(), FPADSNDFI);
        TILGJENGELIGE.put(FPATSJO.getKode(), FPATSJO);
        TILGJENGELIGE.put(FPSVATORD.getKode(), FPSVATORD);
        TILGJENGELIGE.put(FPSVATFRI.getKode(), FPSVATFRI);
        TILGJENGELIGE.put(FPSVSND_OP.getKode(), FPSVSND_OP);
        TILGJENGELIGE.put(FPSVATAL.getKode(), FPSVATAL);
        TILGJENGELIGE.put(FPSVATSJO.getKode(), FPSVATSJO);
        TILGJENGELIGE.put(FPSVSNDDM_OP.getKode(), FPSVSNDDM_OP);
        TILGJENGELIGE.put(FPSVSNDJB_OP.getKode(), FPSVSNDJB_OP);
        TILGJENGELIGE.put(FPSVSNDFI.getKode(), FPSVSNDFI);
    }

    KlasseKode() {
        // Hibernate
    }

    public KlasseKode(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static KlasseKode fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        throw KlasseKodeFeil.FEILFACTORY.ugyldigKlasseKode(kode).toException();
    }

    interface KlasseKodeFeil extends DeklarerteFeil {

        KlasseKodeFeil FEILFACTORY = FeilFactory.create(KlasseKodeFeil.class);

        @TekniskFeil(feilkode = "FPT-312904", feilmelding = "KlasseKode '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigKlasseKode(String klasseKode);
    }

}
