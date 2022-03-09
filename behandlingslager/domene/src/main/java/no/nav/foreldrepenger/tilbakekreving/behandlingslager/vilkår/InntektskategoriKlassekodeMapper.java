package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADATAL;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADATORD;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADATSJO;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADSNDDM_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADSNDFI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADSNDJB_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPADSND_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATAL;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATFER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATFER_SSKT;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATFRI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATORD;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATSJO;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPENAD_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPENFOD_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSNDDM_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSNDFI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSNDJB_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSND_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVATAL;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVATFRI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVATORD;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVATSJO;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVSNDDM_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVSNDFI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVSNDJB_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSVSND_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FRISINN_FRILANS;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FRISINN_SELVST_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.ADOPSJON_ES;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.ARBEIDSLEDIG;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.ARBEIDSTAKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.DAGMAMMA;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FERIEPENGER_ARBEIDSTAKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FISKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FRILANSER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.FØDSEL_ES;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.JORDBRUKER;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori.SJØMANN;

import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Inntektskategori;


public class InntektskategoriKlassekodeMapper {

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD = Map.of(
            FPENFOD_OP, FØDSEL_ES,
            FPENAD_OP, ADOPSJON_ES
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL = Map.of(
            FPADATORD, ARBEIDSTAKER,
            FPATFRI, FRILANSER,
            FPADSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
            FPADATAL, ARBEIDSLEDIG,
            FPADATSJO, SJØMANN,
            FPADSNDDM_OP, DAGMAMMA,
            FPADSNDJB_OP, JORDBRUKER,
            FPADSNDFI, FISKER,
            FPATFER, FERIEPENGER_ARBEIDSTAKER,
            FPATFER_SSKT, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON = Map.of(
            FPATORD, ARBEIDSTAKER,
            FPATFRI, FRILANSER,
            FPSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
            FPATAL, ARBEIDSLEDIG,
            FPATSJO, SJØMANN,
            FPSNDDM_OP, DAGMAMMA,
            FPSNDJB_OP, JORDBRUKER,
            FPSNDFI, FISKER,
            FPATFER, FERIEPENGER_ARBEIDSTAKER,
            FPATFER_SSKT, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_SVANGERSKAPPENGER = Map.of(
            FPSVATORD, ARBEIDSTAKER,
            FPSVATFRI, FRILANSER,
            FPSVSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
            FPSVATAL, ARBEIDSLEDIG,
            FPSVATSJO, SJØMANN,
            FPSVSNDDM_OP, DAGMAMMA,
            FPSVSNDJB_OP, JORDBRUKER,
            FPSVSNDFI, FISKER,
            FPATFER, FERIEPENGER_ARBEIDSTAKER,
            FPATFER_SSKT, FERIEPENGER_ARBEIDSTAKER
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
            KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_SYKT_BARN = Map.of(
            KlasseKode.PNBSATORD, ARBEIDSTAKER,
            KlasseKode.PNBSATFRI, FRILANSER,
            KlasseKode.PNBSSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
            KlasseKode.PNBSATAL, ARBEIDSLEDIG,
            KlasseKode.PNBSATSJO, SJØMANN,
            KlasseKode.PNBSSNDDM_OP, DAGMAMMA,
            KlasseKode.PNBSSNDJB_OP, JORDBRUKER,
            KlasseKode.PNBSSNDFI, FISKER,
            KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_I_LIVETS_SLUTTFASE = Map.of(
        KlasseKode.PPNPATORD, ARBEIDSTAKER,
        KlasseKode.PPNPATFRI, FRILANSER,
        KlasseKode.PPNPSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
        KlasseKode.PPNPATAL, ARBEIDSLEDIG,
        KlasseKode.PPNPATSJO, SJØMANN,
        KlasseKode.PPNPSNDDM_OP, DAGMAMMA,
        KlasseKode.PPNPSNDJB_OP, JORDBRUKER,
        KlasseKode.PPNPSNDFI, FISKER,
        KlasseKode.SPATFER, FERIEPENGER_ARBEIDSTAKER
    );

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_FRISINN = Map.of(
            FRISINN_FRILANS, FRILANSER,
            FRISINN_SELVST_OP, SELVSTENDIG_NÆRINGSDRIVENDE
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
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_OMSORGSPENGER.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_OMSORGSPENGER.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_SYKT_BARN.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_SYKT_BARN.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_I_LIVETS_SLUTTFASE.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_PLEIEPENGER_I_LIVETS_SLUTTFASE.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_FRISINN.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_FRISINN.get(klasseKode);
        } else {
            throw new IllegalStateException("Utvikler feil: Mangler mapping for klasseKode=" + klasseKode);
        }
    }
}
