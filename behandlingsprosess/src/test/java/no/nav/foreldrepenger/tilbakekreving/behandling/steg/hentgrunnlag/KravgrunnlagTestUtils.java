package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.FagOmrådeKode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.GjelderType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KlasseType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravStatusKode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagBelop433Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagPeriode432Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Periode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KravgrunnlagTestUtils {

    public static Kravgrunnlag431Dto lagKravgrunnlag(boolean medKravgrunnlagPeriode432Dto, String enhet, boolean erGyldig) {
        return new Kravgrunnlag431Dto.Builder()
            .vedtakId(100L)
            .eksternKravgrunnlagId("123456789")
            .vedtakFagSystemDato(LocalDate.now().minusYears(1))
            .ansvarligEnhet(enhet)
            .fagSystemId("139015144100")
            .fagOmrådeKode(FagOmrådeKode.FP)
            .hjemmelKode("1234239042304")
            .kontrollFelt("kontrolll-123")
            .referanse("100000001")
            .beregnesRenter("N")
            .saksBehId("Z111111")
            .utbetalesTilId("12345678901")
            .utbetGjelderType(GjelderType.PERSON)
            .gjelderType(GjelderType.PERSON)
            .behandlendeEnhet(enhet)
            .bostedEnhet(enhet)
            .kravStatusKode(KravStatusKode.NY)
            .gjelderVedtakId("12345678901")
            .omgjortVedtakId(207407L)
            .perioder(medKravgrunnlagPeriode432Dto ? lagPerioder(erGyldig) : new ArrayList<>())
            .build();
    }

    private static List<KravgrunnlagPeriode432Dto> lagPerioder(boolean erGyldig) {
        List<KravgrunnlagPeriode432Dto> KravgrunnlagPeriode432DtoListe = new ArrayList<>();
        var kravgrunnlagPeriode1 = new KravgrunnlagPeriode432Dto.Builder()
            .periode(new Periode(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 22)))
            .kravgrunnlagBeloper433(List.of(
                hentBeløp(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), KlasseType.YTEL)
            ));
        var kravgrunnlagPeriode2 = new KravgrunnlagPeriode432Dto.Builder()
            .periode(new Periode(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 4, 25)))
            .kravgrunnlagBeloper433(List.of(
                hentBeløp(BigDecimal.valueOf(3000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(3000.00), BigDecimal.valueOf(3000.00), KlasseType.YTEL)
            ));
        var kravgrunnlagPeriode3 = new KravgrunnlagPeriode432Dto.Builder()
            .periode(new Periode(LocalDate.of(2022, 10, 10), LocalDate.of(2022, 10, 27)))
            .kravgrunnlagBeloper433(List.of(
                hentBeløp(BigDecimal.valueOf(21000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(21000.00), BigDecimal.valueOf(21000.00), KlasseType.YTEL)
            ));
        if (erGyldig) {
            kravgrunnlagPeriode1.beløpSkattMnd(BigDecimal.valueOf(600.00));
            kravgrunnlagPeriode2.beløpSkattMnd(BigDecimal.valueOf(300.00));
            kravgrunnlagPeriode3.beløpSkattMnd(BigDecimal.valueOf(2100.00));
        }

        KravgrunnlagPeriode432DtoListe.add(kravgrunnlagPeriode1.build());
        KravgrunnlagPeriode432DtoListe.add(kravgrunnlagPeriode2.build());
        KravgrunnlagPeriode432DtoListe.add(kravgrunnlagPeriode3.build());
        return KravgrunnlagPeriode432DtoListe;
    }

    public static KravgrunnlagBelop433Dto hentBeløp(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp, BigDecimal opprUtbetBeløp, KlasseType klasseType) {
        return new KravgrunnlagBelop433Dto.Builder()
            .klasseType(klasseType)
            .nyBelop(nyBeløp)
            .opprUtbetBelop(opprUtbetBeløp)
            .tilbakekrevesBelop(tilbakekrevesBeløp)
            .uinnkrevdBelop(BigDecimal.ZERO)
            .klasseKode("FPATAL")
            .skattProsent(BigDecimal.valueOf(10.0000))
            .build();
    }
}
