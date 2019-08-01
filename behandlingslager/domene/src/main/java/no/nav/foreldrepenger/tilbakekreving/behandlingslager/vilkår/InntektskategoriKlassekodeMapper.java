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
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATFRI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATORD;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPATSJO;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPENAD_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPENFOD_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSNDDM_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSNDFI;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSNDJB_OP;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode.FPSND_OP;
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

    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD;
    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL;
    private static final Map<KlasseKode, Inntektskategori> KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON;

    private InntektskategoriKlassekodeMapper() {
    }

    static {
        KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD = Map.of(
                FPENFOD_OP, FØDSEL_ES,
                FPENAD_OP, ADOPSJON_ES
        );
    }

    static {
        KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL = Map.of(
                FPADATORD, ARBEIDSTAKER,
                FPATFRI, FRILANSER,
                FPADSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
                FPADATAL, ARBEIDSLEDIG,
                FPADATSJO, SJØMANN,
                FPADSNDDM_OP, DAGMAMMA,
                FPADSNDJB_OP, JORDBRUKER,
                FPADSNDFI, FISKER,
                FPATFER, FERIEPENGER_ARBEIDSTAKER
        );
    }

    static {
        KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON = Map.of(
                FPATORD, ARBEIDSTAKER,
                FPATFRI, FRILANSER,
                FPSND_OP, SELVSTENDIG_NÆRINGSDRIVENDE,
                FPATAL, ARBEIDSLEDIG,
                FPATSJO, SJØMANN,
                FPSNDDM_OP, DAGMAMMA,
                FPSNDJB_OP, JORDBRUKER,
                FPSNDFI, FISKER,
                FPATFER, FERIEPENGER_ARBEIDSTAKER
        );
    }

    public static Inntektskategori finnInntekstkategoriMedKlasseKode(KlasseKode klasseKode) {
        if (KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_ENGANGSSTØNAD.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_FØDSEL.get(klasseKode);
        } else if (KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON.containsKey(klasseKode)) {
            return KLASSEKODE_INNTEKTSKATEGORI_MAP_ADOPSJON.get(klasseKode);
        } else {
            throw new IllegalStateException("Utvikler feil: Mangler mapping for klasseKode=" + klasseKode);
        }
    }
}
