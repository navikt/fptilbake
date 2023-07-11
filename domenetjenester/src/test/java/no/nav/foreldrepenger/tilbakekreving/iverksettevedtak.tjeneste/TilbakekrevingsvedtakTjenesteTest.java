package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.VilkårsvurderingTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@CdiDbAwareTest
class TilbakekrevingsvedtakTjenesteTest {

    private final ScenarioSimple simple = ScenarioSimple.simple();

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    private KravgrunnlagRepository kravgrunnlagRepository;
    @Inject
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    @Inject
    private TilbakekrevingsvedtakTjeneste tjeneste;
    @Inject
    private EntityManager entityManager;

    private final Periode uke = Periode.of(LocalDate.of(2019, 6, 24), LocalDate.of(2019, 6, 30));

    @Test
    void skal_sende_regne_ut_perioder_og_konvertere_til_dto() {
        var behandling = simple.lagre(behandlingRepositoryProvider);
        var behandlingId = behandling.getId();

        var kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
                        uke, List.of(
                                KravgrunnlagTestBuilder.KgBeløp.feil(9000),
                                KravgrunnlagTestBuilder.KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(9000),
                                KravgrunnlagTestBuilder.KgBeløp.trekk(2000))
                ),
                false);
        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
                uke, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        var resultat = tjeneste.lagTilbakekrevingsvedtak(behandlingId);

        assertThat(resultat.vedtakId().longValue()).isEqualTo(kravgrunnlag.getVedtakId());
        assertThat(resultat.kontrollfelt()).isEqualTo(kravgrunnlag.getKontrollFelt());
        assertThat(resultat.enhetAnsvarlig()).isEqualTo(kravgrunnlag.getAnsvarligEnhet());
        assertThat(resultat.saksbehId()).isEqualTo(behandling.getAnsvarligSaksbehandler());

    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
