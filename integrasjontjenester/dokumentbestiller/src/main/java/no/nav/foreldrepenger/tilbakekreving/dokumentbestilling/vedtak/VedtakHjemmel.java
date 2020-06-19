package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;

public class VedtakHjemmel {

    private static List<VilkårResultat> VILKÅRRESULTAT_MED_FORSETT_ALLTID_RENTER = Arrays.asList(
        VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
        VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER
    );

    private VedtakHjemmel() {
    }

    public static String lagHjemmelstekst(VedtakResultatType vedtakResultatType,
                                          VurdertForeldelse foreldelse,
                                          List<VilkårVurderingPeriodeEntitet> vilkårPerioder,
                                          EffektForBruker effektForBruker,
                                          Språkkode språkkode) {
        boolean foreldetVanlig = erNoeSattTilVanligForeldet(foreldelse);
        boolean foreldetMedTilleggsfrist = erTilleggsfristBenyttet(foreldelse);
        boolean ignorerteSmåbeløp = heleVurderingPgaSmåbeløp(vedtakResultatType, vilkårPerioder);
        boolean renter = erRenterBenyttet(vilkårPerioder);

        List<Hjemler> hjemler = new ArrayList<>();
        if (!vilkårPerioder.isEmpty()) {
            if (ignorerteSmåbeløp) {
                hjemler.add(Hjemler.FOLKETRYGD_22_15_SJETTE);
            } else if (renter) {
                hjemler.add(Hjemler.FOLKETRYGD_22_15_OG_22_17_A);
            } else {
                hjemler.add(Hjemler.FOLKETRYGD_22_15);
            }
        }

        if (foreldetMedTilleggsfrist) {
            hjemler.add(Hjemler.FORELDELSE_2_3_OG_10);
        } else if (foreldetVanlig) {
            hjemler.add(Hjemler.FORELDELSE_2_3);
        }
        if (EffektForBruker.ENDRET_TIL_GUNST_FOR_BRUKER.equals(effektForBruker)) {
            hjemler.add(Hjemler.FORVALTNING_35_A);
        }
        if (EffektForBruker.ENDRET_TIL_UGUNST_FOR_BRUKER.equals(effektForBruker)){
            hjemler.add(Hjemler.FORVALTNING_35_C);
        }

        return join(hjemler, " og ", språkkode);
    }

    private static boolean erRenterBenyttet(List<VilkårVurderingPeriodeEntitet> vilkårPerioder) {
        return vilkårPerioder.stream().anyMatch(v -> (v.getAktsomhet() != null && Boolean.TRUE.equals(v.getAktsomhet().getIleggRenter()))
            || erForsettOgAlltidRenter(v));
    }

    private static boolean erForsettOgAlltidRenter(VilkårVurderingPeriodeEntitet v) {
        return VILKÅRRESULTAT_MED_FORSETT_ALLTID_RENTER.contains(v.getVilkårResultat()) && Aktsomhet.FORSETT.equals(v.getAktsomhetResultat());
    }

    private static boolean heleVurderingPgaSmåbeløp(VedtakResultatType vedtakResultatType, List<VilkårVurderingPeriodeEntitet> vilkårPerioder) {
        return VedtakResultatType.INGEN_TILBAKEBETALING.equals(vedtakResultatType) && vilkårPerioder.stream().anyMatch(p -> Boolean.FALSE.equals(p.tilbakekrevesSmåbeløp()));
    }

    private static boolean erTilleggsfristBenyttet(VurdertForeldelse foreldelse) {
        return foreldelse != null && foreldelse.getVurdertForeldelsePerioder().stream().anyMatch(f -> f.getForeldelseVurderingType().equals(ForeldelseVurderingType.TILLEGGSFRIST));
    }

    private static boolean erNoeSattTilVanligForeldet(VurdertForeldelse foreldelse) {
        return foreldelse != null && foreldelse.getVurdertForeldelsePerioder().stream().anyMatch(f -> f.getForeldelseVurderingType().equals(ForeldelseVurderingType.FORELDET));
    }

    private static String join(List<Hjemler> elementer,
                               String sisteSkille,
                               Språkkode lokale) {
        StringBuilder builder = new StringBuilder();
        boolean første = true;
        for (int i = 0 ; i < elementer.size() ; i++) {
            Hjemler element = elementer.get(i);
            boolean siste = i == (elementer.size() - 1);
            if (!første) {
                builder.append(siste ? sisteSkille : ", ");
            }
            builder.append(element.hjemmelTekst(lokale));
            første = false;
        }
        return builder.toString();
    }

    enum EffektForBruker {
        FØRSTEGANGSVEDTAK,
        ENDRET_TIL_GUNST_FOR_BRUKER,
        ENDRET_TIL_UGUNST_FOR_BRUKER
    }

    private enum Hjemler {
        FOLKETRYGD_22_15("folketrygdloven § 22-15", "folketrygdlova § 22-15"),
        FOLKETRYGD_22_15_SJETTE("folketrygdloven § 22-15 sjette ledd", "folketrygdlova § 22-15 sjette ledd"),
        FOLKETRYGD_22_15_OG_22_17_A("folketrygdloven §§ 22-15 og 22-17 a", "folketrygdlova §§ 22-15 og 22-17 a"),
        FORELDELSE_2_3_OG_10("foreldelsesloven §§ 2, 3 og 10", "foreldingslova §§ 2, 3 og 10"),
        FORELDELSE_2_3("foreldelsesloven §§ 2 og 3", "foreldingslova §§ 2 og 3"),
        FORVALTNING_35_A("forvaltningsloven § 35 a)", "forvaltningslova § 35 a)"),
        FORVALTNING_35_C("forvaltningsloven § 35 c)", "forvaltningslova § 35 c)");

        private Map<Språkkode, String> hjemmelTekster;

        Hjemler(String bokmål, String nynorsk) {
            hjemmelTekster = new HashMap<>();
            hjemmelTekster.put(Språkkode.nb, bokmål);
            hjemmelTekster.put(Språkkode.nn, nynorsk);
        }

        String hjemmelTekst(Språkkode språkkode) {
            return hjemmelTekster.getOrDefault(språkkode, hjemmelTekster.get(Språkkode.nb));
        }
    }
}
