package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeResultat;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.sokos.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

class TilbakekrevingsvedtakSokosMapperTest {

    @Test
    void mapper_vedtak_og_perioder_uten_fpwsproxy_dto() {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(123L);
        when(grunnlag.getHjemmelKode()).thenReturn("FVL-22");
        when(grunnlag.getBeregnesRenter()).thenReturn("J");
        when(grunnlag.getAnsvarligEnhet()).thenReturn("8020");
        when(grunnlag.getKontrollFelt()).thenReturn("kontrollfelt");
        var dato = LocalDate.of(2026, 1, 15);
        when(grunnlag.getVedtakFagSystemDato()).thenReturn(dato);
        var ytelse = new TilbakekrevingBeløp(KlasseType.YTEL, "FPATORD")
            .medUtbetBeløp(BigDecimal.valueOf(1000)).medNyttBeløp(BigDecimal.valueOf(800))
            .medTilbakekrevBeløp(BigDecimal.valueOf(200)).medUinnkrevdBeløp(BigDecimal.ZERO)
            .medSkattBeløp(BigDecimal.valueOf(50)).medKodeResultat(KodeResultat.FULL_TILBAKEKREVING);
        var trekk = new TilbakekrevingBeløp(KlasseType.TREK, "TREKK")
            .medUtbetBeløp(BigDecimal.ZERO).medNyttBeløp(BigDecimal.ZERO)
            .medTilbakekrevBeløp(BigDecimal.ZERO).medUinnkrevdBeløp(BigDecimal.ZERO).medSkattBeløp(BigDecimal.ZERO);
        var periode = TilbakekrevingPeriode.med(Periode.of(dato.minusDays(14), dato))
            .medRenter(BigDecimal.TEN).medBeløp(List.of(ytelse, trekk));

        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());

        var request = TilbakekrevingsvedtakSokosMapper.tilRequest(grunnlag, List.of(periode), null);

        assertThat(request.kodeAksjon()).isEqualTo(KodeAksjon.FATTE_VEDTAK);
        assertThat(request.vedtakId()).isEqualTo(123);
        assertThat(request.vedtaksDato()).isEqualTo(dato);
        assertThat(request.kodeHjemmel()).isEqualTo("FVL-22");
        assertThat(request.enhetAnsvarlig()).isEqualTo("8020");
        assertThat(request.kontrollfelt()).isEqualTo("kontrollfelt");
        assertThat(request.saksbehandlerId()).isNotBlank();
        assertThat(request.perioder()).containsExactly(
            new TilbakekrevingsvedtakRequest.Periode(dato.minusDays(14), dato, BigDecimal.TEN, List.of(
                new TilbakekrevingsvedtakRequest.Postering("FPATORD", BigDecimal.valueOf(1000), BigDecimal.valueOf(800),
                    BigDecimal.valueOf(200), BigDecimal.ZERO, BigDecimal.valueOf(50),
                    "FULL_TILBAKEKREV", "ANNET", "IKKE_FORDELT"),
                new TilbakekrevingsvedtakRequest.Postering("TREKK", BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", "", ""))));
        assertThat(DefaultJsonMapper.toJson(request))
            .contains("\"kodeResultat\":\"\"", "\"kodeAarsak\":\"\"", "\"kodeSkyld\":\"\"")
            .doesNotContain("\"renterBeregnes\"", "\"renterPeriodeBeregnes\"");

        KontekstHolder.fjernKontekst();
    }

    @ParameterizedTest
    @EnumSource(value = KlasseType.class, names = {"FEIL", "JUST", "SKAT", "TREK"})
    void bruker_tomme_koder_for_ikke_ytelse(KlasseType klasseType) {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(123L);
        var beløp = new TilbakekrevingBeløp(klasseType, "KL_KODE_FEIL")
            .medUtbetBeløp(BigDecimal.ZERO).medNyttBeløp(BigDecimal.ZERO)
            .medTilbakekrevBeløp(BigDecimal.ZERO).medUinnkrevdBeløp(BigDecimal.ZERO)
            .medSkattBeløp(BigDecimal.ZERO);
        var periode = TilbakekrevingPeriode.med(Periode.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
            .medBeløp(beløp);

        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());

        var request = TilbakekrevingsvedtakSokosMapper.tilRequest(grunnlag, List.of(periode), LocalDate.now());
        var postering = request.perioder().getFirst().posteringer().getFirst();

        assertThat(postering.kodeResultat()).isEmpty();
        assertThat(postering.kodeAarsak()).isEmpty();
        assertThat(postering.kodeSkyld()).isEmpty();

        assertThat(request.datoTilleggsfrist()).isEqualTo(LocalDate.now());

        KontekstHolder.fjernKontekst();
    }

    @Test
    void tjenesten_genererer_sokos_request_direkte() {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(123L);
        when(grunnlag.getBeregnesRenter()).thenReturn("N");
        var grunnlagRepository = mock(KravgrunnlagRepository.class);
        when(grunnlagRepository.finnKravgrunnlag(42L)).thenReturn(grunnlag);
        var foreldelseRepository = mock(VurdertForeldelseRepository.class);
        when(foreldelseRepository.finnVurdertForeldelse(42L)).thenReturn(Optional.empty());
        var periodeBeregner = mock(TilbakekrevingVedtakPeriodeBeregner.class);
        when(periodeBeregner.lagTilbakekrevingsPerioder(
            org.mockito.ArgumentMatchers.eq(grunnlag), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull()))
            .thenReturn(List.of());
        var tjeneste = new TilbakekrevingsvedtakTjeneste(grunnlagRepository, mock(BeregningsresultatTjeneste.class),
            periodeBeregner, foreldelseRepository);

        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());

        var request = tjeneste.lagSokosTilbakekrevingsvedtak(42L);

        assertThat(request.vedtakId()).isEqualTo(123);
        assertThat(request.kodeHjemmel()).isEqualTo("22-15");
        assertThat(request.perioder()).isEmpty();

        KontekstHolder.fjernKontekst();
    }

    @Test
    void avviser_vedtak_id_som_ikke_stottes_av_sokos() {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(1_000_000_000L);

        assertThatThrownBy(() -> TilbakekrevingsvedtakSokosMapper.tilRequest(grunnlag, List.of(), null))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("vedtakId");
    }

}
