package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum HistorikkOpplysningType implements Kodeverdi {

    FODSELSDATO("FODSELSDATO"),
    PERIODE_FOM("PERIODE_FOM"),
    PERIODE_TOM("PERIODE_TOM"),
    TILBAKEKREVING_OPPFYLT_BEGRUNNELSE("TILBAKEKREVING_OPPFYLT_BEGRUNNELSE"),
    SÆRLIG_GRUNNER_BEGRUNNELSE("SÆRLIG_GRUNNER_BEGRUNNELSE"),
    KRAVGRUNNLAG_VEDTAK_ID("KRAVGRUNNLAG_VEDTAK_ID","ID"),
    KRAVGRUNNLAG_STATUS("KRAVGRUNNLAG_STATUS","Status"),
    UDEFINIERT("-");

    private String kode;
    private String navn;

    public static final String KODEVERK = "HISTORIKK_OPPLYSNING_TYPE"; //$NON-NLS-1$
    private static Map<String, HistorikkOpplysningType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private HistorikkOpplysningType(String kode) {
        this.kode = kode;
    }

    private HistorikkOpplysningType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static HistorikkOpplysningType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkOpplysningType: " + kode);
        }
        return ad;
    }

    public static Map<String, HistorikkOpplysningType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
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
}
