package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.sokos.TilbakekrevingsvedtakRequest;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

final class TilbakekrevingsvedtakSokosMapper {
    private static final String KODE_HJEMMEL = "22-15";
    private static final String KODE_SKYLD = "IKKE_FORDELT";
    private static final String KODE_ÅRSAK = "ANNET";

    private TilbakekrevingsvedtakSokosMapper() {
    }

    static TilbakekrevingsvedtakRequest tilRequest(Kravgrunnlag431 kravgrunnlag, List<TilbakekrevingPeriode> perioder) {
        var vedtakId = Math.toIntExact(Objects.requireNonNull(kravgrunnlag.getVedtakId(), "vedtakId"));
        if (vedtakId > 999_999_999) {
            throw new IllegalArgumentException("vedtakId er større enn tillatt i Sokos-kontrakten: " + vedtakId);
        }
        return new TilbakekrevingsvedtakRequest(KodeAksjon.FATTE_VEDTAK, vedtakId, TilbakekrevingsvedtakMapper.vedatkFagsystemDato(kravgrunnlag),
            Optional.ofNullable(kravgrunnlag.getHjemmelKode()).orElse(KODE_HJEMMEL),
            kravgrunnlag.getAnsvarligEnhet(), kravgrunnlag.getKontrollFelt(), KontekstHolder.getKontekst().getUid(), null,
            perioder.stream().map(TilbakekrevingsvedtakSokosMapper::tilPeriode).toList());
    }

    private static TilbakekrevingsvedtakRequest.Periode tilPeriode(TilbakekrevingPeriode periode) {
        return new TilbakekrevingsvedtakRequest.Periode(periode.getPeriode().getFom(), periode.getPeriode().getTom(),
            periode.getRenter(), periode.getBeløp().stream().map(TilbakekrevingsvedtakSokosMapper::tilPostering).toList());
    }

    private static TilbakekrevingsvedtakRequest.Postering tilPostering(TilbakekrevingBeløp beløp) {
        var ytelse = KlasseType.YTEL.equals(beløp.getKlasseType());
        var kodeResultat = beløp.getKodeResultat() != null ? beløp.getKodeResultat().getKode() : null;
        return new TilbakekrevingsvedtakRequest.Postering(beløp.getKlassekode(), beløp.getUtbetaltBeløp(), beløp.getNyttBeløp(),
            beløp.getTilbakekrevBeløp(), beløp.getUinnkrevdBeløp(), beløp.getSkattBeløp(),
            ytelse ? kodeResultat : "", ytelse ? KODE_ÅRSAK : "", ytelse ? KODE_SKYLD : "");
    }

}
