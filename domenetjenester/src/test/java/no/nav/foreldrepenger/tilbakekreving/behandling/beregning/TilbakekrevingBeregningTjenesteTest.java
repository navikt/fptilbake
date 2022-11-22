package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

public class TilbakekrevingBeregningTjenesteTest extends FellesTestOppsett {

    private TilbakekrevingBeregningTjeneste tjeneste;

    @BeforeEach
    void setUp() {
        tjeneste = new TilbakekrevingBeregningTjeneste(repoProvider, kravgrunnlagBeregningTjeneste);
    }

    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_ikke_er_foreldet() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode, BigDecimal.ZERO);
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.IKKE_FORELDET, null);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(periode);
        assertThat(r.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(r.getVurdering()).isEqualTo(Aktsomhet.FORSETT);
        assertThat(r.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(r.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(r.getManueltSattTilbakekrevingsbeløp()).isNull();
        assertThat(r.getAndelAvBeløp()).isEqualByComparingTo(BigDecimal.valueOf(100));

        assertThat(beregningResultat.getVedtakResultatType()).isEqualByComparingTo(VedtakResultatType.FULL_TILBAKEBETALING);
    }

    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_gjelder_ikke_er_foreldelse() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode, BigDecimal.ZERO);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(periode);
        assertThat(r.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(r.getVurdering()).isEqualTo(Aktsomhet.FORSETT);
        assertThat(r.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(r.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(r.getManueltSattTilbakekrevingsbeløp()).isNull();
        assertThat(r.getAndelAvBeløp()).isEqualByComparingTo(BigDecimal.valueOf(100));

        assertThat(beregningResultat.getVedtakResultatType()).isEqualByComparingTo(VedtakResultatType.FULL_TILBAKEBETALING);
    }

    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_er_foreldet() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode, BigDecimal.ZERO);
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.FORELDET, periode.getFom().plusMonths(8));

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(periode);
        assertThat(r.getTilbakekrevingBeløp()).isZero();
        assertThat(r.getVurdering()).isEqualTo(AnnenVurdering.FORELDET);
        assertThat(r.getRenterProsent()).isNull();
        assertThat(r.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(r.getManueltSattTilbakekrevingsbeløp()).isNull();
        assertThat(r.getAndelAvBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(r.getRenteBeløp()).isZero();
        assertThat(r.getTilbakekrevingBeløpUtenRenter()).isZero();

        assertThat(beregningResultat.getVedtakResultatType()).isEqualByComparingTo(VedtakResultatType.INGEN_TILBAKEBETALING);
    }

    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_ikke_er_foreldet_medSkattProsent() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode, BigDecimal.valueOf(10));
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.IKKE_FORELDET, null);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(periode);
        assertThat(r.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(r.getVurdering()).isEqualTo(Aktsomhet.FORSETT);
        assertThat(r.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(r.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(r.getManueltSattTilbakekrevingsbeløp()).isNull();
        assertThat(r.getAndelAvBeløp()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(r.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(r.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(10000));

        assertThat(beregningResultat.getVedtakResultatType()).isEqualByComparingTo(VedtakResultatType.FULL_TILBAKEBETALING);
    }

    @Test
    public void skal_beregne_rigtig_beløp_og_utbetalt_beløp_for_periode() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode, BigDecimal.valueOf(10));
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.IKKE_FORELDET, null);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getUtbetaltYtelseBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(r.getRiktigYtelseBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_beregne_rigtig_beløp_og_utbetalt_beløp_ved_delvis_feilutbetaling_for_perioder_som_slås_sammen_i_logisk_periode() {
        BigDecimal skatteprosent = BigDecimal.valueOf(10);
        Periode periode1 = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));
        Periode periode2 = new Periode(LocalDate.of(2019, 5, 4), LocalDate.of(2019, 5, 6));
        Periode logiskPeriode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 6));
        Kravgrunnlag431 grunnlag = lagGrunnlag();

        KravgrunnlagPeriode432 grunnlagPeriode1 = lagGrunnlagPeriode(periode1, grunnlag);
        BigDecimal utbetalt1 = BigDecimal.valueOf(10000);
        BigDecimal nyttBeløp1 = BigDecimal.valueOf(5000);
        BigDecimal feilutbetalt1 = utbetalt1.subtract(nyttBeløp1);
        grunnlagPeriode1.leggTilBeløp(lagYtelBeløp(grunnlagPeriode1, utbetalt1, nyttBeløp1, skatteprosent));
        grunnlagPeriode1.leggTilBeløp(lagFeilBeløp(grunnlagPeriode1, feilutbetalt1));
        grunnlag.leggTilPeriode(grunnlagPeriode1);

        KravgrunnlagPeriode432 grunnlagPeriode2 = lagGrunnlagPeriode(periode2, grunnlag);
        BigDecimal utbetalt2 = BigDecimal.valueOf(10000);
        BigDecimal nyttBeløp2 = BigDecimal.valueOf(100);
        BigDecimal feilutbetalt2 = utbetalt2.subtract(nyttBeløp2);
        grunnlagPeriode2.leggTilBeløp(lagYtelBeløp(grunnlagPeriode2, utbetalt2, nyttBeløp2, skatteprosent));
        grunnlagPeriode2.leggTilBeløp(lagFeilBeløp(grunnlagPeriode2, feilutbetalt2));
        grunnlag.leggTilPeriode(grunnlagPeriode2);

        grunnlagRepository.lagre(internBehandlingId, grunnlag);

        lagForeldelse(internBehandlingId, logiskPeriode, ForeldelseVurderingType.IKKE_FORELDET, null);
        lagVilkårsvurderingMedForsett(internBehandlingId, logiskPeriode);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(logiskPeriode);
        assertThat(r.getUtbetaltYtelseBeløp()).isEqualByComparingTo(utbetalt1.add(utbetalt2));
        assertThat(r.getRiktigYtelseBeløp()).isEqualByComparingTo(nyttBeløp1.add(nyttBeløp2));
    }

    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_ikke_er_foreldet_medSkattProsent_når_beregnet_periode_er_på_tvers_av_grunnlag_periode() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));
        Periode periode1 = new Periode(LocalDate.of(2019, 5, 4), LocalDate.of(2019, 5, 6));
        Periode logiskPeriode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 6));
        Kravgrunnlag431 grunnlag = lagGrunnlag();
        KravgrunnlagPeriode432 grunnlagPeriode = lagGrunnlagPeriode(periode, grunnlag);
        grunnlagPeriode.leggTilBeløp(lagYtelBeløp(grunnlagPeriode, BigDecimal.valueOf(10000), BigDecimal.valueOf(10)));
        grunnlagPeriode.leggTilBeløp(lagFeilBeløp(grunnlagPeriode, BigDecimal.valueOf(10000)));

        KravgrunnlagPeriode432 grunnlagPeriode1 = lagGrunnlagPeriode(periode1, grunnlag);
        grunnlagPeriode1.leggTilBeløp(lagYtelBeløp(grunnlagPeriode1, BigDecimal.valueOf(10000), BigDecimal.valueOf(10)));
        grunnlagPeriode1.leggTilBeløp(lagFeilBeløp(grunnlagPeriode1, BigDecimal.valueOf(10000)));

        grunnlag.leggTilPeriode(grunnlagPeriode);
        grunnlag.leggTilPeriode(grunnlagPeriode1);
        grunnlagRepository.lagre(internBehandlingId, grunnlag);

        lagForeldelse(internBehandlingId, logiskPeriode, ForeldelseVurderingType.IKKE_FORELDET, null);
        lagVilkårsvurderingMedForsett(internBehandlingId, logiskPeriode);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(logiskPeriode);
        assertThat(r.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(22000));
        assertThat(r.getVurdering()).isEqualTo(Aktsomhet.FORSETT);
        assertThat(r.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(r.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(r.getManueltSattTilbakekrevingsbeløp()).isNull();
        assertThat(r.getAndelAvBeløp()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(r.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(r.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(20000));

        assertThat(beregningResultat.getVedtakResultatType()).isEqualByComparingTo(VedtakResultatType.FULL_TILBAKEBETALING);
    }

    @Test
    public void skal_ikke_overskyte_maks_skatt_pr_måned_selv_om_vurderingen_splitter_kravgrunnlaget() {
        LocalDate onsdag = LocalDate.of(2020, 1, 15);
        LocalDate torsdag = onsdag.plusDays(1);
        Periode kgPeriode = new Periode(onsdag, torsdag);
        Periode vurderingperiode1 = new Periode(onsdag, onsdag);
        Periode vurderingperiode2 = new Periode(torsdag, torsdag);
        Periode logiskPeriode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 6));
        Kravgrunnlag431 grunnlag = lagGrunnlag();
        KravgrunnlagPeriode432 grunnlagPeriode = lagGrunnlagPeriode(kgPeriode, grunnlag, 1);
        grunnlagPeriode.leggTilBeløp(lagYtelBeløp(grunnlagPeriode, BigDecimal.valueOf(2), BigDecimal.valueOf(99)));
        grunnlagPeriode.leggTilBeløp(lagFeilBeløp(grunnlagPeriode, BigDecimal.valueOf(2)));
        grunnlag.leggTilPeriode(grunnlagPeriode);
        grunnlagRepository.lagre(internBehandlingId, grunnlag);

        lagForeldelse(internBehandlingId, logiskPeriode, ForeldelseVurderingType.IKKE_FORELDET, null);
        lagVilkårsvurderingMedForsett(internBehandlingId, vurderingperiode1, vurderingperiode2);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getSkattBeløp().add(resultat.get(1).getSkattBeløp())).isLessThanOrEqualTo(BigDecimal.ONE);
    }

    @Test
    public void skal_ha_riktig_skatt_når_skatt_for_første_periode_er_høyere_enn_maks_skatt_for_første_måned_siden_første_periode_er_lang() {
        Periode kgPeriode0 = new Periode(LocalDate.of(2019, 3, 18), LocalDate.of(2019, 3, 29));
        Periode kgPeriode1 = new Periode(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
        Periode vurderingPeriode0 = new Periode(LocalDate.of(2019, 3, 18), LocalDate.of(2019, 4, 22));
        Periode vurderingPeriode1 = new Periode(LocalDate.of(2019, 4, 23), LocalDate.of(2019, 4, 30));
        Periode logiskPeriode = Periode.omsluttende(kgPeriode0, kgPeriode1);
        Kravgrunnlag431 grunnlag = lagGrunnlag();
        KravgrunnlagPeriode432 grunnlagPeriode0 = lagGrunnlagPeriode(kgPeriode0, grunnlag, 724);
        grunnlagPeriode0.leggTilBeløp(lagYtelBeløp(grunnlagPeriode0, BigDecimal.valueOf(8280), BigDecimal.valueOf(6150), new BigDecimal("33.9975")));
        grunnlagPeriode0.leggTilBeløp(lagFeilBeløp(grunnlagPeriode0, BigDecimal.valueOf(2130)));

        KravgrunnlagPeriode432 grunnlagPeriode1 = lagGrunnlagPeriode(kgPeriode1, grunnlag, 881);
        grunnlagPeriode1.leggTilBeløp(lagYtelBeløp(grunnlagPeriode1, BigDecimal.valueOf(18216), BigDecimal.valueOf(13530), new BigDecimal("18.8076")));
        grunnlagPeriode1.leggTilBeløp(lagFeilBeløp(grunnlagPeriode1, BigDecimal.valueOf(4686)));

        grunnlag.leggTilPeriode(grunnlagPeriode0);
        grunnlag.leggTilPeriode(grunnlagPeriode1);
        grunnlagRepository.lagre(internBehandlingId, grunnlag);

        lagForeldelse(internBehandlingId, logiskPeriode, ForeldelseVurderingType.IKKE_FORELDET, null);

        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet vurdering0 = VilkårVurderingPeriodeEntitet.builder()
                .medPeriode(vurderingPeriode0)
                .medBegrunnelse("foo")
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medVurderinger(vurdering)
                .build();
        vurdering0.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
                .medProsenterSomTilbakekreves(BigDecimal.valueOf(70))
                .medBegrunnelse("foo")
                .medPeriode(vurdering0)
                .build());
        VilkårVurderingPeriodeEntitet vurdering1 = VilkårVurderingPeriodeEntitet.builder()
                .medPeriode(vurderingPeriode1)
                .medBegrunnelse("foo")
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medVurderinger(vurdering)
                .build();
        vurdering1.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.FORSETT)
                .medBegrunnelse("foo")
                .medPeriode(vurdering1)
                .build());
        vurdering.leggTilPeriode(vurdering0);
        vurdering.leggTilPeriode(vurdering1);
        vilkårsvurderingRepository.lagre(internBehandlingId, vurdering);

        entityManager.flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(2);
        BeregningResultatPeriode brp0 = resultat.get(0);
        assertThat(brp0.getPeriode()).isEqualTo(vurderingPeriode0);
        assertThat(brp0.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(3877));
        assertThat(brp0.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(954));
        assertThat(brp0.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(3877 - 954));
        BeregningResultatPeriode brp1 = resultat.get(1);
        assertThat(brp1.getPeriode()).isEqualTo(vurderingPeriode1);
        assertThat(brp1.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(1278));
        assertThat(brp1.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(240));
        assertThat(brp1.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(128));
        assertThat(brp1.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(1278 - 240 + 128));
    }

    private void lagVilkårsvurderingMedForsett(Long behandlingId, Periode... perioder) {
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        for (Periode periode : perioder) {
            VilkårVurderingPeriodeEntitet p = VilkårVurderingPeriodeEntitet.builder()
                    .medPeriode(periode.getFom(), periode.getTom())
                    .medBegrunnelse("foo")
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medVurderinger(vurdering)
                    .build();
            p.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                    .medAktsomhet(Aktsomhet.FORSETT)
                    .medBegrunnelse("foo")
                    .medPeriode(p)
                    .build());
            vurdering.leggTilPeriode(p);
        }
        vilkårsvurderingRepository.lagre(behandlingId, vurdering);
    }

    private void lagForeldelse(Long behandlingId, Periode periode, ForeldelseVurderingType resultat, LocalDate foreldelsesFrist) {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        vurdertForeldelse.leggTilVurderForeldelsePerioder(VurdertForeldelsePeriode.builder()
                .medPeriode(periode)
                .medBegrunnelse("foo")
                .medForeldelseVurderingType(resultat)
                .medVurdertForeldelse(vurdertForeldelse)
                .medForeldelsesFrist(foreldelsesFrist)
                .build());
        vurdertForeldelseRepository.lagre(behandlingId, vurdertForeldelse);
    }

    private void lagKravgrunnlag(long behandlingId, Periode periode, BigDecimal skattProsent) {
        Kravgrunnlag431 grunnlag = lagGrunnlag();
        KravgrunnlagPeriode432 p = lagGrunnlagPeriode(periode, grunnlag);
        p.leggTilBeløp(lagFeilBeløp(p, BigDecimal.valueOf(10000)));
        p.leggTilBeløp(lagYtelBeløp(p, BigDecimal.valueOf(10000), skattProsent));
        grunnlag.leggTilPeriode(p);

        grunnlagRepository.lagre(behandlingId, grunnlag);
    }

    private KravgrunnlagBelop433 lagFeilBeløp(KravgrunnlagPeriode432 p, BigDecimal feilutbetaling) {
        return KravgrunnlagBelop433.builder()
                .medKlasseKode(KlasseKode.FPATORD)
                .medKlasseType(KlasseType.FEIL)
                .medNyBelop(feilutbetaling)
                .medKravgrunnlagPeriode432(p)
                .build();
    }


    private KravgrunnlagBelop433 lagYtelBeløp(KravgrunnlagPeriode432 p, BigDecimal utbetalt, BigDecimal skattProsent) {
        return lagYtelBeløp(p, utbetalt, BigDecimal.ZERO, skattProsent);
    }

    private KravgrunnlagBelop433 lagYtelBeløp(KravgrunnlagPeriode432 p, BigDecimal utbetalt, BigDecimal nyttBeløp, BigDecimal skattProsent) {
        return KravgrunnlagBelop433.builder()
                .medKlasseKode(KlasseKode.FPATORD)
                .medKlasseType(KlasseType.YTEL)
                .medNyBelop(nyttBeløp)
                .medOpprUtbetBelop(utbetalt)
                .medTilbakekrevesBelop(utbetalt.subtract(nyttBeløp))
                .medUinnkrevdBelop(BigDecimal.ZERO)
                .medSkattProsent(skattProsent)
                .medKravgrunnlagPeriode432(p).build();
    }

    private KravgrunnlagPeriode432 lagGrunnlagPeriode(Periode periode, Kravgrunnlag431 grunnlag) {
        return lagGrunnlagPeriode(periode, grunnlag, 1000);
    }

    private KravgrunnlagPeriode432 lagGrunnlagPeriode(Periode periode, Kravgrunnlag431 grunnlag, int skattMnd) {
        return KravgrunnlagPeriode432.builder()
                .medPeriode(periode)
                .medKravgrunnlag431(grunnlag)
                .medBeløpSkattMnd(BigDecimal.valueOf(skattMnd))
                .build();
    }

    private Kravgrunnlag431 lagGrunnlag() {
        return Kravgrunnlag431.builder()
                .medVedtakId(1111L)
                .medEksternKravgrunnlagId("123")
                .medKravStatusKode(KravStatusKode.NYTT)

                //TODO feltene under skal valideres at finnes i builder, siden de er påkrevd i databasen
                //TODO vurder om alle feltene skal lagres i fptilbake, der er mye her som ikke brukes av fptilbake
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medFagSystemId(Fagsystem.FPTILBAKE.getKode())
                .medGjelderVedtakId("vedtakid-123")
                .medGjelderType(GjelderType.PERSON)
                .medUtbetalesTilId("12345678901")
                .medUtbetIdType(GjelderType.PERSON)
                .medAnsvarligEnhet("8020")
                .medBostedEnhet("8020")
                .medBehandlendeEnhet("8020")
                .medFeltKontroll("kontroll-123")
                .medSaksBehId("VL")
                .build();
    }

}
