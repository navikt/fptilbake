package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.FagOmrådeKode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.GjelderType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KlasseType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravStatusKode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagBelop433Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagPeriode432Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Periode;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;

public class HentKravgrunnlagMapperProxyTest {

    private static final String ENHET = "8020";
    private static final AktørId AKTØR_ID = new AktørId(999999L);
    private final PersoninfoAdapter personinfoAdapterMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(personinfoAdapterMock);
    private final HentKravgrunnlagMapperProxy mapper = new HentKravgrunnlagMapperProxy(tpsAdapterWrapper);

    @Test
    void skalMappeUtenAvvikFraDtoTilDomeneModell() {
        Mockito.when(personinfoAdapterMock.hentAktørForFnr(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(AKTØR_ID));

        var kravgrunnlag431Dto = lagKravgrunnlag(true, true);
        var kravgrunnlagDomenemodell = mapper.mapTilDomene(kravgrunnlag431Dto);

        verifiserAtMappingIkkeMisterNoeData(kravgrunnlag431Dto, kravgrunnlagDomenemodell);
    }

    @Test
    void skal_ignorere_belop_postering_med_positiv_ytel() {
        Mockito.when(personinfoAdapterMock.hentAktørForFnr(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(AKTØR_ID));

        List<KravgrunnlagBelop433Dto> kravgrunnlagBeloper433Liste = new ArrayList<>();
        var feilPostering = hentBeløp(BigDecimal.valueOf(1794), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL);
        var ytelPostering = hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(1794), BigDecimal.valueOf(3270), KlasseType.YTEL);
        var positivYtelPostering = hentBeløp(BigDecimal.valueOf(3930), BigDecimal.ZERO, BigDecimal.valueOf(2454), KlasseType.YTEL);
        kravgrunnlagBeloper433Liste.add(feilPostering);
        kravgrunnlagBeloper433Liste.add(ytelPostering);
        kravgrunnlagBeloper433Liste.add(positivYtelPostering);

        var kravgrunnlag431Dto = lagKravgrunnlag(false, true);

        var kravgrunnlagPeriode = new KravgrunnlagPeriode432Dto.Builder()
            .periode(new Periode(LocalDate.now().minusYears(1), LocalDate.now().minusYears(1).plusMonths(1)))
            .beløpSkattMnd(BigDecimal.valueOf(2104))
            .kravgrunnlagBeloper433(kravgrunnlagBeloper433Liste)
            .build();
        kravgrunnlag431Dto.perioder().add(kravgrunnlagPeriode);


        // Act
        var kravgrunnlag431 = mapper.mapTilDomene(kravgrunnlag431Dto);

        // Assert
        // Fjerner positiv postering fra original kravgrunnlag431Dto og sjekker at denne da er identisk til mappet kravgrunnlag431
        kravgrunnlag431Dto.perioder().forEach(kravgrunnlagPeriode432Dto -> kravgrunnlagPeriode432Dto.kravgrunnlagBeloper433().remove(positivYtelPostering));
        verifiserAtMappingIkkeMisterNoeData(kravgrunnlag431Dto, kravgrunnlag431);

    }


    private static void verifiserAtMappingIkkeMisterNoeData(Kravgrunnlag431Dto kravgrunnlag431Dto, Kravgrunnlag431 kravgrunnlagDomenemodell) {
        assertThat(kravgrunnlag431Dto.eksternKravgrunnlagId()).isEqualTo(kravgrunnlagDomenemodell.getEksternKravgrunnlagId());
        assertThat(kravgrunnlag431Dto.vedtakId()).isEqualTo(kravgrunnlagDomenemodell.getVedtakId());
        assertThat(kravgrunnlag431Dto.kravStatusKode().name()).isEqualTo(kravgrunnlagDomenemodell.getKravStatusKode().getKode());
        assertThat(kravgrunnlag431Dto.fagOmrådeKode().name()).isEqualTo(kravgrunnlagDomenemodell.getFagOmrådeKode().getKode());
        assertThat(kravgrunnlag431Dto.fagSystemId()).isNotNull();
        assertThat(kravgrunnlagDomenemodell.getSaksnummer().getVerdi()).isNotNull();
        assertThat(kravgrunnlag431Dto.vedtakFagSystemDato()).isEqualTo(kravgrunnlagDomenemodell.getVedtakFagSystemDato());
        assertThat(kravgrunnlag431Dto.omgjortVedtakId()).isEqualTo(kravgrunnlagDomenemodell.getOmgjortVedtakId());
        assertThat(AKTØR_ID.getId())
            .isEqualTo(kravgrunnlagDomenemodell.getGjelderVedtakId())
            .isEqualTo(kravgrunnlagDomenemodell.getUtbetalesTilId());
        assertThat(kravgrunnlag431Dto.gjelderType().name())
            .isEqualTo(kravgrunnlagDomenemodell.getGjelderType().name())
            .isEqualTo(GjelderType.PERSON.name());
        assertThat(kravgrunnlag431Dto.hjemmelKode()).isEqualTo(kravgrunnlagDomenemodell.getHjemmelKode());
        assertThat(kravgrunnlag431Dto.beregnesRenter()).isEqualTo(kravgrunnlagDomenemodell.getBeregnesRenter());
        assertThat(kravgrunnlag431Dto.ansvarligEnhet())
            .isEqualTo(kravgrunnlagDomenemodell.getAnsvarligEnhet())
            .isEqualTo(ENHET);
        assertThat(kravgrunnlag431Dto.bostedEnhet()).isEqualTo(kravgrunnlagDomenemodell.getBostedEnhet());
        assertThat(kravgrunnlag431Dto.behandlendeEnhet()).isEqualTo(kravgrunnlagDomenemodell.getBehandlendeEnhet());
        assertThat(kravgrunnlag431Dto.kontrollFelt()).isEqualTo(kravgrunnlagDomenemodell.getKontrollFelt());
        assertThat(kravgrunnlag431Dto.saksBehId()).isEqualTo(kravgrunnlagDomenemodell.getSaksBehId());
        assertThat(kravgrunnlag431Dto.referanse()).isEqualTo(kravgrunnlagDomenemodell.getReferanse().getVerdi());

        // Verifiser kravgrunnlagPerioder432
        var kravgrunnlagPerioder432DtoListe = kravgrunnlag431Dto.perioder();
        var kravgrunnlagPeriode432Liste = kravgrunnlagDomenemodell.getPerioder();
        assertThat(kravgrunnlagPerioder432DtoListe)
            .hasSameSizeAs(kravgrunnlagPeriode432Liste)
            .hasSizeGreaterThan(0);
        for (int i = 0; i < kravgrunnlagPeriode432Liste.size(); i++) {
            var kravgrunnlagPeriode432Dto = kravgrunnlagPerioder432DtoListe.get(i);
            var kravgrunnlagPeriode432 = kravgrunnlagPeriode432Liste.get(i);
            assertThat(kravgrunnlagPeriode432Dto.periode().fom()).isEqualTo(kravgrunnlagPeriode432.getPeriode().getFom());
            assertThat(kravgrunnlagPeriode432Dto.periode().tom()).isEqualTo(kravgrunnlagPeriode432.getPeriode().getTom());
            assertThat(kravgrunnlagPeriode432Dto.beløpSkattMnd()).isEqualTo(kravgrunnlagPeriode432.getBeløpSkattMnd()).isNotNull();

            // Verifiser KravgrunnlagBelop433
            var kravgrunnlagBelop433DtoListe = kravgrunnlagPeriode432Dto.kravgrunnlagBeloper433();
            var kravgrunnlagPeriode433Liste = kravgrunnlagPeriode432.getKravgrunnlagBeloper433();
            assertThat(kravgrunnlagBelop433DtoListe)
                .hasSameSizeAs(kravgrunnlagPeriode433Liste)
                .hasSizeGreaterThan(0);
            for (int j = 0; j < kravgrunnlagPeriode433Liste.size(); j++) {
                var kravgrunnlagBelop433Dto = kravgrunnlagBelop433DtoListe.get(j);
                var kravgrunnlagBelop433 = kravgrunnlagPeriode433Liste.get(j);
                assertThat(kravgrunnlagBelop433Dto.klasseKode()).isEqualTo(kravgrunnlagBelop433.getKlasseKode());
                assertThat(kravgrunnlagBelop433Dto.klasseType().name()).isEqualTo(kravgrunnlagBelop433.getKlasseType().getKode());
                assertThat(kravgrunnlagBelop433Dto.opprUtbetBelop())
                    .isEqualTo(kravgrunnlagBelop433.getOpprUtbetBelop())
                    .isNotNull();
                assertThat(kravgrunnlagBelop433Dto.nyBelop())
                    .isEqualTo(kravgrunnlagBelop433.getNyBelop())
                    .isNotNull();
                assertThat(kravgrunnlagBelop433Dto.tilbakekrevesBelop())
                    .isEqualTo(kravgrunnlagBelop433.getTilbakekrevesBelop())
                    .isNotNull();
                assertThat(kravgrunnlagBelop433Dto.uinnkrevdBelop())
                    .isEqualTo(kravgrunnlagBelop433.getUinnkrevdBelop())
                    .isNotNull();
                assertThat(kravgrunnlagBelop433Dto.skattProsent())
                    .isEqualTo(kravgrunnlagBelop433.getSkattProsent())
                    .isNotNull();
                assertThat(kravgrunnlagBelop433Dto.resultatKode()).isEqualTo(kravgrunnlagBelop433.getResultatKode());
                assertThat(kravgrunnlagBelop433Dto.årsakKode()).isEqualTo(kravgrunnlagBelop433.getÅrsakKode());
                assertThat(kravgrunnlagBelop433Dto.skyldKode()).isEqualTo(kravgrunnlagBelop433.getSkyldKode());
            }
        }
    }

    public static Kravgrunnlag431Dto lagKravgrunnlag(boolean medKravgrunnlagPeriode432Dto, boolean erGyldig) {
        return new Kravgrunnlag431Dto.Builder()
            .vedtakId(100L)
            .eksternKravgrunnlagId("123456789")
            .vedtakFagSystemDato(LocalDate.now().minusYears(1))
            .ansvarligEnhet(ENHET)
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
            .behandlendeEnhet(ENHET)
            .bostedEnhet(ENHET)
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

    private static KravgrunnlagBelop433Dto hentBeløp(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp, BigDecimal opprUtbetBeløp, KlasseType klasseType) {
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