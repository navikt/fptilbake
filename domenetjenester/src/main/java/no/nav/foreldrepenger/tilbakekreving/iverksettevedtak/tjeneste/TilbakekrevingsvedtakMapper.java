package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import static no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.KodeSkyld.IKKE_FORDELT;
import static no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.KodeÅrsak.ANNET;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.KodeResultat;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingsbelopDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingsperiodeDTO;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

public class TilbakekrevingsvedtakMapper {

    private TilbakekrevingsvedtakMapper() {
        //hindrer instansiering
    }

    static TilbakekrevingVedtakDTO tilDto(Kravgrunnlag431 kravgrunnlag, List<TilbakekrevingPeriode> tilbakekrevingPerioder) {
        return new TilbakekrevingVedtakDTO.Builder()
            .vedtakId(kravgrunnlag.getVedtakId())
            .datoVedtakFagsystem(vedatkFagsystemDato(kravgrunnlag))
            .enhetAnsvarlig(kravgrunnlag.getAnsvarligEnhet())
            .kontrollfelt(kravgrunnlag.getKontrollFelt())
            .saksbehId(KontekstHolder.getKontekst().getUid())
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
            .map(TilbakekrevingsvedtakMapper::tilDto)
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
            .map(TilbakekrevingsvedtakMapper::lagTilbakekrevignsbelopDTOPeriode)
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
            builder.kodeResultat(tilKodeResultat(b.getKodeResultat()));
            builder.kodeAarsak(ANNET);
            builder.kodeSkyld(IKKE_FORDELT);
        }
        return builder.build();

    }

    private static KodeResultat tilKodeResultat(no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeResultat kodeResultat) {
        if (kodeResultat == null) return null;
        return switch (kodeResultat) {
            case FORELDET -> KodeResultat.FORELDET;
            case FEILREGISTRERT -> KodeResultat.FEILREGISTRERT;
            case INGEN_TILBAKEKREVING -> KodeResultat.INGEN_TILBAKEKREV;
            case DELVIS_TILBAKEKREVING -> KodeResultat.DELVIS_TILBAKEKREV;
            case FULL_TILBAKEKREVING -> KodeResultat.FULL_TILBAKEKREV;

        };
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
