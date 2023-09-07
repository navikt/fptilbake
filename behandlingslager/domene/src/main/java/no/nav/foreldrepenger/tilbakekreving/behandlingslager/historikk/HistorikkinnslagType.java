package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_1;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_10;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_2;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_3;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_4;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_5;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_6;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_7;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagMal.MAL_TYPE_8;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.Transient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum HistorikkinnslagType implements Kodeverdi {

    // type 1
    BEH_GJEN("BEH_GJEN", "Behandling gjenopptatt", MAL_TYPE_1),
    BEH_MAN_GJEN("BEH_MAN_GJEN", "Gjenoppta behandling", MAL_TYPE_1),
    BEH_STARTET("BEH_STARTET", "Behandling startet", MAL_TYPE_1),
    BEH_STARTET_PÅ_NYTT("BEH_STARTET_PÅ_NYTT", "Behandling startet på nytt", MAL_TYPE_1),
    BEH_STARTET_FORFRA("BEH_STARTET_FORFRA", "Behandling startet forfra", MAL_TYPE_1),
    VEDLEGG_MOTTATT("VEDLEGG_MOTTATT", "Vedlegg mottatt", MAL_TYPE_1),
    BREV_SENT("BREV_SENT", "Brev sendt", MAL_TYPE_1),
    BREV_BESTILT("BREV_BESTILT", "Brev bestilt", MAL_TYPE_1),
    REVURD_OPPR("REVURD_OPPR", "Tilbakekreving Revurdering opprettet", MAL_TYPE_1),
    REGISTRER_PAPIRSØK("REGISTRER_PAPIRSØK", "Registrer papirsøknad", MAL_TYPE_1),
    MANGELFULL_SØKNAD("MANGELFULL_SØKNAD", "Mangelfull søknad", MAL_TYPE_1),
    INNSYN_OPPR("INNSYN_OPPR", "Innsynsbehandling opprettet", MAL_TYPE_1),
    NYE_REGOPPLYSNINGER("NYE_REGOPPLYSNINGER", "Nye registeropplysninger", MAL_TYPE_1),
    KLAGEBEH_STARTET("KLAGEBEH_STARTET", "Klage mottatt", MAL_TYPE_1),
    TBK_OPPR("TILBAKEKREVING_OPPR", "Tilbakekreving opprettet", MAL_TYPE_1),
    OPPGAVE_VEDTAK("OPPGAVE_VEDTAK", "Oppgave før vedtak", MAL_TYPE_1),

    // type 2
    FORSLAG_VEDTAK("FORSLAG_VEDTAK", "Vedtak foreslått og sendt til beslutter", MAL_TYPE_2),
    VEDTAK_FATTET("VEDTAK_FATTET", "Vedtak fattet", MAL_TYPE_2),
    VEDTAK_FATTET_AUTOMATISK("VEDTAK_FATTET_AUTOMATISK", "Vedtak automatisk fattet", MAL_TYPE_2),
    REGISTRER_OM_VERGE("REGISTRER_OM_VERGE", "Registering av opplysninger om verge/fullmektig", MAL_TYPE_2),

    // type 3
    SAK_RETUR("SAK_RETUR", "Sak retur", MAL_TYPE_3),

    // type 4
    AVBRUTT_BEH("AVBRUTT_BEH", "Behandling er henlagt", MAL_TYPE_4),
    BEH_VENT("BEH_VENT", "Behandling på vent", MAL_TYPE_4),
    FJERNET_VERGE("FJERNET_VERGE", "Opplysninger om verge/fullmektig fjernet", MAL_TYPE_4), //"Opplysninger om verge/fullmektig fjernet", HistorikkinnslagMal.MAL_TYPE_4)

    // type 5
    FAKTA_ENDRET("FAKTA_ENDRET", "Fakta endret", MAL_TYPE_5),
    BYTT_ENHET("BYTT_ENHET", "Bytt enhet", MAL_TYPE_5),
    KLAGE_BEH_NFP("KLAGE_BEH_NFP", "Klagebehandling NFP", MAL_TYPE_5),
    KLAGE_BEH_NK("KLAGE_BEH_NK", "Klagebehandling KA", MAL_TYPE_5),

    // type 6
    NY_KRAVGRUNNLAG_MOTTAT("NY_GRUNNLAG_MOTTATT", "Kravgrunnlag Mottatt", MAL_TYPE_6),

    // type 7
    OVERSTYRT("OVERSTYRT", "Overstyrt", MAL_TYPE_7),

    // type 8
    OPPTJENING("OPPTJENING", "Behandlet opptjeningsperiode", MAL_TYPE_8),

    // type 9

    // type 10
    FAKTA_OM_FEILUTBETALING("FAKTA_OM_FEILUTBETALING", "Fakta om feilutbetaling", MAL_TYPE_10),
    TILBAKEKREVING("TILBAKEKREVING", "Tilbakekreving", MAL_TYPE_10),
    FORELDELSE("FORELDELSE", "Foreldelse", MAL_TYPE_10),
    UDEFINIERT("-", "Ikke Definert", "");

    private String kode;

    private String navn;
    @Transient
    private String mal;

    public static final String KODEVERK = "HISTORIKKINNSLAG_TYPE"; //$NON-NLS-1$
    private static Map<String, HistorikkinnslagType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private HistorikkinnslagType(String kode, String navn, String mal) {
        this.kode = kode;
        this.navn = navn;
        this.mal = mal;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static HistorikkinnslagType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(HistorikkinnslagType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkinnslagType: " + kode);
        }
        return ad;
    }


    public static Map<String, HistorikkinnslagType> kodeMap() {
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

    @JsonProperty
    public String getMal() {
        return mal;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<HistorikkinnslagType, String> {
        @Override
        public String convertToDatabaseColumn(HistorikkinnslagType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public HistorikkinnslagType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
