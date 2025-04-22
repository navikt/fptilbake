package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum SivilstandType implements Kodeverdi {
    ETTERLATT("ENKE"),
    GIFT("GIFT"),
    GJENLEVENDE_PARTNER("GJPA"),
    GIFT_ADSKILT("GLAD"),
    UOPPGITT("NULL"),
    REGISTRERT_PARTNER("REPA"),
    SAMBOER("SAMB"),
    SEPARERT_PARTNER("SEPA"),
    SEPARERT("SEPR"),
    SKILT("SKIL"),
    SKILT_PARTNER("SKPA"),
    UGIFT("UGIF");

    public static final String KODEVERK = "SIVILSTAND_TYPE";
    private static final Map<String, SivilstandType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    SivilstandType(String kode) {
        this.kode = kode;
    }

    public static SivilstandType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent SivilstandType: " + kode);
        }
        return ad;
    }

    public static Map<String, SivilstandType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public boolean erGift() {
        return GIFT.equals(this) || GIFT_ADSKILT.equals(this) || SEPARERT.equals(this);
    }

    public boolean erPartner() {
        return REGISTRERT_PARTNER.equals(this) || SEPARERT_PARTNER.equals(this) || GJENLEVENDE_PARTNER.equals(this);
    }

    public boolean erEtterlatt() {
        return ETTERLATT.equals(this);
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
        return null;
    }
}
