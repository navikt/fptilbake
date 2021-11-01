package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder.KgBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.VilkårsvurderingTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;


/**
 * tester her er basert på regneark med eksempler for tilbakekreving
 */
@CdiDbAwareTest
public class TilbakekrevingVedtakPeriodeBeregnerScenarioerTest {

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
    public EntityManager entityManager;

    private static final PeriodeParser PP2018 = new PeriodeParser(2018);

    @Test
    public void scenario_oppør_med_flere_vurderinger() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            PP2018.periode("16/3-31/3"), List.of(
                KgBeløp.feil(11000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(11000)),
            PP2018.periode("1/4-30/4"), List.of(
                KgBeløp.feil(21000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(21000).medTilbakekrevBeløp(21000)),
            PP2018.periode("1/5-26/5"), List.of(
                KgBeløp.feil(19000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(19000).medTilbakekrevBeløp(19000))
        ), false);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            PP2018.periode("16/3-3/4"), VilkårsvurderingTestBuilder.VVurdering.godTro().setManueltBeløp(0),
            PP2018.periode("4/4-20/4"), VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom().setProsenterTilbakekreves(BigDecimal.valueOf(50)),
            PP2018.periode("21/4-26/5"), VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom().setProsenterTilbakekreves(BigDecimal.valueOf(100))
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        Assertions.assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(PP2018.periode("16/3-31/3")).medRenter(0)
                .medBeløp(TbkBeløp.feil(11000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medNyttBeløp(0).medTilbakekrevBeløp(0).medUinnkrevdBeløp(11000).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(PP2018.periode("1/4-3/4")).medRenter(0)
                .medBeløp(TbkBeløp.feil(2000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(2000).medNyttBeløp(0).medTilbakekrevBeløp(0).medUinnkrevdBeløp(2000).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(PP2018.periode("4/4-20/4")).medRenter(0)
                .medBeløp(TbkBeløp.feil(13000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(13000).medNyttBeløp(0).medTilbakekrevBeløp(6500).medUinnkrevdBeløp(6500).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(PP2018.periode("21/4-30/4")).medRenter(0)
                .medBeløp(TbkBeløp.feil(6000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(6000).medNyttBeløp(0).medTilbakekrevBeløp(6000).medUinnkrevdBeløp(0).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(PP2018.periode("1/5-26/5")).medRenter(0)
                .medBeløp(TbkBeløp.feil(19000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(19000).medNyttBeløp(0).medTilbakekrevBeløp(19000).medUinnkrevdBeløp(0).medSkattBeløp(0)));
    }


    @Test
    public void scenario_gradering_og_utsettelse() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            PP2018.periode("10/4-30/4"), List.of(
                KgBeløp.feil(7500),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(15000).medNyttBeløp(7500).medTilbakekrevBeløp(7500)),
            PP2018.periode("1/5-5/5"), List.of(
                KgBeløp.feil(2000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(4000).medNyttBeløp(2000).medTilbakekrevBeløp(2000)),
            PP2018.periode("6/5-20/5"), List.of(
                KgBeløp.feil(10000),
                KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(10000).medTilbakekrevBeløp(10000))
        ), false);

        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            PP2018.periode("10/4-20/5"), VilkårsvurderingTestBuilder.VVurdering.simpelUaktsom().setProsenterTilbakekreves(BigDecimal.valueOf(100))
        ));

        flushAndClear();

        List<TilbakekrevingPeriode> resultat = beregner.lagTilbakekrevingsPerioder(behandlingId, kravgrunnlag);
        Assertions.assertThat(resultat).containsOnly(
            TilbakekrevingPeriode.med(PP2018.periode("10/4-30/4")).medRenter(0)
                .medBeløp(TbkBeløp.feil(7500))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(15000).medNyttBeløp(7500).medTilbakekrevBeløp(7500).medUinnkrevdBeløp(0).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(PP2018.periode("1/5-5/5")).medRenter(0)
                .medBeløp(TbkBeløp.feil(2000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(4000).medNyttBeløp(2000).medTilbakekrevBeløp(2000).medUinnkrevdBeløp(0).medSkattBeløp(0)),
            TilbakekrevingPeriode.med(PP2018.periode("6/5-20/5")).medRenter(0)
                .medBeløp(TbkBeløp.feil(10000))
                .medBeløp(TbkBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(10000).medNyttBeløp(0).medTilbakekrevBeløp(10000).medUinnkrevdBeløp(0).medSkattBeløp(0))
            );
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    static class PeriodeParser {
        private int årstall;

        PeriodeParser(int årstall) {
            this.årstall = årstall;
        }

        Periode periode(String tekst) {
            if (!tekst.matches("\\d+/\\d+-\\d+/\\d+")) {
                throw new IllegalArgumentException("ugyldig format for dato: " + tekst);
            }
            String[] split = tekst.split("-");
            return Periode.of(parseDato(split[0]), parseDato(split[1]));
        }

        LocalDate parseDato(String tekst) {
            String[] split = tekst.split("/");
            return LocalDate.of(årstall, Integer.parseInt(split[1]), Integer.parseInt(split[0]));
        }
    }

}











