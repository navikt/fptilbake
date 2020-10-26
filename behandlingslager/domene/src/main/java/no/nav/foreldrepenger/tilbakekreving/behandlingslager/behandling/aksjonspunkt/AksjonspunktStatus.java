package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum AksjonspunktStatus implements Kodeverdi {

    OPPRETTET("OPPR", "Opprettet"),
    UTFØRT("UTFO", "Utført"),
    AVBRUTT("AVBR", "Avbrutt");

    public static final String KODEVERK = "AKSJONSPUNKT_STATUS";
    private static final List<AksjonspunktStatus> ÅPNE_AKSJONSPUNKT_STATUSER = Arrays.asList(OPPRETTET);
    private static final List<AksjonspunktStatus> BEHANDLEDE_AKSJONSPUNKT_KODER = Arrays.asList(UTFØRT);
    private static final Map<String, AksjonspunktStatus> KODER = new LinkedHashMap<>();

    private String kode;
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    AksjonspunktStatus(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static AksjonspunktStatus fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AksjonspunktStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, AksjonspunktStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public boolean erÅpentAksjonspunkt() {
        return ÅPNE_AKSJONSPUNKT_STATUSER.contains(this);
    }

    public boolean erBehandletAksjonspunkt() {
        return BEHANDLEDE_AKSJONSPUNKT_KODER.contains(this);
    }

    public static List<AksjonspunktStatus> getÅpneAksjonspunktStatuser() {
        return new ArrayList<>(ÅPNE_AKSJONSPUNKT_STATUSER);
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
    public static class KodeverdiConverter implements AttributeConverter<AksjonspunktStatus, String> {
        @Override
        public String convertToDatabaseColumn(AksjonspunktStatus attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public AksjonspunktStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
