package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.mock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.xmlutils.DateUtil;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.tilbakekreving.typer.v1.JaNeiDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.tilbakekreving.typer.v1.PeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

class ØkonomiConsumerMockImpl implements ØkonomiConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ØkonomiConsumerMockImpl.class);
    private static final String ENHET = "8020";

    @Override
    public TilbakekrevingsvedtakResponse iverksettTilbakekrevingsvedtak(Long behandlingId, TilbakekrevingsvedtakRequest request) {
        logger.info("Tilbakekrevingsvedtak sendt til oppdragsystemet for behandlingId={}", behandlingId);
        return lagMockRespons();
    }

    @Override
    public DetaljertKravgrunnlagDto hentKravgrunnlag(Long behandlingId, HentKravgrunnlagDetaljDto kravgrunnlagDetalj) {
        logger.info("Hent detaljertKravgrunnlag for behandlingId={}", behandlingId);
        return hentGrunnlag();
    }

    @Override
    public MmelDto anullereKravgrunnlag(Long behandlingId, AnnullerKravgrunnlagDto annullerKravgrunnlag) {
        logger.info("AnnulereKravgrunnlag sendt til oppdragsystemet for behandlingId={}", behandlingId);
        return lagMockKvittering();
    }

    private TilbakekrevingsvedtakResponse lagMockRespons() {
        TilbakekrevingsvedtakResponse response = new TilbakekrevingsvedtakResponse();
        MmelDto mmelDto = lagMockKvittering();
        response.setMmel(mmelDto);
        return response;
    }

    private MmelDto lagMockKvittering() {
        MmelDto mmelDto = new MmelDto();
        mmelDto.setSystemId("460-BIDR");
        mmelDto.setAlvorlighetsgrad("00");
        mmelDto.setBeskrMelding("OK");
        return mmelDto;
    }

    private DetaljertKravgrunnlagDto hentGrunnlag() {
        DetaljertKravgrunnlagDto detaljertKravgrunnlag = new DetaljertKravgrunnlagDto();
        detaljertKravgrunnlag.setVedtakId(BigInteger.valueOf(207406));
        detaljertKravgrunnlag.setKravgrunnlagId(BigInteger.valueOf(152806));
        detaljertKravgrunnlag.setDatoVedtakFagsystem(konvertDato(LocalDate.of(2019, 3, 14)));
        detaljertKravgrunnlag.setEnhetAnsvarlig(ENHET);
        detaljertKravgrunnlag.setFagsystemId("10000000000000000");
        detaljertKravgrunnlag.setKodeFagomraade("FP");
        detaljertKravgrunnlag.setKodeHjemmel("1234239042304");
        detaljertKravgrunnlag.setKontrollfelt("42354353453454");
        detaljertKravgrunnlag.setReferanse("1");
        detaljertKravgrunnlag.setRenterBeregnes(JaNeiDto.N);
        detaljertKravgrunnlag.setSaksbehId(KontekstHolder.getKontekst().getUid());
        detaljertKravgrunnlag.setUtbetalesTilId("07125125470");
        detaljertKravgrunnlag.setEnhetBehandl(ENHET);
        detaljertKravgrunnlag.setEnhetBosted(ENHET);
        detaljertKravgrunnlag.setKodeStatusKrav("BEHA");
        detaljertKravgrunnlag.setTypeGjelderId(TypeGjelderDto.PERSON);
        detaljertKravgrunnlag.setTypeUtbetId(TypeGjelderDto.PERSON);
        detaljertKravgrunnlag.setVedtakGjelderId("07125125470");
        detaljertKravgrunnlag.setVedtakIdOmgjort(BigInteger.valueOf(207407));
        detaljertKravgrunnlag.getTilbakekrevingsPeriode().addAll(hentPerioder());

        return detaljertKravgrunnlag;
    }

    private List<DetaljertKravgrunnlagPeriodeDto> hentPerioder() {
        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode1 = new DetaljertKravgrunnlagPeriodeDto();
        PeriodeDto periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 3, 16)));
        periode.setTom(konvertDato(LocalDate.of(2016, 3, 25)));
        kravgrunnlagPeriode1.setPeriode(periode);
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode2 = new DetaljertKravgrunnlagPeriodeDto();
        periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 3, 26)));
        periode.setTom(konvertDato(LocalDate.of(2016, 3, 31)));
        kravgrunnlagPeriode2.setPeriode(periode);
        kravgrunnlagPeriode2.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(3000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode2.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(3000.00), BigDecimal.valueOf(3000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode3 = new DetaljertKravgrunnlagPeriodeDto();
        periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 4, 1)));
        periode.setTom(konvertDato(LocalDate.of(2016, 4, 30)));
        kravgrunnlagPeriode3.setPeriode(periode);
        kravgrunnlagPeriode3.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(21000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode3.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(21000.00), BigDecimal.valueOf(21000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));


        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode4 = new DetaljertKravgrunnlagPeriodeDto();
        periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 5, 11)));
        periode.setTom(konvertDato(LocalDate.of(2016, 5, 26)));
        kravgrunnlagPeriode4.setPeriode(periode);
        kravgrunnlagPeriode4.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(9000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode4.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(9000.00), BigDecimal.valueOf(9000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        return Lists.newArrayList(kravgrunnlagPeriode1, kravgrunnlagPeriode2, kravgrunnlagPeriode3, kravgrunnlagPeriode4);
    }

    private DetaljertKravgrunnlagBelopDto hentBeløp(BigDecimal nyBeløp, BigDecimal tilbakekrevesBeløp,
                                                    BigDecimal opprUtbetBeløp, BigDecimal uInnkrevdBeløp, TypeKlasseDto typeKlasse) {
        DetaljertKravgrunnlagBelopDto detaljertKravgrunnlagBelop = new DetaljertKravgrunnlagBelopDto();
        detaljertKravgrunnlagBelop.setTypeKlasse(typeKlasse);
        detaljertKravgrunnlagBelop.setBelopNy(nyBeløp);
        detaljertKravgrunnlagBelop.setBelopOpprUtbet(opprUtbetBeløp);
        detaljertKravgrunnlagBelop.setBelopTilbakekreves(tilbakekrevesBeløp);
        detaljertKravgrunnlagBelop.setBelopUinnkrevd(uInnkrevdBeløp);
        detaljertKravgrunnlagBelop.setKodeKlasse("FPATAL");

        return detaljertKravgrunnlagBelop;
    }

    private XMLGregorianCalendar konvertDato(LocalDate localDate) {
        return DateUtil.convertToXMLGregorianCalendar(localDate);
    }
}
