package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder.KgBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.VilkårsvurderingTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.exception.TekniskException;

@CdiDbAwareTest
public class TilbakekrevingVedtakPeriodeBeregnerTest {

    public static final DateTimeFormatter DATO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ScenarioSimple simple = ScenarioSimple.simple();

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
    @Inject
    private EntityManager entityManager;

    private final LocalDate dag1 = LocalDate.of(2019, 7, 1);
    private final Periode uke1 = Periode.of(dag1, dag1.plusDays(6));
    private final Periode uke2 = uke1.plusDays(7);
    private final Periode uke3 = uke1.plusDays(14);
    private final Periode uke1og2 = Periode.omsluttende(uke1, uke2);

    @Test
    public void skal_sende_tilbake_perioder_fra_grunnlag_ved_full_innkreving_og_ingen_splitting() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
                        uke1, List.of(
                                KgBeløp.feil(9000),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(9000),
                                KgBeløp.trekk(2000))
                ),
                false);
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
                        uke1, List.of(
                                KgBeløp.feil(9000),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(9000),
                                KgBeløp.trekk(2000))
                ),
                false);

        VurdertForeldelse foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(VurdertForeldelsePeriode.builder()
                .medVurdertForeldelse(foreldelse)
                .medPeriode(uke1)
                .medForeldelseVurderingType(ForeldelseVurderingType.FORELDET)
                .medBegrunnelse("foo")
                .medForeldelsesFrist(uke1.getFom().plusMonths(8))
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
                        uke1, List.of(
                                KgBeløp.feil(1000),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000))
                        ,
                        uke2, List.of(
                                KgBeløp.feil(2000),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(2000).medTilbakekrevBeløp(2000))
                ),
                false);

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
                uke1til3, List.of(
                        KgBeløp.feil(3000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(3000).medTilbakekrevBeløp(3000).medSkattProsent(0))
        ), false);

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
    public void skal_fordele_på_ulike_klassekoder_med_ulik_skatt() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
                uke1, List.of(
                        KgBeløp.feil(2500),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(25),
                        KgBeløp.ytelse(KlasseKode.FPSNDFI).medUtbetBeløp(1500).medTilbakekrevBeløp(1500).medSkattProsent(0))
        ), false);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
                uke1, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(
                TilbakekrevingPeriode.med(uke1).medRenter(250)
                        .medBeløp(TbkBeløp.feil(2500))
                        .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medUinnkrevdBeløp(0).medSkattBeløp(250))
                        .medBeløp(TbkBeløp.ytelse(KlasseKode.FPSNDFI).medNyttBeløp(0).medUtbetBeløp(1500).medTilbakekrevBeløp(1500).medUinnkrevdBeløp(0).medSkattBeløp(0))
        );
    }

    @Test
    public void skal_tilpasse_avrunding_slik_at_tilbakekrevingsbeløp_fra_vedtaket_blir_eksakt_riktig() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
                uke1, List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0)
                ),
                uke2, List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0)
                ),
                uke3, List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(0)
                )
        ), false);

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
                Periode.omsluttende(uke1, uke3), List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000)
                )
        ), false);

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
                uke1, List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
                )
        ), 100, false);

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
                uke1, List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
                )
        ), 100, false);

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
                uke1, List.of(
                        KgBeløp.feil(1000),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000).medSkattProsent(10)
                )
        ), 100, false);

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
                Periode.omsluttende(uke1, uke2, uke3), List.of(
                        KgBeløp.feil(3180).medSkattProsent(BigDecimal.valueOf(9.5041)),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(6380).medTilbakekrevBeløp(3180).medNyttBeløp(3200).medSkattProsent(BigDecimal.valueOf(9.5041))
                )
        ), 302, false);

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
                        janDel1, List.of(
                                KgBeløp.feil(10).medSkattProsent(BigDecimal.valueOf(50)),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(10).medTilbakekrevBeløp(10).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
                        ),
                        janDel2, List.of(
                                KgBeløp.feil(10).medSkattProsent(BigDecimal.valueOf(50)),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(10).medTilbakekrevBeløp(10).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
                        ),
                        feb, List.of(
                                KgBeløp.feil(5).medSkattProsent(BigDecimal.valueOf(50)),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(5).medTilbakekrevBeløp(5).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
                        ),
                        mars, List.of(
                                KgBeløp.feil(6).medSkattProsent(BigDecimal.valueOf(50)),
                                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(6).medTilbakekrevBeløp(6).medNyttBeløp(0).medSkattProsent(BigDecimal.valueOf(50))
                        )),
                Map.of(
                        janDel1, 5,
                        janDel2, 5,
                        feb, 2,
                        mars, 3
                ));

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
                Periode.omsluttende(janDel1, mars), VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        //det viktigste her er at skatt fordeles slik at den ikke overstiger "kvoten" skatt pr mnd slik det er definert i grunnlaget
        //det er viktig at det kommer 1 kr skatt pr periode i januar,
        //og 0 krone i februar og i mars.
        assertThat(resultat).containsOnly(
                TilbakekrevingPeriode.med(janDel1).medRenter(1)
                        .medBeløp(TbkBeløp.feil(10))
                        .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(10).medTilbakekrevBeløp(10).medUinnkrevdBeløp(0).medSkattBeløp(5)),
                TilbakekrevingPeriode.med(janDel2).medRenter(1)
                        .medBeløp(TbkBeløp.feil(10))
                        .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(10).medTilbakekrevBeløp(10).medUinnkrevdBeløp(0).medSkattBeløp(5)),
                TilbakekrevingPeriode.med(feb).medRenter(0)
                        .medBeløp(TbkBeløp.feil(5))
                        .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(5).medTilbakekrevBeløp(5).medUinnkrevdBeløp(0).medSkattBeløp(2)),
                TilbakekrevingPeriode.med(mars).medRenter(1)
                        .medBeløp(TbkBeløp.feil(6))
                        .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medNyttBeløp(0).medUtbetBeløp(6).medTilbakekrevBeløp(6).medUinnkrevdBeløp(0).medSkattBeløp(3)));
    }


    static KravgrunnlagTestBuilder.KgPeriode grunnlagPeriode(String periodeTekst, int utbetalt, int nytt, int tilbakekreves, String skatteprosent, int maxSkattMnd) {
        Periode periode = parsePeriode(periodeTekst);
        List<KgBeløp> kgBeløp = List.of(
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
                tilbakekrevingPeriode("2019-04-23,2019-04-30", 5526, 3690, 1413, 0, 309),
                tilbakekrevingPeriode("2019-05-01,2019-05-08", 5526, 3690, 1414, 0, 311),
                tilbakekrevingPeriode("2019-05-09,2019-05-15", 4605, 3075, 1530, 153, 336),
                tilbakekrevingPeriode("2019-05-16,2019-05-31", 11052, 7380, 3672, 367, 807),
                tilbakekrevingPeriode("2019-06-01,2019-06-26", 16578, 11070, 5508, 551, 1211),
                tilbakekrevingPeriode("2019-06-27,2019-06-30", 1842, 1230, 612, 61, 134),
                tilbakekrevingPeriode("2019-07-01,2019-07-31", 21183, 14145, 7038, 704, 1548),
                tilbakekrevingPeriode("2019-08-01,2019-08-31", 20262, 13530, 6732, 673, 1480),
                tilbakekrevingPeriode("2019-09-01,2019-09-25", 16578, 11070, 5508, 551, 1211),
                tilbakekrevingPeriode("2019-09-26,2019-09-30", 2763, 1845, 918, 92, 201),
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
                uke1og2, List.of(
                        KgBeløp.feil(2500),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000))
        ), false);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
                uke1, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        //TODO det bør helt klart feile tidligere i løypa istedet for på dette punktet. Legger validering her nå for å
        // ha ekstra sikring mot feil
        assertThatThrownBy(() -> beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("har 10 virkedager, forventer en-til-en, men ovelapper mot beregningsresultat med 5 dager");
    }

    @Test
    public void skal_feile_når_det_finnes_brperiode_som_ikke_helt_overlapper_med_kgPerioder() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
                uke1, List.of(
                        KgBeløp.feil(2500),
                        KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(1000).medTilbakekrevBeløp(1000))
        ), false);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
                uke1og2, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        //TODO det bør helt klart feile tidligere i løypa istedet for på dette punktet. Legger validering her nå for å
        // ha ekstra sikring mot feil
        assertThatThrownBy(() -> beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("har 10 virkedager, forventer en-til-en, men ovelapper mot kravgrunnlag med 5 dager");
    }

    @Test
    public void skal_beregne_riktig_beløp_for_engangsstønad_i_helgen() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();
        var dag = LocalDate.of(2020, 11, 8);
        var dagsPeriodeHelg = Periode.of(dag, dag);

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
                        dagsPeriodeHelg, List.of(
                                KgBeløp.feil(84720),
                                KgBeløp.ytelse(KlasseKode.FPENFOD_OP).medUtbetBeløp(84720).medTilbakekrevBeløp(84720))
                ),
                true);
        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
                dagsPeriodeHelg, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        assertThat(resultat).containsOnly(TilbakekrevingPeriode.med(dagsPeriodeHelg).medRenter(8472)
                .medBeløp(TbkBeløp.feil(84720))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPENFOD_OP).medNyttBeløp(0).medUtbetBeløp(84720).medTilbakekrevBeløp(84720).medUinnkrevdBeløp(0).medSkattBeløp(0)));
    }

    @Test
    public void skal_ikke_kreve_høyere_midre_skatt_enn_spesifisert_i_kravgrunnlag() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();
        List<KravgrunnlagTestBuilder.KgPeriode> kgData = Arrays.asList(
            grunnlagPeriode("2022-01-31,2022-01-31", 2455, 0, 2455, "30.3869", 745),
            grunnlagPeriode("2022-02-01,2022-02-08", 14730, 0, 14730, "30.8472", 15145),
            grunnlagPeriode("2022-02-09,2022-02-28", 34370, 0, 34370, "30.8472", 15145),
            grunnlagPeriode("2022-03-01,2022-03-22", 39280, 0, 39280, "32.5387", 18372),
            grunnlagPeriode("2022-03-23,2022-03-31", 17185, 0, 17185, "32.5387", 18372),
            grunnlagPeriode("2022-04-01,2022-04-30", 51555, 0, 51555, "31.2055", 16087),
            grunnlagPeriode("2022-05-01,2022-05-24", 41735, 0, 41735, "31.7922", 17170),
            grunnlagPeriode("2022-05-25,2022-05-31", 12275, 0, 12275, "31.7922", 17170),
            grunnlagPeriode("2022-06-01,2022-06-30", 54010, 0, 54010, "31.7922", 17170),
            grunnlagPeriode("2022-07-01,2022-07-31", 51555, 0, 51555, "31.2055", 16087),
            grunnlagPeriode("2022-08-01,2022-08-02", 4910, 0, 4910, "30.4684", 1495),
            grunnlagPeriode("2022-09-07,2022-09-30", 44190, 0, 44190, "34.2023", 15113));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, kgData);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            parsePeriode("2022-01-31,2022-08-02"), VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom(),
            parsePeriode("2022-09-07,2022-09-30"), VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom())
        );

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);

        //fordeling av skatt mellom perioder er ikke kritisk, men sum pr måned MÅ være under grensen for måneden
        assertThat(resultat).containsOnly(
            tilbakekrevingPeriode("2022-01-31,2022-01-31", 2455, 0, 2455,  0,745),
            tilbakekrevingPeriode("2022-02-01,2022-02-08", 14730, 0, 14730, 0  , 4543),
            tilbakekrevingPeriode("2022-02-09,2022-02-28", 34370, 0, 34370, 0  , 10602),
            tilbakekrevingPeriode("2022-03-01,2022-03-22", 39280, 0, 39280, 0  , 12781),
            tilbakekrevingPeriode("2022-03-23,2022-03-31", 17185, 0, 17185, 0  , 5591),
            tilbakekrevingPeriode("2022-04-01,2022-04-30", 51555, 0, 51555, 0  , 16087),
            tilbakekrevingPeriode("2022-05-01,2022-05-24", 41735, 0, 41735, 0  , 13268),
            tilbakekrevingPeriode("2022-05-25,2022-05-31", 12275, 0, 12275, 0  , 3902),
            tilbakekrevingPeriode("2022-06-01,2022-06-30", 54010, 0, 54010, 0  , 17170),
            tilbakekrevingPeriode("2022-07-01,2022-07-31", 51555, 0, 51555, 0  , 16087),
            tilbakekrevingPeriode("2022-08-01,2022-08-02", 4910, 0, 4910,  0,1495),
            tilbakekrevingPeriode("2022-09-07,2022-09-30", 44190, 0, 44190, 0  , 15113));
    }

    private static BigDecimal finSumAv(Collection<TilbakekrevingPeriode> perioder, Function<TilbakekrevingBeløp, BigDecimal> hva, KlasseType klasseType) {
        return perioder.stream()
                .flatMap(p -> p.getBeløp().stream())
                .filter(b -> b.getKlasseType() == klasseType)
                .map(hva)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
