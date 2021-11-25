package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsbelopDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsperiodeDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.tilbakekreving.typer.v1.PeriodeDto;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.xmlutils.DateUtil;

public class TilbakekrevingsvedtakMapper {

    private TilbakekrevingsvedtakMapper() {
        //hindrer instansiering
    }

    static TilbakekrevingsvedtakDto tilDto(Kravgrunnlag431 kravgrunnlag, List<TilbakekrevingPeriode> tilbakekrevingPerioder) {
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = TilbakekrevingsvedtakMapper.tilDto(kravgrunnlag);
        for (TilbakekrevingPeriode tilbakekrevingPeriode : tilbakekrevingPerioder) {
            tilbakekrevingsvedtak.getTilbakekrevingsperiode().add(TilbakekrevingsvedtakMapper.tilDto(tilbakekrevingPeriode));
        }
        return tilbakekrevingsvedtak;
    }

    private static TilbakekrevingsvedtakDto tilDto(Kravgrunnlag431 kravgrunnlag) {
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = new TilbakekrevingsvedtakDto();
        tilbakekrevingsvedtak.setKodeAksjon(KodeAksjon.FATTE_VEDTAK.getKode()); // fast verdi, Fatte Vedtak(8)
        tilbakekrevingsvedtak.setVedtakId(BigInteger.valueOf(kravgrunnlag.getVedtakId()));
        LocalDate vedtakFagsystemDato = kravgrunnlag.getVedtakFagSystemDato();
        if (vedtakFagsystemDato == null) {
            vedtakFagsystemDato = LocalDate.now();
        }
        tilbakekrevingsvedtak.setDatoVedtakFagsystem(DateUtil.convertToXMLGregorianCalendar(vedtakFagsystemDato));
        tilbakekrevingsvedtak.setKodeHjemmel("22-15"); // fast verdi
        tilbakekrevingsvedtak.setEnhetAnsvarlig(kravgrunnlag.getAnsvarligEnhet());
        tilbakekrevingsvedtak.setKontrollfelt(kravgrunnlag.getKontrollFelt());
        tilbakekrevingsvedtak.setSaksbehId(SubjectHandler.getSubjectHandler().getUid());
        return tilbakekrevingsvedtak;
    }

    private static TilbakekrevingsperiodeDto tilDto(TilbakekrevingPeriode tilbakekrevingPeriode) {
        TilbakekrevingsperiodeDto dto = new TilbakekrevingsperiodeDto();
        PeriodeDto periodeDto = lagPeriodeDto(tilbakekrevingPeriode.getPeriode());
        dto.setPeriode(periodeDto);
        dto.setBelopRenter(tilbakekrevingPeriode.getRenter());
        tilbakekrevingPeriode.getBeløp().forEach(
            b -> dto.getTilbakekrevingsbelop().add(lagDto(b)));
        return dto;
    }

    private static TilbakekrevingsbelopDto lagDto(TilbakekrevingBeløp b) {
        TilbakekrevingsbelopDto dto = new TilbakekrevingsbelopDto();
        dto.setKodeKlasse(b.getKlassekode());
        dto.setBelopTilbakekreves(b.getTilbakekrevBeløp());
        dto.setBelopUinnkrevd(b.getUinnkrevdBeløp());
        dto.setBelopOpprUtbet(b.getUtbetaltBeløp());
        dto.setBelopNy(b.getNyttBeløp());
        dto.setBelopSkatt(b.getSkattBeløp());
        if (KlasseType.YTEL.equals(b.getKlasseType())) {
            dto.setKodeResultat(b.getKodeResultat().getKode());
            dto.setKodeAarsak("ANNET"); // fast verdi
            dto.setKodeSkyld("IKKE_FORDELT"); // fast verdi
        }
        //FIXME setKlasseType mangler
        return dto;
    }

    private static PeriodeDto lagPeriodeDto(Periode periode) {
        PeriodeDto periodeDto = new PeriodeDto();
        periodeDto.setFom(DateUtil.convertToXMLGregorianCalendar(periode.getFom()));
        periodeDto.setTom(DateUtil.convertToXMLGregorianCalendar(periode.getTom()));
        return periodeDto;
    }

}
