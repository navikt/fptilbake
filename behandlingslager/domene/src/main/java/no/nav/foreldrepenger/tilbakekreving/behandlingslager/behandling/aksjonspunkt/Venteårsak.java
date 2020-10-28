package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum Venteårsak implements Kodeverdi {


    VENT_PÅ_BRUKERTILBAKEMELDING("VENT_PÅ_BRUKERTILBAKEMELDING","Venter på tilbakemelding fra bruker"),
    VENT_PÅ_TILBAKEKREVINGSGRUNNLAG("VENT_PÅ_TILBAKEKREVINGSGRUNNLAG","Venter på tilbakekrevingsgrunnlag fra økonomi"),
    AVVENTER_DOKUMENTASJON("AVV_DOK","Avventer dokumentasjon"),
    UTVIDET_TILSVAR_FRIST("UTV_TIL_FRIST","Utvidet tilsvarsfrist"),
    ENDRE_TILKJENT_YTELSE("ENDRE_TILKJENT_YTELSE","Mulig endring i tilkjent ytelse"),
    VENT_PÅ_MULIG_MOTREGNING("VENT_PÅ_MULIG_MOTREGNING","Mulig motregning med annen ytelse"),
    UDEFINERT("-","");

    public static final String KODEVERK = "VENT_AARSAK";

    private static final Map<String, Venteårsak> KODER = new LinkedHashMap<>();

    private String kode;

    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    Venteårsak(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static Venteårsak fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Venteårsak: " + kode);
        }
        return ad;
    }

    public static Map<String, Venteårsak> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }


    public static boolean venterPåBruker(Venteårsak venteårsak) {
        return VENT_PÅ_BRUKERTILBAKEMELDING.equals(venteårsak) || UTVIDET_TILSVAR_FRIST.equals(venteårsak) || AVVENTER_DOKUMENTASJON.equals(venteårsak);
    }

    public static boolean venterPåØkonomi(Venteårsak venteårsak) {
        return VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(venteårsak) || VENT_PÅ_MULIG_MOTREGNING.equals(venteårsak);
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

    @JsonProperty
    @Override
    public String getNavn() {
        return navn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<Venteårsak, String> {
        @Override
        public String convertToDatabaseColumn(Venteårsak attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public Venteårsak convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
