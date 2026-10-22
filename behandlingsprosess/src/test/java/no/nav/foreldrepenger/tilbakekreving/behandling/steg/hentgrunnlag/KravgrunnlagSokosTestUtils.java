package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerResponse;

public class KravgrunnlagSokosTestUtils {

    public static HentKravgrunnlagDetaljerResponse lagKravgrunnlag(boolean medKravgrunnlagPeriode432Dto, String enhet, boolean erGyldig) {
        return new HentKravgrunnlagDetaljerResponse(0, "", lagKravgrunnlagDetaljer(medKravgrunnlagPeriode432Dto, enhet, erGyldig));
    }

    private static HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer lagKravgrunnlagDetaljer(boolean medKravgrunnlagPeriode432Dto, String enhet, boolean erGyldig) {
        return new HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer(123456789L,
            100L,
            KravStatusKode.NYTT,
            FagOmrådeKode.FORELDREPENGER,
            "139015144100",
            LocalDate.now().minusYears(1),
            207407L,
            "12345678901",
            GjelderType.PERSON,
            "12345678901",
            GjelderType.PERSON,
            "1234239042304",
            false,
            enhet,
            enhet,
            enhet,
            "kontrolll-123",
            "Z111111",
            "100000001",
            null,
            medKravgrunnlagPeriode432Dto ? lagPerioder(erGyldig) : List.of());
    }


    private static List<HentKravgrunnlagDetaljerResponse.DetaljerPeriode> lagPerioder(boolean erGyldig) {
        var kravgrunnlagPeriode1 = new HentKravgrunnlagDetaljerResponse.DetaljerPeriode(
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 22),
            erGyldig ? BigDecimal.valueOf(600) : BigDecimal.ZERO, List.of(
                lagPostering(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                lagPostering(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), KlasseType.YTEL)
            )
        );
        var kravgrunnlagPeriode2 = new HentKravgrunnlagDetaljerResponse.DetaljerPeriode(
            LocalDate.of(2022, 4, 1), LocalDate.of(2022, 4, 25),
            erGyldig ? BigDecimal.valueOf(300) : BigDecimal.ZERO,
            List.of(
                lagPostering(BigDecimal.valueOf(3000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                lagPostering(BigDecimal.ZERO, BigDecimal.valueOf(3000.00), BigDecimal.valueOf(3000.00), KlasseType.YTEL)
            ));
        var kravgrunnlagPeriode3 = new HentKravgrunnlagDetaljerResponse.DetaljerPeriode(
            LocalDate.of(2022, 10, 10), LocalDate.of(2022, 10, 27),
            erGyldig ? BigDecimal.valueOf(2100) : BigDecimal.ZERO,
            List.of(
                lagPostering(BigDecimal.valueOf(21000.00), BigDecimal.ZERO, BigDecimal.ZERO, KlasseType.FEIL),
                lagPostering(BigDecimal.ZERO, BigDecimal.valueOf(21000.00), BigDecimal.valueOf(21000.00), KlasseType.YTEL)
            ));
        return List.of(kravgrunnlagPeriode1, kravgrunnlagPeriode2, kravgrunnlagPeriode3);
    }

    private static HentKravgrunnlagDetaljerResponse.DetaljerPostering lagPostering(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp, BigDecimal opprUtbetBeløp, KlasseType klasseType) {
        return new HentKravgrunnlagDetaljerResponse.DetaljerPostering(
            "FPATAL",
            klasseType,
            opprUtbetBeløp,
            nyBeløp,
            tilbakekrevesBeløp,
            BigDecimal.ZERO,
            BigDecimal.valueOf(10.0000),
            null,
            null,
            null
        );
    }
}
