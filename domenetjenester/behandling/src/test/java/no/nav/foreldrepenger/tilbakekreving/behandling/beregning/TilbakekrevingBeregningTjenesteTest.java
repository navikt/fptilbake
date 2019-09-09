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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TilbakekrevingBeregningTjenesteTest extends FellesTestOppsett {

    TilbakekrevingBeregningTjeneste tjeneste = new TilbakekrevingBeregningTjeneste(vurdertForeldelseTjeneste, repoProvider);


    @Test
    public void skal_beregne_tilbakekrevingsbeløp_for_periode_som_ikke_er_foreldet() {
        Periode periode = new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3));

        lagKravgrunnlag(internBehandlingId, periode);
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.IKKE_FORELDET);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

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

        lagKravgrunnlag(internBehandlingId, periode);
        lagVilkårsvurderingMedForsett(internBehandlingId, periode);

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

        lagKravgrunnlag(internBehandlingId, periode);
        lagForeldelse(internBehandlingId, periode, ForeldelseVurderingType.FORELDET);

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
        VilkårVurderingAggregateEntitet vurderingAggregat = VilkårVurderingAggregateEntitet.builder()
            .medBehandlingId(behandlingId)
            .medManuellVilkår(vurdering)
            .medAktiv(true)
            .build();
        vilkårsvurderingRepository.lagre(vurderingAggregat);
    }

    private void lagTomVilkårsvurdering(Long behandlingId) {
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingAggregateEntitet vurderingAggregat = VilkårVurderingAggregateEntitet.builder()
            .medBehandlingId(behandlingId)
            .medManuellVilkår(vurdering)
            .medAktiv(true)
            .build();
        vilkårsvurderingRepository.lagre(vurderingAggregat);
    }

    private void lagForeldelse(Long behandlingId, Periode periode, ForeldelseVurderingType resultat) {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        vurdertForeldelse.leggTilVurderForeldelsePerioder(VurdertForeldelsePeriode.builder()
            .medPeriode(periode)
            .medBegrunnelse("foo")
            .medForeldelseVurderingType(resultat)
            .medVurdertForeldelse(vurdertForeldelse)
            .build());

        VurdertForeldelseAggregate aggregate = VurdertForeldelseAggregate.builder()
            .medAktiv(true)
            .medBehandlingId(behandlingId)
            .medVurdertForeldelse(vurdertForeldelse)
            .build();
        vurdertForeldelseRepository.lagre(aggregate);
    }

    private void lagKravgrunnlag(long behandlingId, Periode periode) {
        Kravgrunnlag431 grunnlag = Kravgrunnlag431.builder()
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
        KravgrunnlagPeriode432 p = KravgrunnlagPeriode432.builder()
            .medPeriode(periode)
            .medKravgrunnlag431(grunnlag)
            .build();
        p.leggTilBeløp(KravgrunnlagBelop433.builder()
            .medKlasseKode(KlasseKode.FPATORD)
            .medKlasseType(KlasseType.FEIL)
            .medNyBelop(BigDecimal.valueOf(10000))
            //TODO feltene under skal valideres i builder:
            .medKravgrunnlagPeriode432(p)
            .build());
        grunnlag.leggTilPeriode(p);

        KravgrunnlagAggregate aggregat = new KravgrunnlagAggregate.Builder()
            .medBehandlingId(behandlingId)
            .medGrunnlagØkonomi(grunnlag)
            .medAktiv(true)
            .build();
        grunnlagRepository.lagre(aggregat);
    }

}
