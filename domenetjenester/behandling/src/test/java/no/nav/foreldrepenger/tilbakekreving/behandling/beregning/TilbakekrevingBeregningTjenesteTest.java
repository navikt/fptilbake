package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

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
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TilbakekrevingBeregningTjenesteTest extends FellesTestOppsett {

    private TilbakekrevingBeregningTjeneste tjeneste = new TilbakekrevingBeregningTjeneste(vurdertForeldelseTjeneste, repoProvider);

    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_ikke_er_foreldet() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode, BigDecimal.ZERO);
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.IKKE_FORELDET);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

        flush();

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

        flush();

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
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.FORELDET);

        flush();

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
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.IKKE_FORELDET);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

        flush();

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
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_ikke_er_foreldet_medSkattProsent_når_beregnet_periode_er_på_tvers_av_grunnlag_periode() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));
        Periode periode1 = new Periode(LocalDate.of(2019, 5, 4), LocalDate.of(2019, 5, 6));
        Periode logikkPeriode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 6));
        Kravgrunnlag431 grunnlag = lagGrunnlag();
        KravgrunnlagPeriode432 grunnlagPeriode = lagGrunnlagPeriode(periode, grunnlag);
        grunnlagPeriode.leggTilBeløp(lagYtelBeløp(BigDecimal.valueOf(10), grunnlagPeriode));
        grunnlagPeriode.leggTilBeløp(lagFeilBeløp(grunnlagPeriode));

        KravgrunnlagPeriode432 grunnlagPeriode1 = lagGrunnlagPeriode(periode1, grunnlag);
        grunnlagPeriode1.leggTilBeløp(lagYtelBeløp(BigDecimal.valueOf(10), grunnlagPeriode1));
        grunnlagPeriode1.leggTilBeløp(lagFeilBeløp(grunnlagPeriode1));

        grunnlag.leggTilPeriode(grunnlagPeriode);
        grunnlag.leggTilPeriode(grunnlagPeriode1);
        grunnlagRepository.lagre(internBehandlingId, grunnlag);

        lagForeldelse(internBehandlingId, logikkPeriode, ForeldelseVurderingType.IKKE_FORELDET);
        lagVilkårsvurderingMedForsett(internBehandlingId, logikkPeriode);

        flush();

        BeregningResultat beregningResultat = tjeneste.beregn(internBehandlingId);
        List<BeregningResultatPeriode> resultat = beregningResultat.getBeregningResultatPerioder();

        assertThat(resultat).hasSize(1);
        BeregningResultatPeriode r = resultat.get(0);
        assertThat(r.getPeriode()).isEqualTo(logikkPeriode);
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

    private void flush() {
        repoRule.getEntityManager().flush();
    }


    private void lagVilkårsvurderingMedForsett(Long behandlingId, Periode periode) {
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet p = VilkårVurderingPeriodeEntitet.builder()
            .medPeriode(periode.getFom(), periode.getTom())
            .medBegrunnelse("foo")
            //TODO følgende skal legges til i validering i builder, siden det er påkrevet i modellen:
            .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
            .medVurderinger(vurdering)
            .build();
        p.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.FORSETT)
            .medBegrunnelse("foo")
            .medPeriode(p)
            .build());
        vurdering.leggTilPeriode(p);
        vilkårsvurderingRepository.lagre(behandlingId, vurdering);
    }

    private void lagForeldelse(Long behandlingId, Periode periode, ForeldelseVurderingType resultat) {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        vurdertForeldelse.leggTilVurderForeldelsePerioder(VurdertForeldelsePeriode.builder()
            .medPeriode(periode)
            .medBegrunnelse("foo")
            .medForeldelseVurderingType(resultat)
            .medVurdertForeldelse(vurdertForeldelse)
            .build());
        vurdertForeldelseRepository.lagre(behandlingId, vurdertForeldelse);
    }

    private void lagKravgrunnlag(long behandlingId, Periode periode, BigDecimal skattProsent) {
        Kravgrunnlag431 grunnlag = lagGrunnlag();
        KravgrunnlagPeriode432 p = lagGrunnlagPeriode(periode, grunnlag);
        p.leggTilBeløp(lagFeilBeløp(p));
        p.leggTilBeløp(lagYtelBeløp(skattProsent, p));
        grunnlag.leggTilPeriode(p);

        grunnlagRepository.lagre(behandlingId, grunnlag);
    }

    private KravgrunnlagBelop433 lagFeilBeløp(KravgrunnlagPeriode432 p) {
        return KravgrunnlagBelop433.builder()
            .medKlasseKode(KlasseKode.FPATORD)
            .medKlasseType(KlasseType.FEIL)
            .medNyBelop(BigDecimal.valueOf(10000))
            //TODO feltene under skal valideres i builder:
            .medKravgrunnlagPeriode432(p)
            .build();
    }

    private KravgrunnlagBelop433 lagYtelBeløp(BigDecimal skattProsent, KravgrunnlagPeriode432 p) {
        return KravgrunnlagBelop433.builder()
            .medKlasseKode(KlasseKode.FPATORD)
            .medKlasseType(KlasseType.YTEL)
            .medNyBelop(BigDecimal.ZERO)
            .medOpprUtbetBelop(BigDecimal.valueOf(10000))
            .medTilbakekrevesBelop(BigDecimal.valueOf(10000))
            .medUinnkrevdBelop(BigDecimal.ZERO)
            .medSkattProsent(skattProsent)
            //TODO feltene under skal valideres i builder:
            .medKravgrunnlagPeriode432(p).build();
    }

    private KravgrunnlagPeriode432 lagGrunnlagPeriode(Periode periode, Kravgrunnlag431 grunnlag) {
        return KravgrunnlagPeriode432.builder()
            .medPeriode(periode)
            .medKravgrunnlag431(grunnlag)
            .medBeløpSkattMnd(BigDecimal.valueOf(1000))
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
