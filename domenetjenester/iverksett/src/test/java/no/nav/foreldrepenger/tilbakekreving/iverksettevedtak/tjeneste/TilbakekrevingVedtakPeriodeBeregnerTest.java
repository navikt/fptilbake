package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import no.nav.vedtak.util.Objects;

@RunWith(CdiRunner.class)
public class TilbakekrevingVedtakPeriodeBeregnerTest {

    public static final DateTimeFormatter DATO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    private LocalDate dag1 = LocalDate.of(2019, 7, 1);
    private Periode uke1 = Periode.of(dag1, dag1.plusDays(6));
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
            .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(11000).medTilbakekrevBeløp(9000).medUinnkrevdBeløp(0).medSkattBeløp(0)));
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
            .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(11000).medTilbakekrevBeløp(0).medUinnkrevdBeløp(9000).medSkattBeløp(0)));
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
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medUinnkrevdBeløp(0).medSkattBeløp(0)),

            TilbakekrevingPeriode.med(uke2).medRenter(200)
                .medBeløp(TbkBeløp.feil(2000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(2000).medTilbakekrevBeløp(2000).medUinnkrevdBeløp(0).medSkattBeløp(0))
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
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(2000).medTilbakekrevBeløp(2000).medUinnkrevdBeløp(0).medSkattBeløp(0)),

            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(300).medUinnkrevdBeløp(700).medSkattBeløp(0))

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
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medUinnkrevdBeløp(0).medSkattBeløp(0))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPSNDFI).medNyttBeløp(0).medUtbetBeløp(1500).medTilbakekrevBeløp(1500).medUinnkrevdBeløp(0).medSkattBeløp(0))
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
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(334).medUinnkrevdBeløp(666).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(uke2).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(333).medUinnkrevdBeløp(667).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(333).medUinnkrevdBeløp(667).medSkattBeløp(0))
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
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(333).medTilbakekrevBeløp(333).medUinnkrevdBeløp(0).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(uke2).medRenter(0)
                .medBeløp(TbkBeløp.feil(334))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(334).medTilbakekrevBeløp(100).medUinnkrevdBeløp(234).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(333))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(333).medTilbakekrevBeløp(333).medUinnkrevdBeløp(0).medSkattBeløp(0))
        );
    }

    @Test
    public void skal_beregne_skatt_beløp_for_grunnlag_med_skatt_prosent_for_full_tilbakekreving() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
            )
        ), 100);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getNyttBeløp, KlasseType.FEIL)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUtbetaltBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(1000));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medUinnkrevdBeløp(0).medSkattBeløp(100)));

    }

    @Test
    public void skal_beregne_skatt_beløp_for_grunnlag_med_skatt_prosent_for_ingen_tilbakekreving() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
            )
        ), 100);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.godTro()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getNyttBeløp, KlasseType.FEIL)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUtbetaltBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(1000));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(0).medUinnkrevdBeløp(1000).medSkattBeløp(0)));

    }

    @Test
    public void skal_beregne_skatt_beløp_for_grunnlag_med_skatt_prosent_for_delvis_tilbakekreving() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke1, Arrays.asList(
                KgBeløp.feil(1000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
            )
        ), 100);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(500)
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getNyttBeløp, KlasseType.FEIL)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUtbetaltBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(1000));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(1000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(500).medUinnkrevdBeløp(500).medSkattBeløp(50)));

    }

    @Test
    public void skal_beregne_skatt_beløp_for_grunnlag_med_skatt_prosent_for_full_tilbakekreving_når_total_skatt_beløp_blir_høyere_enn_skattBeløpMnd() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();
        Periode uke1 = Periode.of(LocalDate.of(2019, 8, 5), LocalDate.of(2019, 8, 11));
        Periode uke2 = Periode.of(LocalDate.of(2019, 8, 12), LocalDate.of(2019, 8, 23));
        Periode uke3 = Periode.of(LocalDate.of(2019, 8, 24), LocalDate.of(2019, 8, 31));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            Periode.omsluttende(uke1, uke2, uke3), Arrays.asList(
                KgBeløp.feil(3180).medSkattProsent(BigDecimal.valueOf(9.5041)),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(6380).medTilbakekrevBeløp(3180).medNyttBeløp(3200).medSkattProsent(BigDecimal.valueOf(9.5041))
            )
        ), 302);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke1, VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom(),
            uke2, VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom(),
            uke3, VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getNyttBeløp, KlasseType.FEIL)).isEqualByComparingTo(BigDecimal.valueOf(3180));
        assertThat(finSumAv(resultat, TilbakekrevingBeløp::getUtbetaltBeløp, KlasseType.YTEL)).isEqualByComparingTo(BigDecimal.valueOf(6380));

        //resterende asserts i testen definerer i hvilken periode den ekstra kronen legges. Det er sannsynligvis ikke viktig.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(uke1).medRenter(0)
                .medBeløp(TbkBeløp.feil(795))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(800).medUtbetBeløp(1595).medTilbakekrevBeløp(795).medUinnkrevdBeløp(0).medSkattBeløp(75)),
            TilbakekrevingPeriode.med(uke2).medRenter(0)
                .medBeløp(TbkBeløp.feil(1590))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(1600).medUtbetBeløp(3190).medTilbakekrevBeløp(1590).medUinnkrevdBeløp(0).medSkattBeløp(151)),
            TilbakekrevingPeriode.med(uke3).medRenter(0)
                .medBeløp(TbkBeløp.feil(795))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(800).medUtbetBeløp(1595).medTilbakekrevBeløp(795).medUinnkrevdBeløp(0).medSkattBeløp(75)));
    }

    @Test
    public void skal_begrense_avrunding_av_skatt_slik_at_skatt_ikke_går_over_maksgrensen_for_måneden_selv_når_måneden_er_splittet_i_flere_kravgrunnlagperioder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();
        Periode janDel1 = Periode.of(LocalDate.of(2019, 1, 18), LocalDate.of(2019, 1, 24));
        Periode janDel2 = Periode.of(LocalDate.of(2019, 1, 25), LocalDate.of(2019, 1, 31));
        Periode feb = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
        Periode mars = Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            janDel1, Arrays.asList(
                KgBeløp.feil(2).medSkattProsent(BigDecimal.valueOf(50)),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(2).medTilbakekrevBeløp(2).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
            ),
            janDel2, Arrays.asList(
                KgBeløp.feil(2).medSkattProsent(BigDecimal.valueOf(50)),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(2).medTilbakekrevBeløp(2).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
            ),
            feb, Arrays.asList(
                KgBeløp.feil(1).medSkattProsent(BigDecimal.valueOf(50)),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1).medTilbakekrevBeløp(1).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
            ),
            mars, Arrays.asList(
                KgBeløp.feil(1).medSkattProsent(BigDecimal.valueOf(50)),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1).medTilbakekrevBeløp(1).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
            )),
            Map.of(
                janDel1, 2,
                janDel2, 2,
                feb, 1,
                mars, 1
            ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            Periode.omsluttende(janDel1, mars), VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        //det viktigste her er at skatt fordeles slik at den ikke overstiger "kvoten" skatt pr mnd slik det er definert i grunnlaget
        //det er viktig at det kommer 1 kr skatt pr periode i januar,
        //og 1 krone i februar eller i mars.
        assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(janDel1).medRenter(0)
                .medBeløp(TbkBeløp.feil(2))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(2).medTilbakekrevBeløp(2).medUinnkrevdBeløp(0).medSkattBeløp(1)),
            TilbakekrevingPeriode.med(janDel2).medRenter(0)
                .medBeløp(TbkBeløp.feil(2))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(2).medTilbakekrevBeløp(2).medUinnkrevdBeløp(0).medSkattBeløp(1)),
            TilbakekrevingPeriode.med(feb).medRenter(0)
                .medBeløp(TbkBeløp.feil(1))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1).medTilbakekrevBeløp(1).medUinnkrevdBeløp(0).medSkattBeløp(1)),
            TilbakekrevingPeriode.med(mars).medRenter(0)
                .medBeløp(TbkBeløp.feil(1))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1).medTilbakekrevBeløp(1).medUinnkrevdBeløp(0).medSkattBeløp(0)));
    }


    static KravgrunnlagTestBuilder.KgPeriode grunnlagPeriode(String periodeTekst, int utbetalt, int nytt, int tilbakekreves, String skatteprosent, int maxSkattMnd) {
        Periode periode = parsePeriode(periodeTekst);
        Objects.check(tilbakekreves == utbetalt - nytt, "Tilbakekreves er feil");
        List<KgBeløp> kgBeløp = Arrays.asList(
            KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(utbetalt).medNyttBeløp(nytt).medTilbakekrevBeløp(tilbakekreves).medSkattProsent(new BigDecimal(skatteprosent)),
            KgBeløp.feil(tilbakekreves).medSkattProsent(new BigDecimal(skatteprosent)));
        return new KravgrunnlagTestBuilder.KgPeriode(periode, kgBeløp, BigDecimal.valueOf(maxSkattMnd));
    }

    static Periode parsePeriode(String input) {
        String[] split = input.split(",");
        return Periode.of(parse(split[0]), parse(split[1]));
    }

    static LocalDate parse(String input) {
        return LocalDate.parse(input, DATO_FORMATTER);
    }

    @Test
    public void skal_ikke_kreve_høyere_skatt_i_måneden_enn_grensen_case_fra_testmiljø_som_trigget_en_feil_her() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();
        List<KravgrunnlagTestBuilder.KgPeriode> kgData = Arrays.asList(
            grunnlagPeriode("2019-04-23,2019-04-30", 5526, 3690, 1836, "21.9869", 404),
            grunnlagPeriode("2019-05-01,2019-05-15", 10131, 6765, 3366, "21.9987", 1548),
            grunnlagPeriode("2019-05-16,2019-05-31", 11052, 7380, 3672, "21.9987", 1548),
            grunnlagPeriode("2019-06-01,2019-06-26", 16578, 11070, 5508, "21.9978", 1346),
            grunnlagPeriode("2019-06-27,2019-06-30", 1842, 1230, 612, "21.9978", 1346),
            grunnlagPeriode("2019-07-01,2019-07-31", 21183, 14145, 7038, "21.9987", 1548),
            grunnlagPeriode("2019-08-01,2019-08-31", 20262, 13530, 6732, "21.9968", 1481),
            grunnlagPeriode("2019-09-01,2019-09-25", 16578, 11070, 5508, "21.9998", 1414),
            grunnlagPeriode("2019-09-26,2019-09-30", 2763, 1845, 918, "21.9998", 1414),
            grunnlagPeriode("2019-10-01,2019-10-31", 21183, 14145, 7038, "21.9987", 1548),
            grunnlagPeriode("2019-11-01,2019-11-30", 19341, 12915, 6426, "21.9998", 1414),
            grunnlagPeriode("2019-12-01,2019-12-31", 20262, 13530, 6732, "10.9959", 740));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, kgData);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            parsePeriode("2019-04-23,2019-05-08"), VilkårsvurderingTestBuilder.VVurdering.grovtUaktsom(77),
            parsePeriode("2019-05-09,2019-12-31"), VilkårsvurderingTestBuilder.VVurdering.forsett())
        );

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        //fordeling av skatt mellom perioder er ikke kritisk, men sum pr måned MÅ være under grensen for måneden
        assertThat(resultat).containsOnly(
            tilbakekrevingPeriode("2019-04-23,2019-04-30", 5526, 3690, 1413, 0, 310),
            tilbakekrevingPeriode("2019-05-01,2019-05-08", 5526, 3690, 1414, 0, 311),
            tilbakekrevingPeriode("2019-05-09,2019-05-15", 4605, 3075, 1530, 153, 337),
            tilbakekrevingPeriode("2019-05-16,2019-05-31", 11052, 7380, 3672, 367, 808),
            tilbakekrevingPeriode("2019-06-01,2019-06-26", 16578, 11070, 5508, 551, 1212),
            tilbakekrevingPeriode("2019-06-27,2019-06-30", 1842, 1230, 612, 61, 134),
            tilbakekrevingPeriode("2019-07-01,2019-07-31", 21183, 14145, 7038, 704, 1548),
            tilbakekrevingPeriode("2019-08-01,2019-08-31", 20262, 13530, 6732, 673, 1481),
            tilbakekrevingPeriode("2019-09-01,2019-09-25", 16578, 11070, 5508, 551, 1212),
            tilbakekrevingPeriode("2019-09-26,2019-09-30", 2763, 1845, 918, 92, 202),
            tilbakekrevingPeriode("2019-10-01,2019-10-31", 21183, 14145, 7038, 704, 1548),
            tilbakekrevingPeriode("2019-11-01,2019-11-30", 19341, 12915, 6426, 643, 1413),
            tilbakekrevingPeriode("2019-12-01,2019-12-31", 20262, 13530, 6732, 673, 740));
    }

    static TilbakekrevingPeriode tilbakekrevingPeriode(String periode, int utbetalt, int nytt, int tilbakekreves, int renter, int skattbeløp) {
        Periode p = parsePeriode(periode);
        return TilbakekrevingPeriode.med(p).medRenter(renter)
            .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(utbetalt).medNyttBeløp(nytt).medTilbakekrevBeløp(tilbakekreves).medUinnkrevdBeløp(utbetalt - nytt - tilbakekreves).medSkattBeløp(skattbeløp))
            .medBeløp(TbkBeløp.feil(utbetalt - nytt));
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
