package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.ADOPSJON_ES;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.ARBEIDSLEDIG;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.ARBEIDSTAKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.DAGMAMMA;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FERIEPENGER_ARBEIDSTAKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FERIETILLEGG;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FISKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FRILANSER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FØDSEL_ES;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.JORDBRUKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.SJØMANN;

import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori;


public class InntektskategoriKlassekodeMapper {

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD = Map.of(
        KlasseKode.FPENFOD_OP, FØDSEL_ES,
        KlasseKode.FPENAD_OP, ADOPSJON_ES
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL = Map.of(
        KlasseKode.FPADATORD, ARBEIDSTAKER,
        KlasseKode.FPATFRI, FRILANSER,
        KlasseKode.FPADSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
        KlasseKode.FPADATAL, ARBEIDSLEDIG,
        KlasseKode.FPADATSJO, SJØMANN,
        KlasseKode.FPADSNDDM_OP, DAGMAMMA,
        KlasseKode.FPADSNDJB_OP, JORDBRUKER,
        KlasseKode.FPADSNDFI, FISKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON = Map.of(
        KlasseKode.FPATORD, ARBEIDSTAKER,
        KlasseKode.FPATFRI, FRILANSER,
        KlasseKode.FPSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
        KlasseKode.FPATAL, ARBEIDSLEDIG,
        KlasseKode.FPATSJO, SJØMANN,
        KlasseKode.FPSNDDM_OP, DAGMAMMA,
        KlasseKode.FPSNDJB_OP, JORDBRUKER,
        KlasseKode.FPSNDFI, FISKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_SVANGERSKAPPENGER = Map.of(
        KlasseKode.FPSVATORD, ARBEIDSTAKER,
        KlasseKode.FPSVATFRI, FRILANSER,
        KlasseKode.FPSVSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
        KlasseKode.FPSVATAL, ARBEIDSLEDIG,
        KlasseKode.FPSVATSJO, SJØMANN,
        KlasseKode.FPSVSNDDM_OP, DAGMAMMA,
        KlasseKode.FPSVSNDJB_OP, JORDBRUKER,
        KlasseKode.FPSVSNDFI, FISKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_FERIE_FORELDREPENGER = Map.of(
        KlasseKode.FPATFER, FERIEPENGER_ARBEIDSTAKER,
        KlasseKode.FPADATFER, FERIEPENGER_ARBEIDSTAKER,
        KlasseKode.FPSVATFER, FERIEPENGER_ARBEIDSTAKER,
        KlasseKode.FPATFER_SSKT, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_OMSORGSPENGER = Map.of(
        KlasseKode.OMATORD, ARBEIDSTAKER,
        KlasseKode.OMATFRI, FRILANSER,
        KlasseKode.OMSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
        KlasseKode.OMATAL, ARBEIDSLEDIG,
        KlasseKode.OMATSJO, SJØMANN,
        KlasseKode.OMSNDDM_OP, DAGMAMMA,
        KlasseKode.OMSNDJB_OP, JORDBRUKER,
        KlasseKode.OMSNDFI, FISKER,
        KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER,
        KlasseKode.OMATFER, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_SYKT_BARN = new LinkedHashMap<>() {{
        put(KlasseKode.PNBSATORD, ARBEIDSTAKER);
        put(KlasseKode.PNBSATFRI, FRILANSER);
        put(KlasseKode.PNBSSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE);
        put(KlasseKode.PNBSATAL, ARBEIDSLEDIG);
        put(KlasseKode.PNBSATSJO, SJØMANN);
        put(KlasseKode.PNBSSNDDM_OP, DAGMAMMA);
        put(KlasseKode.PNBSSNDJB_OP, JORDBRUKER);
        put(KlasseKode.PNBSSNDFI, FISKER);
        put(KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER);
        put(KlasseKode.PPATFER, FERIEPENGER_ARBEIDSTAKER);
        put(KlasseKode.PPALFERTILL, FERIETILLEGG);
    }};

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_I_LIVETS_SLUTTFASE = new LinkedHashMap<>() {{
        put(KlasseKode.PPNPATORD, ARBEIDSTAKER);
        put(KlasseKode.PPNPATFRI, FRILANSER);
        put(KlasseKode.PPNPSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE);
        put(KlasseKode.PPNPATAL, ARBEIDSLEDIG);
        put(KlasseKode.PPNPATSJO, SJØMANN);
        put(KlasseKode.PPNPSNDDM_OP, DAGMAMMA);
        put(KlasseKode.PPNPSNDJB_OP, JORDBRUKER);
        put(KlasseKode.PPNPSNDFI, FISKER);
        put(KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER);
        put(KlasseKode.PPATFER, FERIEPENGER_ARBEIDSTAKER);
        put(KlasseKode.PPALFERTILL, FERIETILLEGG);
    }};

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_OPPLÆRINGSPENGER = new LinkedHashMap<>() {{
        put(KlasseKode.OPPATORD, ARBEIDSTAKER);
        put(KlasseKode.OPPATFRI, FRILANSER);
        put(KlasseKode.OPPSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE);
        put(KlasseKode.OPPATAL, ARBEIDSLEDIG);
        put(KlasseKode.OPPATSJO, SJØMANN);
        put(KlasseKode.OPPSNDDM_OP, DAGMAMMA);
        put(KlasseKode.OPPSNDJB_OP, JORDBRUKER);
        put(KlasseKode.OPPSNDFI, FISKER);
        put(KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER);
        put(KlasseKode.OPPATFER, FERIEPENGER_ARBEIDSTAKER);
        put(KlasseKode.OPALFERTILL, FERIETILLEGG);
    }};

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_FRISINN = Map.of(
        KlasseKode.FRISINN_FRILANS, FRILANSER,
        KlasseKode.FRISINN_SELVST_OP, SELVSTENDIG_NÆRINGSDRIVENDE
    );

    private InntektskategoriKlassekodeMapper() {
    }


    public static Inntektskategori finnInntekstkategoriMedKlasseKode(KlasseKode klasseKode) {
        if (KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_SVANGERSKAPPENGER.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_SVANGERSKAPPENGER.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_FERIE_FORELDREPENGER.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_FERIE_FORELDREPENGER.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_OMSORGSPENGER.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_OMSORGSPENGER.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_SYKT_BARN.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_SYKT_BARN.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_I_LIVETS_SLUTTFASE.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_I_LIVETS_SLUTTFASE.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_OPPLÆRINGSPENGER.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_OPPLÆRINGSPENGER.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_FRISINN.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_FRISINN.get(klasseKode);
        } else {
            throw new IllegalStateException("Utvikler feil: Mangler mapping for klasseKode=" + klasseKode);
        }
    }
}
