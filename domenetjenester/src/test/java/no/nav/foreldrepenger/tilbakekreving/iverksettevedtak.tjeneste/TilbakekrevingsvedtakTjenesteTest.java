package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.KravgrunnlagTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.VilkårsvurderingTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

@CdiDbAwareTest
public class TilbakekrevingsvedtakTjenesteTest {

    private final ScenarioSimple simple = ScenarioSimple.simple();

    @Inject
    public BehandlingRepositoryProvider behandlingRepositoryProvider;
    @Inject
    public KravgrunnlagRepository kravgrunnlagRepository;
    @Inject
    public VilkårsvurderingRepository vilkårsvurderingRepository;
    @Inject
    public TilbakekrevingsvedtakTjeneste tjeneste;
    @Inject
    public EntityManager entityManager;

    private final Periode uke = Periode.of(LocalDate.of(2019, 6, 24), LocalDate.of(2019, 6, 30));

    @Test
    public void skal_sende_regne_ut_perioder_og_konvertere_til_dto() {
        Behandling behandling = simple.lagre(behandlingRepositoryProvider);
        Long behandlingId = behandling.getId();

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagTestBuilder.medRepo(kravgrunnlagRepository).lagreKravgrunnlag(behandlingId, Map.of(
            uke, Arrays.asList(
                KravgrunnlagTestBuilder.KgBeløp.feil(9000),
                KravgrunnlagTestBuilder.KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(11000).medTilbakekrevBeløp(9000),
                KravgrunnlagTestBuilder.KgBeløp.trekk(2000))
            )
        );
        VilkårsvurderingTestBuilder.medRepo(vilkårsvurderingRepository).lagre(behandlingId, Map.of(
            uke, VilkårsvurderingTestBuilder.VVurdering.forsett()
        ));

        flushAndClear();

        TilbakekrevingsvedtakDto resultat = tjeneste.lagTilbakekrevingsvedtak(behandlingId);

        assertThat(resultat.getVedtakId().longValue()).isEqualTo(kravgrunnlag.getVedtakId());
        assertThat(resultat.getKodeAksjon()).isEqualTo("8"); //Fast verdi
        assertThat(resultat.getKodeHjemmel()).isEqualTo("22-15");
        assertThat(resultat.getKontrollfelt()).isEqualTo(kravgrunnlag.getKontrollFelt());
        assertThat(resultat.getEnhetAnsvarlig()).isEqualTo(kravgrunnlag.getAnsvarligEnhet());
        assertThat(resultat.getSaksbehId()).isEqualTo(behandling.getAnsvarligSaksbehandler());

    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
