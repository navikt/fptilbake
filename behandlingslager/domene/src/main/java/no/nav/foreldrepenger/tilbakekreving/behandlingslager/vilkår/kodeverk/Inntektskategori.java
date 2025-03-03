package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum Inntektskategori implements Kodeverdi {

    FØDSEL_ES("FØDSEL", "Fødsel"), //TODO skal fjernes
    ADOPSJON_ES("ADOPSJON", "Adopsjon"), //TODO skal fjernes
    ARBEIDSTAKER("ARBEIDSTAKER", "Arbeidstaker"),
    FRILANSER("FRILANSER", "Frilanser"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE", "Selvstendig Næringsdrivende"),
    ARBEIDSLEDIG("ARBEIDSLEDIG", "Arbeidsledig"),
    SJØMANN("SJØMANN", "Sjømann"),
    DAGMAMMA("DAGMAMMA", "Dagmamma"),
    JORDBRUKER("JORDBRUKER", "Jordbruker"),
    FISKER("FISKER", "Fisker"),
    FERIEPENGER_ARBEIDSTAKER("FERIEPENGER_ARBEIDSTAKER", "Feriepenger arbeidstaker"),
    FERIETILLEGG("FERIETILLEGG", "Ferietillegg dagpenger"),

    UDEFINERT("-", "Ikke Definert");

    public static final String KODEVERK = "INNTEKTS_KATEGORI";
    private static final Map<String, Inntektskategori> KODER = new LinkedHashMap<>();

    private String kode;
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    Inntektskategori(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static Inntektskategori fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Inntektskategori: " + kode);
        }
        return ad;
    }

    public static Map<String, Inntektskategori> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonValue
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
        return navn;
    }
}
