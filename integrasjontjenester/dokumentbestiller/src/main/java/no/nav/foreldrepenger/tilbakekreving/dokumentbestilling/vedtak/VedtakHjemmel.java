package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;

public class VedtakHjemmel {

    private VedtakHjemmel() {
    }

    public static String lagHjemmelstekst(VedtakResultatType vedtakResultatType, VurdertForeldelse foreldelse, List<VilkårVurderingPeriodeEntitet> vilkårPerioder, EffektForBruker effektForBruker) {
        boolean foreldetVanlig = erNoeSattTilVanligForeldet(foreldelse);
        boolean foreldetMedTilleggsfrist = erTilleggsfristBenyttet(foreldelse);
        boolean ignorerteSmåbeløp = heleVurderingPgaSmåbeløp(vedtakResultatType, vilkårPerioder);
        boolean renter = erRenterBenyttet(vilkårPerioder);

        List<String> hjemler = new ArrayList<>();

        if (!vilkårPerioder.isEmpty()) {
            if (ignorerteSmåbeløp) {
                hjemler.add("folketrygdloven § 22-15 sjette ledd");
            } else if (renter) {
                hjemler.add("folketrygdloven §§ 22-15 og 22-17 a");
            } else {
                hjemler.add("folketrygdloven § 22-15");
            }
        }

        if (foreldetMedTilleggsfrist) {
            hjemler.add("foreldelsesloven §§ 2, 3 og 10");
        } else if (foreldetVanlig) {
            hjemler.add("foreldelsesloven §§ 2 og 3");
        }
        if (effektForBruker.erRevurdering()) {
            if (effektForBruker.erTilGunstForBruker()) {
                hjemler.add("forvaltningsloven § 35 a)");
            } else {
                hjemler.add("forvaltningsloven § 35 c)");
            }
        }

        return join(hjemler, " og ");
    }

    private static boolean erRenterBenyttet(List<VilkårVurderingPeriodeEntitet> vilkårPerioder) {
        return vilkårPerioder.stream().anyMatch(v -> v.getAktsomhet() != null && Boolean.TRUE.equals(v.getAktsomhet().getIleggRenter()));
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

    private static String join(List<String> elementer, String sisteSkille) {
        StringBuilder builder = new StringBuilder();
        boolean første = true;
        for (int i = 0 ; i < elementer.size() ; i++) {
            String element = elementer.get(i);
            boolean siste = i == (elementer.size() - 1);
            if (!første && !siste) {
                builder.append(", ");
            }
            if (!første && siste) {
                builder.append(sisteSkille);
            }
            builder.append(element);
            første = false;
        }
        return builder.toString();
    }

    enum EffektForBruker {
        FØRSTEGANGSVEDTAK,
        ENDRET_TIL_GUNST_FOR_BRUKER,
        ENDRET_TIL_UGUNST_FOR_BRUKER;

        boolean erRevurdering() {
            return ENDRET_TIL_GUNST_FOR_BRUKER.equals(this) || ENDRET_TIL_UGUNST_FOR_BRUKER.equals(this);
        }

        boolean erTilGunstForBruker() {
            return ENDRET_TIL_GUNST_FOR_BRUKER.equals(this);
        }
    }
}
