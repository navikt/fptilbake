package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingsbelopDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingsperiodeDTO;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

public class TilbakekrevingsvedtakMapperFpwsproxy {

    private TilbakekrevingsvedtakMapperFpwsproxy() {
        //hindrer instansiering
    }

    static TilbakekrevingVedtakDTO tilDto(Kravgrunnlag431 kravgrunnlag, List<TilbakekrevingPeriode> tilbakekrevingPerioder) {
        return new TilbakekrevingVedtakDTO.Builder()
            .kodeAksjon(KodeAksjon.FATTE_VEDTAK.getKode()) // fast verdi, Fatte Vedtak(8)
            .vedtakId(kravgrunnlag.getVedtakId())
            .datoVedtakFagsystem(vedatkFagsystemDato(kravgrunnlag))
            .kodeHjemmel("22-15") // fast verdi
            .enhetAnsvarlig(kravgrunnlag.getAnsvarligEnhet())
            .kontrollfelt(kravgrunnlag.getKontrollFelt())
            .saksbehId(SubjectHandler.getSubjectHandler().getUid())
            .tilbakekrevingsperiode(tilTilbakekrevingsperiodeDTOer(tilbakekrevingPerioder))
            .build();
    }
    private static LocalDate vedatkFagsystemDato(Kravgrunnlag431 kravgrunnlag) {
        var vedtakFagsystemDato = kravgrunnlag.getVedtakFagSystemDato();
        if (vedtakFagsystemDato == null) {
            vedtakFagsystemDato = LocalDate.now();
        }
        return vedtakFagsystemDato;
    }

    private static List<TilbakekrevingsperiodeDTO> tilTilbakekrevingsperiodeDTOer(List<TilbakekrevingPeriode> tilbakekrevingPerioder) {
        return safeStream(tilbakekrevingPerioder)
            .map(TilbakekrevingsvedtakMapperFpwsproxy::tilDto)
            .toList();
    }

    private static TilbakekrevingsperiodeDTO tilDto(TilbakekrevingPeriode tilbakekrevingPeriode) {
        return new TilbakekrevingsperiodeDTO.Builder()
            .periode(lagPeriodeDto(tilbakekrevingPeriode.getPeriode()))
            .belopRenter(tilbakekrevingPeriode.getRenter())
            .tilbakekrevingsbelop(lagTilbakekrevignsbelopDTOPerioder(tilbakekrevingPeriode.getBeløp()))
            .build();
    }

    private static List<TilbakekrevingsbelopDTO> lagTilbakekrevignsbelopDTOPerioder(List<TilbakekrevingBeløp> beløp) {
        return safeStream(beløp)
            .map(TilbakekrevingsvedtakMapperFpwsproxy::lagTilbakekrevignsbelopDTOPeriode)
            .toList();
    }

    private static TilbakekrevingsbelopDTO lagTilbakekrevignsbelopDTOPeriode(TilbakekrevingBeløp b) {
        var builder = new TilbakekrevingsbelopDTO.Builder()
            .kodeKlasse(b.getKlassekode())
            .belopTilbakekreves(b.getTilbakekrevBeløp())
            .belopUinnkrevd(b.getUinnkrevdBeløp())
            .belopOpprUtbet(b.getUtbetaltBeløp())
            .belopNy(b.getNyttBeløp())
            .belopSkatt(b.getSkattBeløp());
        if (KlasseType.YTEL.equals(b.getKlasseType())) {
            builder.kodeResultat(b.getKodeResultat().getKode());
            builder.kodeAarsak("ANNET"); // fast verdi
            builder.kodeSkyld("IKKE_FORDELT"); // fast verdi
        }
        return builder.build();

    }

    private static no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Periode lagPeriodeDto(Periode periode) {
        return new no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Periode(periode.getFom(), periode.getTom());
    }

    public static <T> Stream<T> safeStream(List<T> list) {
        return Optional.ofNullable(list)
            .orElseGet(List::of)
            .stream();
    }

}
