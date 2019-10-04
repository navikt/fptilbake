package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto;
import no.nav.tilbakekreving.typer.v1.JaNeiDto;
import no.nav.tilbakekreving.typer.v1.PeriodeDto;
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto;
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class HentKravgrunnlagMapperTest extends FellesTestOppsett {

    private static final String ENHET = "8020";

    private HentKravgrunnlagMapper mapper = new HentKravgrunnlagMapper(tpsAdapterWrapper);

    @Test
    public void skal_mapTilDomene_fraHentgrunnlagrespons(){
        Mockito.when(tpsAdapterMock.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Kravgrunnlag431 kravgrunnlag431 = mapper.mapTilDomene(hentGrunnlag());
        assertThat(kravgrunnlag431).isNotNull();
        assertThat(kravgrunnlag431.getAnsvarligEnhet()).isEqualTo(ENHET);
        assertThat(kravgrunnlag431.getFagOmrådeKode()).isEqualByComparingTo(FagOmrådeKode.FORELDREPENGER);
        assertThat(kravgrunnlag431.getEksternKravgrunnlagId()).isEqualTo("152806");
        assertThat(kravgrunnlag431.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.BEHANDLET);
        assertThat(kravgrunnlag431.getGjelderType()).isEqualByComparingTo(GjelderType.PERSON);

        assertThat(kravgrunnlag431.getPerioder()).isNotEmpty();
        assertThat(kravgrunnlag431.getPerioder().size()).isEqualTo(3);
        List<KravgrunnlagPeriode432> perioder = kravgrunnlag431.getPerioder();

        KravgrunnlagPeriode432 førstePeriode = perioder.get(0);
        førstePeriode.getPeriode().equals(Periode.of(LocalDate.of(2016, 3, 16),LocalDate.of(2016, 3, 31)));
        assertThat(førstePeriode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(600.00));
        assertThat(førstePeriode.getKravgrunnlagBeloper433()).isNotEmpty();
        assertThat(førstePeriode.getKravgrunnlagBeloper433().size()).isEqualTo(2);

        KravgrunnlagPeriode432 andrePeriode = perioder.get(1);
        andrePeriode.getPeriode().equals(Periode.of(LocalDate.of(2016, 4, 1),LocalDate.of(2016, 4, 30)));
        assertThat(andrePeriode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(andrePeriode.getKravgrunnlagBeloper433()).isNotEmpty();
        assertThat(andrePeriode.getKravgrunnlagBeloper433().size()).isEqualTo(2);

        KravgrunnlagPeriode432 tredjePeriode = perioder.get(2);
        tredjePeriode.getPeriode().equals(Periode.of(LocalDate.of(2016, 5, 1),LocalDate.of(2016, 5, 26)));
        assertThat(tredjePeriode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(2100.00));
        assertThat(tredjePeriode.getKravgrunnlagBeloper433()).isNotEmpty();
        assertThat(tredjePeriode.getKravgrunnlagBeloper433().size()).isEqualTo(2);
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
        detaljertKravgrunnlag.setSaksbehId("Z9901136");
        detaljertKravgrunnlag.setUtbetalesTilId("12345678901");
        detaljertKravgrunnlag.setEnhetBehandl(ENHET);
        detaljertKravgrunnlag.setEnhetBosted(ENHET);
        detaljertKravgrunnlag.setKodeStatusKrav("BEHA");
        detaljertKravgrunnlag.setTypeGjelderId(TypeGjelderDto.PERSON);
        detaljertKravgrunnlag.setTypeUtbetId(TypeGjelderDto.PERSON);
        detaljertKravgrunnlag.setVedtakGjelderId("12345678901");
        detaljertKravgrunnlag.setVedtakIdOmgjort(BigInteger.valueOf(207407));
        detaljertKravgrunnlag.getTilbakekrevingsPeriode().addAll(hentPerioder());

        return detaljertKravgrunnlag;
    }

    private List<DetaljertKravgrunnlagPeriodeDto> hentPerioder() {
        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode1 = new DetaljertKravgrunnlagPeriodeDto();
        PeriodeDto periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 3, 16)));
        periode.setTom(konvertDato(LocalDate.of(2016, 3, 31)));
        kravgrunnlagPeriode1.setPeriode(periode);
        kravgrunnlagPeriode1.setBelopSkattMnd(BigDecimal.valueOf(600.00));
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(6000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode1.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(6000.00), BigDecimal.valueOf(6000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode2 = new DetaljertKravgrunnlagPeriodeDto();
        periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 4, 01)));
        periode.setTom(konvertDato(LocalDate.of(2016, 4, 30)));
        kravgrunnlagPeriode2.setPeriode(periode);
        kravgrunnlagPeriode2.setBelopSkattMnd(BigDecimal.valueOf(300.00));
        kravgrunnlagPeriode2.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(3000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode2.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(3000.00), BigDecimal.valueOf(3000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        DetaljertKravgrunnlagPeriodeDto kravgrunnlagPeriode3 = new DetaljertKravgrunnlagPeriodeDto();
        periode = new PeriodeDto();
        periode.setFom(konvertDato(LocalDate.of(2016, 5, 1)));
        periode.setTom(konvertDato(LocalDate.of(2016, 5, 26)));
        kravgrunnlagPeriode3.setPeriode(periode);
        kravgrunnlagPeriode3.setBelopSkattMnd(BigDecimal.valueOf(2100.00));
        kravgrunnlagPeriode3.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.valueOf(21000.00), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, TypeKlasseDto.FEIL));
        kravgrunnlagPeriode3.getTilbakekrevingsBelop().add(hentBeløp(BigDecimal.ZERO, BigDecimal.valueOf(21000.00), BigDecimal.valueOf(21000.00), BigDecimal.ZERO, TypeKlasseDto.YTEL));

        return Lists.newArrayList(kravgrunnlagPeriode1, kravgrunnlagPeriode2, kravgrunnlagPeriode3);
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
        detaljertKravgrunnlagBelop.setSkattProsent(BigDecimal.valueOf(10.0000));

        return detaljertKravgrunnlagBelop;
    }

    private XMLGregorianCalendar konvertDato(LocalDate localDate) {
        return DateUtil.convertToXMLGregorianCalendar(localDate);
    }
}
