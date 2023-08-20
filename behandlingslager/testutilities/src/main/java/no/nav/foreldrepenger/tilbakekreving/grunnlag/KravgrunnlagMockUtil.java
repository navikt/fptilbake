package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

// Metoder i KravgrunnlagMockUtil har skrevet for å teste grunnlag. Så dette metoder må være brukte bare for test.
public class KravgrunnlagMockUtil {

    KravgrunnlagMockUtil() {
        // for CDI
    }

    public static Kravgrunnlag431 lagMockObject(List<KravgrunnlagMock> kravgrunnlagMocker) {
        return lagMockObject(kravgrunnlagMocker, LocalDateTime.now());
    }

    public static Kravgrunnlag431 lagMockObject(List<KravgrunnlagMock> kravgrunnlagMocker, LocalDateTime kontroll) {
        var kravgrunnlag431 = Kravgrunnlag431.builder()
                .medVedtakId(100000l)
                .medEksternKravgrunnlagId("12123")
                .medKravStatusKode(KravStatusKode.NYTT)
                .medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medFagSystemId("100000000000")
                .medGjelderVedtakId("100000000")
                .medGjelderType(GjelderType.ORGANISASJON)
                .medUtbetalesTilId("100000000")
                .medUtbetIdType(GjelderType.ORGANISASJON)
                .medAnsvarligEnhet("8020")
                .medBehandlendeEnhet("8020")
                .medBostedEnhet("8020")
                .medFeltKontroll(kontroll.format(Kravgrunnlag431.KONTROLL_FELT_FORMAT))
                .medSaksBehId("Z991035")
                .medReferanse(Henvisning.fraEksternBehandlingId(100000000L))
                .build();
        var perioder = kravgrunnlagMocker.stream().collect(Collectors.groupingBy(m -> m.getPeriode().getFom()));
        for (var mock : perioder.values()) {
            kravgrunnlag431.leggTilPeriode(lagMockPeriode(mock, kravgrunnlag431));
        }
        return kravgrunnlag431;
    }

    public static KravgrunnlagPeriode432 lagMockPeriode(List<KravgrunnlagMock> mock, Kravgrunnlag431 kravgrunnlag431) {
        var periode = mock.stream().map(KravgrunnlagMock::getPeriode).findFirst().orElseThrow();
        var kravgrunnlagPeriode432 = KravgrunnlagPeriode432.builder()
                .medPeriode(periode)
                .medKravgrunnlag431(kravgrunnlag431).build();
        mock.forEach(m -> kravgrunnlagPeriode432.leggTilBeløp(lagBeløp(m, kravgrunnlagPeriode432)));
        return kravgrunnlagPeriode432;
    }

    public static KravgrunnlagBelop433 lagBeløp(KravgrunnlagMock mock, KravgrunnlagPeriode432 kravgrunnlagPeriode432) {
        return KravgrunnlagBelop433.builder().medKravgrunnlagPeriode432(kravgrunnlagPeriode432)
                .medKlasseKode(mock.getKlasseKode() != null ? mock.getKlasseKode() : KlasseKode.FPADATORD)
                .medKlasseType(mock.getKlasseType())
                .medOpprUtbetBelop(mock.getTilbakekrevesBelop())
                .medNyBelop(mock.getNyBelop())
                .medTilbakekrevesBelop(mock.getTilbakekrevesBelop()).build();
    }
}
