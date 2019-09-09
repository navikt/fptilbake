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

    public static String lagHjemmelstekst(VedtakResultatType vedtakResultatType, VurdertForeldelse foreldelse, List<VilkårVurderingPeriodeEntitet> vilkårPerioder) {
        boolean foreldelseErVurdert = foreldelse != null;
        boolean foreldelseValgtTilleggsfrist = erTilleggsfristBenyttet(foreldelse);
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

        if (foreldelseValgtTilleggsfrist) {
            hjemler.add("foreldelsesloven §§ 2, 3 og 10");
        } else if (foreldelseErVurdert) {
            hjemler.add("foreldelsesloven §§ 2 og 3");
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

    private static String join(List<String> elementer, String skille) {
        StringBuilder builder = new StringBuilder();
        boolean første = true;
        for (String element : elementer) {
            if (!første) {
                builder.append(skille);
            }
            builder.append(element);
            første = false;
        }
        return builder.toString();
    }

}
