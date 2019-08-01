package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;

public class KravgrunnlagMapperTest {
    private TpsAdapter tpsTjeneste = Mockito.mock(TpsAdapter.class);
    private KravgrunnlagMapper mapper = new KravgrunnlagMapper(tpsTjeneste);

    @Test
    public void skal_mappe_om_til_egen_domenemodell_og_konvertere_fnr_til_aktørId() {
        String xml = getInputXML("xml/kravgrunnlag_periode_FEIL.xml");
        DetaljertKravgrunnlag input = KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);

        Mockito.when(tpsTjeneste.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Kravgrunnlag431 oversatt = mapper.mapTilDomene(input);
        assertThat(oversatt.getEksternKravgrunnlagId()).isEqualTo("123456789");
        assertThat(oversatt.getUtbetalesTilId()).isEqualTo("999999"); //aktørId
        assertThat(oversatt.getGjelderVedtakId()).isEqualTo("999999"); //aktørId

        assertThat(oversatt.getPerioder()).hasSize(1);
        KravgrunnlagPeriode432 periode = oversatt.getPerioder().get(0);

        List<KravgrunnlagBelop433> beløpene = periode.getKravgrunnlagBeloper433();
        assertThat(beløpene).hasSize(1);
        KravgrunnlagBelop433 beløp = beløpene.get(0);
        assertThat(beløp.getKlasseType()).isEqualTo(KlasseType.FEIL);
        assertThat(beløp.getKlasseKode()).isEqualTo("foobar");
        assertThat(beløp.getNyBelop()).isEqualByComparingTo(BigDecimal.valueOf(9000));
    }

    @Test
    public void skal_ha_med_klassekode_når_det_mappes_for_ytelsePeriode() {
        String xml = getInputXML("xml/kravgrunnlag_periode_YTEL.xml");
        DetaljertKravgrunnlag input = KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);

        Mockito.when(tpsTjeneste.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));

        Kravgrunnlag431 oversatt = mapper.mapTilDomene(input);
        assertThat(oversatt.getUtbetalesTilId()).isEqualTo("999999"); //aktørId
        assertThat(oversatt.getGjelderVedtakId()).isEqualTo("999999"); //aktørId

        assertThat(oversatt.getPerioder()).hasSize(1);
        KravgrunnlagPeriode432 periode = oversatt.getPerioder().get(0);

        List<KravgrunnlagBelop433> beløpene = periode.getKravgrunnlagBeloper433();
        assertThat(beløpene).hasSize(1);
        KravgrunnlagBelop433 beløp = beløpene.get(0);
        assertThat(beløp.getKlasseType()).isEqualTo(KlasseType.YTEL);
        assertThat(beløp.getKlasseKode()).isEqualTo(KlasseKode.FPATORD.getKode());
        assertThat(beløp.getNyBelop()).isEqualByComparingTo(BigDecimal.valueOf(9000));
    }

    private String getInputXML(String filename) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }
}
