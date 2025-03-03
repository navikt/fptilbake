package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;


public enum AksjonspunktStatus implements Kodeverdi {

    OPPRETTET("OPPR", "Opprettet"),
    UTFØRT("UTFO", "Utført"),
    AVBRUTT("AVBR", "Avbrutt");

    public static final String KODEVERK = "AKSJONSPUNKT_STATUS";
    private static final List<AksjonspunktStatus> ÅPNE_AKSJONSPUNKT_STATUSER = List.of(OPPRETTET);
    private static final List<AksjonspunktStatus> BEHANDLEDE_AKSJONSPUNKT_KODER = List.of(UTFØRT);
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

    public static AksjonspunktStatus fraKode(final String kode) {
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
