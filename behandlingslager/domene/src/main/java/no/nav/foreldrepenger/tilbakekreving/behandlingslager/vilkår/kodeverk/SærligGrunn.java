package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.vedtak.util.InputValideringRegex;

public enum SærligGrunn implements Kodeverdi {

    GRAD_AV_UAKTSOMHET("GRAD_UAKTSOMHET", "Graden av uaktsomhet hos den kravet retter seg mot"),
    HELT_ELLER_DELVIS_NAVS_FEIL("HELT_ELLER_DELVIS_NAVS_FEIL", "Om feilen helt eller delvis kan tilskrives Nav"),
    STØRRELSE_BELØP("STOERRELSE_BELOEP", "Størrelsen på feilutbetalt beløp"),
    TID_FRA_UTBETALING("TID_FRA_UTBETALING", "Hvor lang tid siden utbetalingen fant sted"),
    ANNET("ANNET", "Annet");

    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String kode;
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String navn;

    public static final String KODEVERK = "SAERLIG_GRUNN";
    private static final Map<String, SærligGrunn> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private SærligGrunn(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static SærligGrunn fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent SærligGrunn: " + kode);
        }
        return ad;
    }

    public static Map<String, SærligGrunn> kodeMap() {
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

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<SærligGrunn, String> {
        @Override
        public String convertToDatabaseColumn(SærligGrunn attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public SærligGrunn convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
