package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

public class KravgrunnlagTjenesteTest extends FellesTestOppsett {

    private static final String SSN = "11112222333";
    private static final String ENHET = "8020";

    private GjenopptaBehandlingTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
    private KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repoProvider, mockGjenopptaBehandlingTjeneste, behandlingskontrollTjeneste);

    private static final LocalDate fom = LocalDate.of(2016, 3, 15);
    private static final LocalDate tom = LocalDate.of(2016, 3, 18);

    @Before
    public void setup() {
        when(mockTpsTjeneste.hentAktørForFnr(new PersonIdent(SSN))).thenReturn(Optional.of(aktørId));
    }

    @Test
    public void lagreTilbakekrevingsgrunnlagFraØkonomi() {
        Kravgrunnlag431 kravgrunnlag = formKravgrunnlagDto(KravStatusKode.NYTT);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag);

        assertKravgrunnlag();
    }

    @Test
    public void lagreTilbakekrevingsgrunnlagFraØkonomi_medEndretGrunnlag() {
        Kravgrunnlag431 kravgrunnlag = formKravgrunnlagDto(KravStatusKode.ENDRET);
        formPerioder(fom, tom, kravgrunnlag);
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internBehandlingId, kravgrunnlag);

        assertKravgrunnlag();
    }

    private Kravgrunnlag431 formKravgrunnlagDto(KravStatusKode kravStatusKode) {
        return Kravgrunnlag431.builder()
            .medEksternKravgrunnlagId("123")
            .medVedtakId(10000L)
            .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
            .medKravStatusKode(kravStatusKode)
            .medGjelderVedtakId(aktørId.getId())
            .medGjelderType(GjelderType.PERSON)
            .medUtbetalesTilId(aktørId.getId())
            .medUtbetIdType(GjelderType.PERSON)
            .medFagSystemId("10000000000000000")
            .medAnsvarligEnhet(ENHET)
            .medBostedEnhet(ENHET)
            .medBehandlendeEnhet(ENHET)
            .medSaksBehId("Z991036")
            .medFeltKontroll("42354353453454")
            .build();
    }

    private void formPerioder(LocalDate fom, LocalDate tom, Kravgrunnlag431 kravgrunnlag) {
        IntStream.range(4, 6).forEach(j -> {
            KravgrunnlagPeriode432 periode = KravgrunnlagPeriode432.builder()
                .medKravgrunnlag431(kravgrunnlag)
                .medPeriode(fom.plusDays(j), tom.plusDays(j))
                .build();
            kravgrunnlag.leggTilPeriode(periode);
            formBeløper(periode);
        });
    }

    private void formBeløper(KravgrunnlagPeriode432 periode) {
        IntStream.range(0, 2).forEach(i -> {
            KravgrunnlagBelop433 beløp = KravgrunnlagBelop433.builder()
                .medKravgrunnlagPeriode432(periode)
                .medKlasseKode(KlasseKode.FPATORD)
                .medKlasseType(i == 0 ? KlasseType.FEIL : KlasseType.YTEL)
                .medNyBelop(i == 0 ? BigDecimal.valueOf(11000) : BigDecimal.ZERO)
                .medTilbakekrevesBelop(i > 0 ? BigDecimal.valueOf(11000) : BigDecimal.ZERO)
                .medResultatKode("VED")
                .build();
            periode.leggTilBeløp(beløp);
        });
    }

    private void assertKravgrunnlag() {
        Optional<KravgrunnlagAggregate> kravgrunnlagAggregate = grunnlagRepository.finnGrunnlagForBehandlingId(internBehandlingId);
        assertThat(kravgrunnlagAggregate).isNotEmpty();
        KravgrunnlagAggregate aggregate = kravgrunnlagAggregate.get();
        assertThat(aggregate.getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(aggregate.isAktiv()).isTrue();
        assertThat(aggregate.getGrunnlagØkonomi()).isNotNull();
        assertThat(aggregate.getGrunnlagØkonomi().getGjelderVedtakId()).isEqualTo(aktørId.getId());
        assertThat(aggregate.getGrunnlagØkonomi().getUtbetalesTilId()).isEqualTo(aktørId.getId());
        List<KravgrunnlagPeriode432> kravgrunnlagPerioder = new ArrayList<>(aggregate.getGrunnlagØkonomi().getPerioder());

        assertThat(kravgrunnlagPerioder).isNotEmpty();
        kravgrunnlagPerioder.sort(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        assertThat(kravgrunnlagPerioder.get(0).getFom()).isEqualTo(fom.plusDays(4));
        List<KravgrunnlagBelop433> kravgrunnlagBeloper = new ArrayList<>(kravgrunnlagPerioder.get(0).getKravgrunnlagBeloper433());
        assertThat(kravgrunnlagBeloper).isNotEmpty();
        kravgrunnlagBeloper.sort(Comparator.comparing(KravgrunnlagBelop433::getId));
        assertThat(kravgrunnlagBeloper.get(0).getKlasseType()).isEqualTo(KlasseType.FEIL);
        assertThat(kravgrunnlagBeloper.get(1).getKlasseType()).isEqualTo(KlasseType.YTEL);
        assertThat(kravgrunnlagBeloper.get(0).getNyBelop()).isEqualByComparingTo(kravgrunnlagBeloper.get(1).getTilbakekrevesBelop());
    }


}
