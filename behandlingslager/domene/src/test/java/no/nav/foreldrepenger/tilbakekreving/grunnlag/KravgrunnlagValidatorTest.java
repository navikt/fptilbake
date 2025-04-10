package no.nav.foreldrepenger.tilbakekreving.grunnlag;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

class KravgrunnlagValidatorTest {

    private Kravgrunnlag431 kravgrunnlag = lagKravgrunnlag(Henvisning.fraEksternBehandlingId(1000000L));
    private BigDecimal maxSkattJanuar = BigDecimal.valueOf(100);

    @Test
    void skal_godta_kravgrunnlag_som_er_OK() {
        Periode periode = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 15));
        KravgrunnlagPeriode432 kgPeriode = leggTilKravgrunnlagPeriode(kravgrunnlag, periode, maxSkattJanuar);
        BigDecimal skatteprosent = BigDecimal.valueOf(10);
        leggTilFeilutbetaling(kgPeriode, 1000, skatteprosent);

        KravgrunnlagValidator.validerGrunnlag(kravgrunnlag);
    }

    @Test
    void skal_gi_feilmelding_ved_manglende_referanse_felt() {
        kravgrunnlag = lagKravgrunnlag(null);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-879716:Ugyldig kravgrunnlag for kravgrunnlagId 12341. Mangler referanse.");
    }

    @Test
    void skal_gi_feilmelding_ved_overlappende_perioder() {
        Periode periode1 = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
        Periode periode2 = Periode.of(LocalDate.of(2020, 1, 6), LocalDate.of(2020, 1, 31));
        KravgrunnlagPeriode432 kgPeriode1 = leggTilKravgrunnlagPeriode(kravgrunnlag, periode1, maxSkattJanuar);
        KravgrunnlagPeriode432 kgPeriode2 = leggTilKravgrunnlagPeriode(kravgrunnlag, periode2, maxSkattJanuar);
        leggTilFeilutbetaling(kgPeriode1, 1000);
        leggTilFeilutbetaling(kgPeriode2, 1000);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-936521:Ugyldig kravgrunnlag for kravgrunnlagId 12341. Overlappende perioder 01.01.2020-10.01.2020 og 06.01.2020-31.01.2020.");
    }

    @Test
    void skal_gi_feilmelding_når_utregnet_skatt_kan_bli_høyere_enn_maxSkatt_for_måneden() {
        BigDecimal maxSkattJanuar = BigDecimal.valueOf(299);
        BigDecimal skatteprosent = BigDecimal.valueOf(30);
        Periode periode1 = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 15));
        Periode periode2 = Periode.of(LocalDate.of(2020, 1, 16), LocalDate.of(2020, 1, 31));
        KravgrunnlagPeriode432 kgPeriode1 = leggTilKravgrunnlagPeriode(kravgrunnlag, periode1, maxSkattJanuar);
        KravgrunnlagPeriode432 kgPeriode2 = leggTilKravgrunnlagPeriode(kravgrunnlag, periode2, maxSkattJanuar);
        leggTilFeilutbetaling(kgPeriode1, 500, skatteprosent);
        leggTilFeilutbetaling(kgPeriode2, 500, skatteprosent);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-930235:Ugyldig kravgrunnlag. For måned 2020-01 er maks skatt 299, men maks tilbakekreving ganget med skattesats blir 300");
    }

    @Test
    void skal_gi_feilmelding_når_feilutbetaling_i_ytel_er_ulik_nytt_beløp_i_feilposteringen() {
        Periode periode1 = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 15));
        KravgrunnlagPeriode432 kgPeriode = leggTilKravgrunnlagPeriode(kravgrunnlag, periode1, maxSkattJanuar);

        BigDecimal skattOrd = BigDecimal.valueOf(50);
        BigDecimal skattNæringsdrivende = BigDecimal.valueOf(0);
        leggTilYtel(kgPeriode, KlasseKode.FPATORD, 100, skattOrd);
        leggTilYtel(kgPeriode, KlasseKode.FPADSND_OP, 100, skattNæringsdrivende);
        leggTilFeil(kgPeriode, 100 + 100 + 1, BigDecimal.ZERO);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-361605:Ugyldig kravgrunnlag for kravgrunnlagId 12341. For periode 01.01.2020-15.01.2020 er sum tilkakekreving fra YTEL 200, mens belopNytt i FEIL er 201. Det er forventet at disse er like.");
    }

    @Test
    void skal_gi_feilmelding_når_perioden_ikke_har_postering_av_klasseType_FEIL() {
        Periode periode1 = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 15));
        KravgrunnlagPeriode432 kgPeriode = leggTilKravgrunnlagPeriode(kravgrunnlag, periode1, maxSkattJanuar);

        BigDecimal skattOrd = BigDecimal.valueOf(50);
        BigDecimal skattNæringsdrivende = BigDecimal.valueOf(0);
        leggTilYtel(kgPeriode, KlasseKode.FPATORD, 0, skattOrd);
        leggTilYtel(kgPeriode, KlasseKode.FPADSND_OP, 0, skattNæringsdrivende);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-727260:Ugyldig kravgrunnlag for kravgrunnlagId 12341. Perioden 01.01.2020-15.01.2020 mangler postering med klasseType=FEIL.");
    }

    @Test
    void skal_gi_feilmelding_når_perioden_ikke_har_postering_av_klasseType_YTEL() {
        Periode periode1 = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 15));
        KravgrunnlagPeriode432 kgPeriode = leggTilKravgrunnlagPeriode(kravgrunnlag, periode1, maxSkattJanuar);

        BigDecimal skattOrd = BigDecimal.valueOf(50);
        leggTilFeil(kgPeriode, 1000, skattOrd);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-727261:Ugyldig kravgrunnlag for kravgrunnlagId 12341. Perioden 01.01.2020-15.01.2020 mangler postering med klasseType=YTEL.");
    }

    @Test
    void skal_gi_feilmelding_når_perioden_har_FEIL_postering_med_negativt_beløp() {
        Periode periode1 = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 15));
        KravgrunnlagPeriode432 kgPeriode = leggTilKravgrunnlagPeriode(kravgrunnlag, periode1, maxSkattJanuar);

        BigDecimal skattOrd = BigDecimal.valueOf(50);
        leggTilFeil(kgPeriode, -1000, skattOrd);
        leggTilYtel(kgPeriode, KlasseKode.FPATORD, -1000, skattOrd);

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-930247:Ugyldig kravgrunnlag for kravgrunnlagId 12341. Perioden 01.01.2020-15.01.2020 har FEIL postering med negativ beløp");
    }

    @Test
    void skal_gi_feilmelding_når_tilbakekreves_beløp_er_høyere_enn_differanse_mellom_nytt_og_gammelt_beløp() {
        Periode periode = Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
        KravgrunnlagPeriode432 kgPeriode = leggTilKravgrunnlagPeriode(kravgrunnlag, periode, BigDecimal.ZERO);
        leggTilFeil(kgPeriode, 1000, BigDecimal.ZERO);
        kgPeriode.leggTilBeløp(KravgrunnlagBelop433.builder()
                .medKravgrunnlagPeriode432(kgPeriode)
                .medKlasseType(KlasseType.YTEL)
                .medKlasseKode(KlasseKode.FPATORD)
                .medTilbakekrevesBelop(BigDecimal.valueOf(1000))
                .medOpprUtbetBelop(BigDecimal.valueOf(1000))
                .medNyBelop(BigDecimal.valueOf(200))
                .medSkattProsent(BigDecimal.valueOf(0))
                .build());

        var e = assertThrows(KravgrunnlagValidator.UgyldigKravgrunnlagException.class, () -> KravgrunnlagValidator.validerGrunnlag(kravgrunnlag));
        assertThat(e.getMessage()).isEqualTo("FPT-615761:Ugyldig kravgrunnlag for kravgrunnlagId 12341. For perioden 01.01.2020-10.01.2020 finnes YTEL-postering med tilbakekrevesBeløp 1000 som er større enn differanse mellom nyttBeløp 200 og opprinneligBeløp 1000");
    }

    private void leggTilFeilutbetaling(KravgrunnlagPeriode432 kgPeriode, int feilutbetaltBeløp) {
        leggTilFeilutbetaling(kgPeriode, feilutbetaltBeløp, BigDecimal.ZERO);
    }

    private void leggTilFeilutbetaling(KravgrunnlagPeriode432 kgPeriode, int feilutbetaltBeløp, BigDecimal skatt) {
        leggTilYtel(kgPeriode, KlasseKode.FPATORD, feilutbetaltBeløp, skatt);
        leggTilFeil(kgPeriode, feilutbetaltBeløp, skatt);
    }

    private void leggTilFeil(KravgrunnlagPeriode432 kgPeriode, int feilutbetaltBeløp, BigDecimal skatt) {
        kgPeriode.leggTilBeløp(KravgrunnlagBelop433.builder()
                .medKravgrunnlagPeriode432(kgPeriode)
                .medKlasseType(KlasseType.FEIL)
                .medKlasseKode("foo")
                .medNyBelop(BigDecimal.valueOf(feilutbetaltBeløp))
                .medSkattProsent(skatt)
                .build());
    }

    private void leggTilYtel(KravgrunnlagPeriode432 kgPeriode, KlasseKode klasseKode, int feilutbetaltBeløp, BigDecimal skatt) {
        kgPeriode.leggTilBeløp(KravgrunnlagBelop433.builder()
                .medKravgrunnlagPeriode432(kgPeriode)
                .medKlasseType(KlasseType.YTEL)
                .medKlasseKode(klasseKode)
                .medTilbakekrevesBelop(BigDecimal.valueOf(feilutbetaltBeløp))
                .medOpprUtbetBelop(BigDecimal.valueOf(feilutbetaltBeløp))
                .medSkattProsent(skatt)
                .build());
    }

    private KravgrunnlagPeriode432 leggTilKravgrunnlagPeriode(Kravgrunnlag431 kravgrunnlag, Periode periode, BigDecimal maxSkattMnd) {
        KravgrunnlagPeriode432 kgPeriode = new KravgrunnlagPeriode432.Builder().medKravgrunnlag431(kravgrunnlag)
                .medPeriode(periode)
                .medBeløpSkattMnd(maxSkattMnd)
                .build();
        kravgrunnlag.leggTilPeriode(kgPeriode);
        return kgPeriode;
    }

    private static Kravgrunnlag431 lagKravgrunnlag(Henvisning henvisning) {
        return new Kravgrunnlag431.Builder()
                .medEksternKravgrunnlagId("12341")
                .medFagSystemId("GSAKNR-12312")
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medKravStatusKode(KravStatusKode.NYTT)
                .medVedtakId(1412L)
                .medAnsvarligEnhet("8020")
                .medBehandlendeEnhet("8020")
                .medBostedEnhet("8020")
                .medFeltKontroll(LocalDateTime.now().format(Kravgrunnlag431.KONTROLL_FELT_FORMAT))
                .medGjelderType(GjelderType.PERSON)
                .medGjelderVedtakId("???")
                .medSaksBehId("Z111111")
                .medReferanse(henvisning)
                .medUtbetalesTilId("99999999999")
                .medUtbetIdType(GjelderType.PERSON)
                .build();
    }

}
