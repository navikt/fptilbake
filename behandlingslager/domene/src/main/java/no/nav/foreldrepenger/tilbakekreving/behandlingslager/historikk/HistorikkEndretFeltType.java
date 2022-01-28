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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum HistorikkEndretFeltType implements Kodeverdi {

    BEHANDLENDE_ENHET("BEHANDLENDE_ENHET", ""),
    BEHANDLING("BEHANDLING", ""),
    ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT("ER_VILKARENE_TILBAKEKREVING_OPPFYLT", "Er vilkårene for tilbakekreving oppfylt?"),
    ER_SÆRLIGE_GRUNNER_TIL_REDUKSJON("ER_SARLIGE_GRUNNER_TIL_REDUKSJON", "Er det særlige grunner til reduksjon?"),
    HENDELSE_ÅRSAK("HENDELSE_AARSAK", "Hendelse"),
    HENDELSE_UNDER_ÅRSAK("HENDELSE_UNDER_AARSAK", "Hendelse Under Årsak"),
    MOTTAKER_UAKTSOMHET_GRAD("MOTTAKER_UAKTSOMHET_GRAD", "I hvilken grad har mottaker handlet uaktsomhet?"),
    ANDEL_TILBAKEKREVES("ANDEL_TILBAKEKREVES", "Andel som tilbakekreves"),
    BELØP_TILBAKEKREVES("BELOEP_TILBAKEKREVES", "Beløp som skal tilbakekreves"),
    TILBAKEKREV_SMÅBELOEP("TILBAKEKREV_SMAABELOEP", "Skal beløp under 4 rettsgebyr(6.ledd) tilbakekreves?"),
    ER_BELØPET_BEHOLD("BELOEP_ER_I_BEHOLD", "Er beløpet i behold?"),
    ILEGG_RENTER("ILEGG_RENTER", "Skal det tilegges renter?"),
    FORELDELSE("FORELDELSE", "Foreldelse"),
    FORELDELSESFRIST("FORELDELSESFRIST", "Foreldelsesfrist"),
    OPPDAGELSES_DATO("OPPDAGELSES_DATO", "Dato for når feilutbetaling ble oppdaget");

    private String kode;
    private String navn;

    public static final String KODEVERK = "HISTORIKK_ENDRET_FELT_TYPE"; //$NON-NLS-1$
    private static final Map<String, HistorikkEndretFeltType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private HistorikkEndretFeltType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static HistorikkEndretFeltType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(HistorikkEndretFeltType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkEndretFeltType: " + kode);
        }
        return ad;
    }

    public static Map<String, HistorikkEndretFeltType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
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
