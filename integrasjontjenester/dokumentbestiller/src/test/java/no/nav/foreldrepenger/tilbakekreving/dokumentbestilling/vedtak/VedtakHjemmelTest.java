package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class VedtakHjemmelTest {


    Periode periode = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    void skal_gi_riktig_hjemmel_når_det_ikke_er_foreldelse_eller_renter() {
        var vurderingPerioder = aktsomhet(periode, Function.identity());

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven § 22-15");
        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NN, true)).isEqualTo("folketrygdlova § 22-15");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_er_forsto_burde_forstått_og_forsett() {
        var vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.FORSETT).medIleggRenter(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven § 22-15");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_er_feilaktig_opplysninger_og_forsett() {
        var vurderingPerioder = aktsomhet(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                periode, a -> a.medAktsomhet(Aktsomhet.FORSETT));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven §§ 22-15 og 22-17 a");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_er_feilaktig_opplysninger_og_forsett_men_frisinn_og_dermed_ikke_renter() {
        var vurderingPerioder = aktsomhet(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                periode, a -> a.medAktsomhet(Aktsomhet.FORSETT));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, false)).isEqualTo("folketrygdloven § 22-15");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_ikke_kreves_tilbake_pga_lavt_beløp() {
        var vurderingPerioder = aktsomhet(periode, a -> a.medTilbakekrevSmåBeløp(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven § 22-15 sjette ledd");
        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NN, true)).isEqualTo("folketrygdlova § 22-15 sjette ledd");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_alt_er_foreldet() {
        var vurdertForeldelse = lagForeldelseperiode(periode, f -> {
            f.medForeldelseVurderingType(ForeldelseVurderingType.FORELDET);
            f.medForeldelsesFrist(periode.getFom().plusMonths(11));
            return f;
        });

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, Collections.emptyList(), VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("foreldelsesloven §§ 2 og 3");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_noe_er_foreldet_uten_tilleggsfrist_og_ikke_renter() {
        var vurdertForeldelse = lagForeldelseperiode(periode, f -> {
            f.medForeldelseVurderingType(ForeldelseVurderingType.FORELDET);
            f.medForeldelsesFrist(periode.getFom().plusMonths(11));
            return f;
        });
        var vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven § 22-15 og foreldelsesloven §§ 2 og 3");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_foreldelse_er_vurdert_men_ikke_ilagt_uten_tilleggsfrist_og_renter() {
        var vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.IKKE_FORELDET));
        var vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(true));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven §§ 22-15 og 22-17 a");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_er_både_foreldelse_med_tilleggsfrist_og_ikke_renter() {
        var vurdertForeldelse = lagForeldelseperiode(periode, f -> {
            f.medForeldelseVurderingType(ForeldelseVurderingType.TILLEGGSFRIST);
            f.medForeldelsesFrist(periode.getFom().plusMonths(11));
            f.medOppdagelseDato(periode.getFom().plusMonths(5));
            return f;
        });
        var vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true)).isEqualTo("folketrygdloven § 22-15 og foreldelsesloven §§ 2, 3 og 10");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_er_både_foreldelse_med_tilleggsfrist_og_renter() {
        var vurdertForeldelse = lagForeldelseperiode(periode, f -> {
            f.medForeldelseVurderingType(ForeldelseVurderingType.TILLEGGSFRIST);
            f.medForeldelsesFrist(periode.getFom().plusMonths(11));
            f.medOppdagelseDato(periode.getFom().plusMonths(5));
            return f;
        });
        var vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(true));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, VedtakHjemmel.EffektForBruker.FØRSTEGANGSVEDTAK, Språkkode.NB, true))
                .isEqualTo("folketrygdloven §§ 22-15 og 22-17 a og foreldelsesloven §§ 2, 3 og 10");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_ikke_er_foreldelse_eller_renter_er_klage() {
        var vurderingPerioder = aktsomhet(periode, Function.identity());

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, VedtakHjemmel.EffektForBruker.ENDRET_TIL_UGUNST_FOR_BRUKER, Språkkode.NB, true))
                .isEqualTo("folketrygdloven § 22-15 og forvaltningsloven § 35 c)");
    }

    @Test
    void skal_gi_riktig_hjemmel_når_det_er_både_foreldelse_med_tilleggsfrist_og_renter_er_klage() {
        var vurdertForeldelse = lagForeldelseperiode(periode, f -> {
            f.medForeldelseVurderingType(ForeldelseVurderingType.TILLEGGSFRIST);
            f.medForeldelsesFrist(periode.getFom().plusMonths(11));
            f.medOppdagelseDato(periode.getFom().plusMonths(5));
            return f;
        });
        var vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(true));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, VedtakHjemmel.EffektForBruker.ENDRET_TIL_GUNST_FOR_BRUKER, Språkkode.NB, true))
                .isEqualTo("folketrygdloven §§ 22-15 og 22-17 a, foreldelsesloven §§ 2, 3 og 10 og forvaltningsloven § 35 a)");
    }

    private VurdertForeldelse lagForeldelseperiode(Periode periode, Function<VurdertForeldelsePeriode.Builder, VurdertForeldelsePeriode.Builder> oppsett) {
        var vurdertForeldelse = new VurdertForeldelse();
        var periodeBuilder = new VurdertForeldelsePeriode.Builder()
                .medVurdertForeldelse(vurdertForeldelse)
                .medPeriode(periode)
                .medForeldelseVurderingType(ForeldelseVurderingType.UDEFINERT);
        vurdertForeldelse.leggTilVurderForeldelsePerioder(oppsett.apply(periodeBuilder).build());
        return vurdertForeldelse;
    }

    private List<VilkårVurderingPeriodeEntitet> aktsomhet(Periode periode,
                                                          Function<VilkårVurderingAktsomhetEntitet.Builder, VilkårVurderingAktsomhetEntitet.Builder> oppsett) {
        return aktsomhet(VilkårResultat.FORSTO_BURDE_FORSTÅTT, periode, oppsett);
    }

    private List<VilkårVurderingPeriodeEntitet> aktsomhet(VilkårResultat resultat,
                                                          Periode periode,
                                                          Function<VilkårVurderingAktsomhetEntitet.Builder, VilkårVurderingAktsomhetEntitet.Builder> oppsett) {
        var vurdering = new VilkårVurderingEntitet();
        var vurderingPeriode = new VilkårVurderingPeriodeEntitet.Builder()
                .medVurderinger(vurdering)
                .medPeriode(periode)
                .medVilkårResultat(resultat)
                .medBegrunnelse("foo")
                .build();
        var builder = VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
                .medPeriode(vurderingPeriode)
                .medBegrunnelse("foo");

        var aktsomhet = oppsett.apply(builder).build();
        vurderingPeriode.setAktsomhet(aktsomhet);
        return Collections.singletonList(vurderingPeriode);
    }
}
