package no.nav.foreldrepenger.tilbakekreving.sokos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

public record HentKravgrunnlagDetaljerResponse(Integer status,
                                               String melding,
                                               KravgrunnlagDetaljer kravgrunnlag) {

    public record KravgrunnlagDetaljer(Long kravgrunnlagId,
                                       Long vedtakId,
                                       KravStatusKode kodeStatusKrav,
                                       FagOmrådeKode kodeFagomraade,
                                       String fagsystemId,
                                       LocalDate datoVedtakFagsystem,
                                       Long vedtakIdOmgjort,
                                       String gjelderId,
                                       GjelderType typeGjelder,
                                       String utbetalesTilId,
                                       GjelderType typeUtbetalesTilId,
                                       String kodeHjemmel,
                                       Boolean renterBeregnes,
                                       String enhetAnsvarlig,
                                       String enhetBosted,
                                       String enhetBehandl,
                                       String kontrollfelt,
                                       String saksbehandlerId,
                                       String referanse,
                                       LocalDate datoTilleggsfrist,
                                       List<DetaljerPeriode> perioder) {
    }

    public record DetaljerPeriode(LocalDate periodeFom, LocalDate periodeTom,
                                  BigDecimal belopSkattMnd,
                                  List<DetaljerPostering> posteringer) {
    }

    public record DetaljerPostering(String kodeKlasse,
                                    KlasseType typeKlasse,
                                    BigDecimal belopOpprinneligUtbetalt,
                                    BigDecimal belopNy,
                                    BigDecimal belopTilbakekreves,
                                    BigDecimal belopUinnkrevd,
                                    BigDecimal skattProsent,
                                    String kodeResultat,
                                    String kodeAarsak,
                                    String kodeSkyld) {
    }
}
