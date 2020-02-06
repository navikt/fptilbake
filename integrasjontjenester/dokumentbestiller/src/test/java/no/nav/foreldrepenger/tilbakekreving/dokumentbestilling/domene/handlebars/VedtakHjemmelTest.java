package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.VedtakHjemmel;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class VedtakHjemmelTest {


    Periode periode = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    public void skal_gi_riktig_hjemmel_når_det_ikke_er_foreldelse_eller_renter() {
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, Function.identity());

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, false, false)).isEqualTo("folketrygdloven § 22-15");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_det_ikke_kreves_tilbake_pga_lavt_beløp() {
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, a -> a.medTilbakekrevSmåBeløp(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, false, false)).isEqualTo("folketrygdloven § 22-15 sjette ledd");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_alt_er_foreldet() {
        VurdertForeldelse vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.FORELDET));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, Collections.emptyList(), false, false)).isEqualTo("foreldelsesloven §§ 2 og 3");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_noe_er_foreldet_uten_tilleggsfrist_og_ikke_renter() {
        VurdertForeldelse vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.FORELDET));
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, false, false)).isEqualTo("folketrygdloven § 22-15 og foreldelsesloven §§ 2 og 3");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_foreldelse_er_vurdert_men_ikke_ilagt_uten_tilleggsfrist_og_renter() {
        VurdertForeldelse vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.IKKE_FORELDET));
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(true));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, false, false)).isEqualTo("folketrygdloven §§ 22-15 og 22-17 a");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_det_er_både_foreldelse_med_tilleggsfrist_og_ikke_renter() {
        VurdertForeldelse vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.TILLEGGSFRIST));
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(false));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, false, false)).isEqualTo("folketrygdloven § 22-15 og foreldelsesloven §§ 2, 3 og 10");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_det_er_både_foreldelse_med_tilleggsfrist_og_renter() {
        VurdertForeldelse vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.TILLEGGSFRIST));
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(true));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, false, false))
            .isEqualTo("folketrygdloven §§ 22-15 og 22-17 a og foreldelsesloven §§ 2, 3 og 10");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_det_ikke_er_foreldelse_eller_renter_er_klage() {
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, Function.identity());

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, null, vurderingPerioder, true, false))
            .isEqualTo("folketrygdloven § 22-15 og forvaltningsloven § 35 c)");
    }

    @Test
    public void skal_gi_riktig_hjemmel_når_det_er_både_foreldelse_med_tilleggsfrist_og_renter_er_klage() {
        VurdertForeldelse vurdertForeldelse = lagForeldelseperiode(periode, f -> f.medForeldelseVurderingType(ForeldelseVurderingType.TILLEGGSFRIST));
        List<VilkårVurderingPeriodeEntitet> vurderingPerioder = aktsomhet(periode, a -> a.medAktsomhet(Aktsomhet.GROVT_UAKTSOM).medIleggRenter(true));

        assertThat(VedtakHjemmel.lagHjemmelstekst(VedtakResultatType.INGEN_TILBAKEBETALING, vurdertForeldelse, vurderingPerioder, true, true))
            .isEqualTo("folketrygdloven §§ 22-15 og 22-17 a, foreldelsesloven §§ 2, 3 og 10 og forvaltningsloven § 35 a)");
    }

    private VurdertForeldelse lagForeldelseperiode(Periode periode, Function<VurdertForeldelsePeriode.Builder, VurdertForeldelsePeriode.Builder> oppsett) {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        VurdertForeldelsePeriode.Builder periodeBuilder = new VurdertForeldelsePeriode.Builder()
            .medVurdertForeldelse(vurdertForeldelse)
            .medPeriode(periode);
        vurdertForeldelse.leggTilVurderForeldelsePerioder(oppsett.apply(periodeBuilder).build());
        return vurdertForeldelse;
    }

    private List<VilkårVurderingPeriodeEntitet> aktsomhet(Periode periode, Function<VilkårVurderingAktsomhetEntitet.Builder, VilkårVurderingAktsomhetEntitet.Builder> oppsett) {
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet vurderingPeriode = new VilkårVurderingPeriodeEntitet.Builder()
            .medVurderinger(vurdering)
            .medPeriode(periode)
            .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
            .medBegrunnelse("foo")
            .build();
        VilkårVurderingAktsomhetEntitet.Builder builder = VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
            .medPeriode(vurderingPeriode)
            .medBegrunnelse("foo");

        VilkårVurderingAktsomhetEntitet aktsomhet = oppsett.apply(builder).build();
        vurderingPeriode.setAktsomhet(aktsomhet);
        return Collections.singletonList(vurderingPeriode);
    }
}
