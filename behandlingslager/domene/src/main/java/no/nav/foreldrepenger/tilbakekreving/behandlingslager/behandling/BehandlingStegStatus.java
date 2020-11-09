package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;


/**
 * Kodefor status i intern håndtering av flyt på et steg
 * <p>
 * Kommer kun til anvendelse dersom det oppstår aksjonspunkter eller noe må legges på vent i et steg. Hvis ikke
 * flyter et rett igjennom til UTFØRT.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BehandlingStegStatus implements Kodeverdi {

    INNGANG("INNGANG"),
    /**
     * midlertidig intern tilstand når steget startes (etter inngang).
     */
    STARTET("STARTET"),
    VENTER("VENTER"),
    UTGANG("UTGANG"),
    AVBRUTT("AVBRUTT"),
    UTFØRT("UTFØRT"),
    FREMOVERFØRT("FREMOVERFØRT"),
    TILBAKEFØRT("TILBAKEFØRT"),
    /**
     * Kun for intern bruk.
     */
    UDEFINERT("-");

    public static final String KODEVERK = "BEHANDLING_STEG_STATUS";
    private static final Map<String, BehandlingStegStatus> KODER = new LinkedHashMap<>();

    private static final Set<BehandlingStegStatus> KAN_UTFØRE_STEG = new HashSet<>(Arrays.asList(STARTET, VENTER));
    private static final Set<BehandlingStegStatus> KAN_FORTSETTE_NESTE = new HashSet<>(Arrays.asList(UTFØRT, FREMOVERFØRT));
    private static final Set<BehandlingStegStatus> SLUTT_STATUSER = new HashSet<>(Arrays.asList(AVBRUTT, UTFØRT, TILBAKEFØRT));

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    BehandlingStegStatus(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BehandlingStegStatus fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BehandlingStegStatus.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BehandlingStegStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, BehandlingStegStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public boolean kanUtføreSteg() {
        return KAN_UTFØRE_STEG.contains(this);
    }

    public boolean kanFortsetteTilNeste() {
        return KAN_FORTSETTE_NESTE.contains(this);
    }

    public static boolean erSluttStatus(BehandlingStegStatus status) {
        return SLUTT_STATUSER.contains(status);
    }

    public boolean erVedInngang() {
        return Objects.equals(INNGANG, this);
    }

    public static boolean erVedUtgang(BehandlingStegStatus stegStatus) {
        return Objects.equals(UTGANG, stegStatus);
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

    @Override
    public String getNavn() {
        return null;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<BehandlingStegStatus, String> {
        @Override
        public String convertToDatabaseColumn(BehandlingStegStatus attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BehandlingStegStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
