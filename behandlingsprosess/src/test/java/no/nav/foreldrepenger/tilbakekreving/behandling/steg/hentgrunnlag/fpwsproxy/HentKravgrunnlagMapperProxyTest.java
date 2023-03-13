package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.KravgrunnlagTestUtils.hentBeløp;
import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.KravgrunnlagTestUtils.lagKravgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.GjelderType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KlasseType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagBelop433Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.KravgrunnlagPeriode432Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Periode;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.PersonOrganisasjonWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;

class HentKravgrunnlagMapperProxyTest {

    private static final String ENHET = "8020";
    private static final AktørId AKTØR_ID = new AktørId(999999L);
    private final PersoninfoAdapter personinfoAdapterMock = mock(PersoninfoAdapter.class);
    private final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(personinfoAdapterMock);
    private final HentKravgrunnlagMapperProxy mapper = new HentKravgrunnlagMapperProxy(tpsAdapterWrapper);

    @Test
    void skalMappeUtenAvvikFraDtoTilDomeneModell() {
        when(personinfoAdapterMock.hentAktørForFnr(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(AKTØR_ID));

        var kravgrunnlag431Dto = lagKravgrunnlag(true, ENHET, true);
        var kravgrunnlagDomenemodell = mapper.mapTilDomene(kravgrunnlag431Dto);

        verifiserAtMappingIkkeMisterNoeData(kravgrunnlag431Dto, kravgrunnlagDomenemodell);
    }

    @Test
    void skal_ignorere_belop_postering_med_positiv_ytel() {
        when(personinfoAdapterMock.hentAktørForFnr(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(AKTØR_ID));

        List<KravgrunnlagBelop433Dto> kravgrunnlagBeloper433Liste = new ArrayList<>();
        var feilPostering = hentBeløp(BigDecimal.valueOf(1794), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL);
        var ytelPostering = hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(1794), BigDecimal.valueOf(3270), KlasseType.YTEL);
        var positivYtelPostering = hentBeløp(BigDecimal.valueOf(3930), BigDecimal.ZERO, BigDecimal.valueOf(2454), KlasseType.YTEL);
        kravgrunnlagBeloper433Liste.add(feilPostering);
        kravgrunnlagBeloper433Liste.add(ytelPostering);
        kravgrunnlagBeloper433Liste.add(positivYtelPostering);

        var kravgrunnlag431Dto = lagKravgrunnlag(false, ENHET, true);

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
}
