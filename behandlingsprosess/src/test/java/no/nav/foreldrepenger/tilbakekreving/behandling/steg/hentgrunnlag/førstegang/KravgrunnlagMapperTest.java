package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;

public class KravgrunnlagMapperTest extends FellesTestOppsett {

    @Test
    public void skal_mappe_om_til_egen_domenemodell_og_konvertere_fnr_til_aktørId() {
        String xml = getInputXML("xml/kravgrunnlag_periode_FEIL.xml");
        DetaljertKravgrunnlag input = KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);

        Mockito.when(tpsAdapterMock.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Kravgrunnlag431 oversatt = mapper.mapTilDomene(input);
        assertThat(oversatt.getEksternKravgrunnlagId()).isEqualTo("123456789");
        assertThat(oversatt.getUtbetalesTilId()).isEqualTo("999999"); //aktørId
        assertThat(oversatt.getGjelderVedtakId()).isEqualTo("999999"); //aktørId

        assertThat(oversatt.getPerioder()).hasSize(1);
        KravgrunnlagPeriode432 periode = oversatt.getPerioder().get(0);
        assertThat(periode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(4500));

        List<KravgrunnlagBelop433> beløpene = periode.getKravgrunnlagBeloper433();
        assertThat(beløpene).hasSize(2);
        KravgrunnlagBelop433 beløpYTEL = beløpene.stream().filter(b -> b.getKlasseType().equals(KlasseType.FEIL)).findFirst().orElseThrow();
        assertThat(beløpYTEL.getKlasseType()).isEqualTo(KlasseType.FEIL);
        assertThat(beløpYTEL.getKlasseKode()).isEqualTo("KL_KODE_FEIL_KORTTID");
        assertThat(beløpYTEL.getNyBelop()).isEqualByComparingTo(BigDecimal.valueOf(9000));
        assertThat(beløpYTEL.getSkattProsent()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    public void skal_ha_med_klassekode_når_det_mappes_for_ytelsePeriode() {
        String xml = getInputXML("xml/kravgrunnlag_periode_YTEL.xml");
        DetaljertKravgrunnlag input = KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);

        Mockito.when(tpsAdapterMock.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Kravgrunnlag431 oversatt = mapper.mapTilDomene(input);
        assertThat(oversatt.getUtbetalesTilId()).isEqualTo("999999"); //aktørId
        assertThat(oversatt.getGjelderVedtakId()).isEqualTo("999999"); //aktørId

        assertThat(oversatt.getPerioder()).hasSize(1);
        KravgrunnlagPeriode432 periode = oversatt.getPerioder().get(0);
        assertThat(periode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(4500));

        List<KravgrunnlagBelop433> beløpene = periode.getKravgrunnlagBeloper433();
        assertThat(beløpene).hasSize(2);
        KravgrunnlagBelop433 beløpYTEL = beløpene.stream().filter(b -> b.getKlasseType().equals(KlasseType.YTEL)).findFirst().orElseThrow();
        assertThat(beløpYTEL.getKlasseType()).isEqualTo(KlasseType.YTEL);
        assertThat(beløpYTEL.getKlasseKode()).isEqualTo(KlasseKode.FPATORD.getKode());
        assertThat(beløpYTEL.getTilbakekrevesBelop()).isEqualByComparingTo(BigDecimal.valueOf(9000));
        assertThat(beløpYTEL.getNyBelop()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(beløpYTEL.getOpprUtbetBelop()).isEqualByComparingTo(BigDecimal.valueOf(9000));
        assertThat(beløpYTEL.getSkattProsent()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    public void skal_ignorere_belop_postering_med_positiv_ytel() {
        String xml = getInputXML("xml/kravgrunnlag_periode_POSITIV_YTEL.xml");
        DetaljertKravgrunnlag input = KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);

        Mockito.when(tpsAdapterMock.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Kravgrunnlag431 oversatt = mapper.mapTilDomene(input);
        assertThat(oversatt.getUtbetalesTilId()).isEqualTo("999999"); //aktørId
        assertThat(oversatt.getGjelderVedtakId()).isEqualTo("999999"); //aktørId

        assertThat(oversatt.getPerioder()).hasSize(1);
        KravgrunnlagPeriode432 periode = oversatt.getPerioder().get(0);
        assertThat(periode.getBeløpSkattMnd()).isEqualByComparingTo(BigDecimal.valueOf(2104));

        List<KravgrunnlagBelop433> beløpene = periode.getKravgrunnlagBeloper433();
        assertThat(beløpene).hasSize(2);
        assertThat(beløpene.stream().filter(b -> b.getKlasseType().equals(KlasseType.FEIL)).findAny()).isNotEmpty();
        KravgrunnlagBelop433 beløpYTEL = beløpene.stream().filter(b -> b.getKlasseType().equals(KlasseType.YTEL)).findFirst().orElseThrow();
        assertThat(beløpYTEL.getKlasseType()).isEqualTo(KlasseType.YTEL);
        assertThat(beløpYTEL.getKlasseKode()).isEqualTo(KlasseKode.FPATORD.getKode());
        assertThat(beløpYTEL.getTilbakekrevesBelop()).isEqualByComparingTo(BigDecimal.valueOf(1794));
        assertThat(beløpYTEL.getNyBelop()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(beløpYTEL.getOpprUtbetBelop()).isEqualByComparingTo(BigDecimal.valueOf(3270));
        assertThat(beløpYTEL.getSkattProsent()).isEqualByComparingTo(BigDecimal.valueOf(43.9799));
    }

}
