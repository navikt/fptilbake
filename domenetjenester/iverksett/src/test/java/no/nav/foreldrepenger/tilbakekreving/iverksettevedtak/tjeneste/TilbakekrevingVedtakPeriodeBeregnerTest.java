package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder.KgBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.VilkårsvurderingTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste.TbkBeløp;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TilbakekrevingVedtakPeriodeBeregnerTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ScenarioSimple simple = ScenarioSimple.simple();

    @Inject
    public BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    public KravgrunnlagRepository kravgrunnlagRepository;
    @Inject
    public VilkårsvurderingRepository vilkårsvurderingRepository;
    @Inject
    public TilbakekrevingVedtakPeriodeBeregner beregner;

    @Inject
    public VurdertForeldelseRepository foreldelseRepository;

    private LocalDate nå = LocalDate.now();
    private Periode uke1 = Periode.of(nå, nå.plusDays(6));
    private Periode uke2 = uke1.plusDays(7);
    private Periode uke3 = uke1.plusDays(14);
    private Periode uke1og2 = Periode.omsluttende(uke1, uke2);

    @Test
    public void skal_sende_tilbake_perioder_fra_grunnlag_ved_full_innkreving_og_ingen_splitting() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(9000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(9000),
                KgBeløp.trekk(2000))
            )
        );
        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(TilbakekrevingPeriode.med(uke1).medRenter(900)
            .medBeløp(TbkBeløp.feil(9000))
            .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(11000).medTilbakekrevBeløp(9000).medUinnkrevdBeløp(0).medSkattProsent(0)));
    }

    @Test
    public void skal_sende_tilbake_perioder_fra_grunnlag_og_ikke_kreve_noe_tilbake_ved_foreldelse() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(9000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(9000),
                KgBeløp.trekk(2000))
            )
        );

        VurdertForeldelse foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(VurdertForeldelsePeriode.builder()
            .medVurdertForeldelse(foreldelse)
            .medPeriode(uke1)
            .medForeldelseVurderingType(ForeldelseVurderingType.FORELDET)
            .medBegrunnelse("foo")
            .build());
        foreldelseRepository.lagre(behandlingId, foreldelse);

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(TilbakekrevingPeriode.med(uke1).medRenter(0)
            .medBeløp(TbkBeløp.feil(9000))
            .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(11000).medTilbakekrevBeløp(0).medUinnkrevdBeløp(9000).medSkattProsent(0)));
    }

    @Test
    public void skal_fordele_en_lang_vedtaksperiode_ut_på_2_grunnlagsperioder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000))
            ,
            uke2, Arrays.asList(
                KgBeløp.feil(2000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(2000).medTilbakekrevBeløp(2000))
            )
        );

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1og2, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(100)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medUinnkrevdBeløp(0).medSkattProsent(0)),

            TilbakekrevingPeriode.med(uke2).medRenter(200)
                .medBeløp(TbkBeløp.feil(2000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(2000).medTilbakekrevBeløp(2000).medUinnkrevdBeløp(0).medSkattProsent(0))
        );
    }

    @Test
    public void skal_fordele_en_lang_grunnlagsperiode_ut_på_to_vedtaksperioder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Periode uke1til3 = Periode.omsluttende(uke1, uke3);

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1til3, Arrays.asList(
                KgBeløp.feil(3000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(3000).medTilbakekrevBeløp(3000).medSkattProsent(0))
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1og2, VilkårsvurderingTestBuilder.VVurdering.forsett(),
            uke3, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(300)
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1og2).medRenter(200)
                .medBeløp(TbkBeløp.feil(2000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(2000).medTilbakekrevBeløp(2000).medUinnkrevdBeløp(0).medSkattProsent(0)),

            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(300).medUinnkrevdBeløp(700).medSkattProsent(0))

        );
    }

    @Test
    public void skal_fordele_på_ulike_klassekoder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(2500),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0),
                KgBeløp.ytelse(KlasseKode.FPSNDFI).medUtbetBeløp(1500).medTilbakekrevBeløp(1500).medSkattProsent(0))
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(250)
                .medBeløp(TbkBeløp.feil(2500))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medUinnkrevdBeløp(0).medSkattProsent(0))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPSNDFI).medNyttBeløp(0).medUtbetBeløp(1500).medTilbakekrevBeløp(1500).medUinnkrevdBeløp(0).medSkattProsent(0))
        );
    }

    @Test
    public void skal_tilpasse_avrunding_slik_at_tilbakekrevingsbeløp_fra_vedtaket_blir_eksakt_riktig() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0)
            ),
            uke2, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0)
            ),
            uke3, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0)
            )
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            Periode.omsluttende(uke1, uke3), VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(1000)
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getTilbakekrevBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUinnkrevdBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(2000));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(334).medUinnkrevdBeløp(666).medSkattProsent(0)),
            TilbakekrevingPeriode.med(uke2).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(333).medUinnkrevdBeløp(667).medSkattProsent(0)),
            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(333).medUinnkrevdBeløp(667).medSkattProsent(0))
        );
    }

    @Test
    public void skal_tilpasse_avrunding_slik_at_summer_fra_kravgrunnlagperioder_har_samme_summer_i_vedtaket() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            Periode.omsluttende(uke1, uke3), Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000)
            )
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(333),
            uke2, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(100),
            uke3, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(333)
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getNyttBeløp, KlasseType.FEIL)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUtbetaltBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(1000));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(333))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(333).medTilbakekrevBeløp(333).medUinnkrevdBeløp(0).medSkattProsent(0)),
            TilbakekrevingPeriode.med(uke2).medRenter(0)
                .medBeløp(TbkBeløp.feil(334))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(334).medTilbakekrevBeløp(100).medUinnkrevdBeløp(234).medSkattProsent(0)),
            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(333))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(333).medTilbakekrevBeløp(333).medUinnkrevdBeløp(0).medSkattProsent(0))
        );
    }

    @Test
    public void skal_beregne_netto_tilbakekreves_beløp_med_grunnlag_med_skatt_prosent(){
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            Periode.omsluttende(uke1, uke3), Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
            )
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(333),
            uke2, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(100),
            uke3, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(333)
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getNyttBeløp, KlasseType.FEIL)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUtbetaltBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(1000));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(333))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(333).medTilbakekrevBeløp(300).medUinnkrevdBeløp(33).medSkattProsent(10)),
            TilbakekrevingPeriode.med(uke2).medRenter(0)
                .medBeløp(TbkBeløp.feil(334))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(334).medTilbakekrevBeløp(90).medUinnkrevdBeløp(244).medSkattProsent(10)),
            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(333))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(333).medTilbakekrevBeløp(300).medUinnkrevdBeløp(33).medSkattProsent(10))
        );

    }

    @Test
    public void skal_feile_når_det_finnes_kgperiode_som_ikke_helt_overlapper_med_brPerioder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1og2, Arrays.asList(
                KgBeløp.feil(2500),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000))
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        //TODO det bør helt klart feile tidligere i løypa istedet for på dette punktet. Legger validering her nå for å
        // ha ekstra sikring mot feil
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("har 10 virkedager, forventer en-til-en, men ovelapper mot beregningsresultat med 5 dager");

        beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
    }

    @Test
    public void skal_feile_når_det_finnes_brperiode_som_ikke_helt_overlapper_med_kgPerioder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(2500),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000))
        ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1og2, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        //TODO det bør helt klart feile tidligere i løypa istedet for på dette punktet. Legger validering her nå for å
        // ha ekstra sikring mot feil
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("har 10 virkedager, forventer en-til-en, men ovelapper mot kravgrunnlag med 5 dager");

        beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
    }

    private static BigDecimal finSumAv(Collection<TilbakekrevingPeriode> perioder, Function<TilbakekrevingBeløp, BigDecimal> hva, KlasseType klasseType) {
        return perioder.stream()
            .flatMap(p -> p.getBeløp().stream())
            .filter(b -> b.getKlasseType() == klasseType)
            .map(hva)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void flushAndClear() {
        repositoryRule.getEntityManager().flush();
        repositoryRule.getEntityManager().clear();
    }
}
