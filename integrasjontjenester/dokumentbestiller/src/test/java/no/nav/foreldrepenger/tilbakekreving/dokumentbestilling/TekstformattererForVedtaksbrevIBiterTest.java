package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TekstformattererForVedtaksbrevIBiterTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

    @Test
    public void skal_generere_brev_delt_i_avsnitt_og_underavsnitt() {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .medErFødsel(true)
            .medAntallBarn(2)
            .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(33001))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
            .medTotaltRentebeløp(BigDecimal.ZERO)
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(4)
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                .medSærligeGrunner(Arrays.asList(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP))
                .medRiktigBeløp(BigDecimal.valueOf(10000))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(30001))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(20002))
                .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(februar)
                .medHendelsetype(HendelseType.FP_ANNET_HENDELSE_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.OKONOMI_DOBBELUTBETALING)
                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                .medSærligeGrunner(Arrays.asList(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP))
                .medRiktigBeløp(BigDecimal.valueOf(3000))
                .medUtbetaltBeløp(BigDecimal.valueOf(6000))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(3000))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(3000))
                .build()
        );
        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        List<Avsnitt> resultat = TekstformattererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(data, "Du må betale tilbake foreldrepengene");
        System.out.println(resultat);
    }

    @Test
    public void skal_generere_tekst_for_faktaperiode() {
        HbVedtaksbrevFelles felles = HbVedtaksbrevFelles.builder()
            .medErFødsel(true)
            .medAntallBarn(2)
            .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medLovhjemmelVedtak("foo")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(33001))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
            .medTotaltRentebeløp(BigDecimal.ZERO)
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(4)
            .build();
        HbVedtaksbrevPeriode periode = HbVedtaksbrevPeriode.builder()
            .medPeriode(januar)
            .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
            .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
            .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
            .medSærligeGrunner(Arrays.asList(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP))
            .medRiktigBeløp(BigDecimal.valueOf(10000))
            .medFeilutbetaltBeløp(BigDecimal.valueOf(30001))
            .medTilbakekrevesBeløp(BigDecimal.valueOf(20002))
            .medFritekstVilkår("Du er heldig som slapp å betale alt!")
            .build();
        HbVedtaksbrevPeriodeOgFelles data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);

        String generertTekst = TekstformattererVedtaksbrev.lagFaktaTekst(data);
        assertThat(generertTekst).isEqualTo("Du har jobbet samtidig som at du har fått utbetalt foreldrepenger. Fordi du har fått endret hvor mye du skal jobbe og hvor mye du tar ut i foreldrepenger, er deler av beløpet du har fått utbetalt feil. Du har derfor fått 30001 kroner for mye utbetalt.");
    }

    @Test
    public void skal_parse_tekst_til_avsnitt() {
        Avsnitt resultat = TekstformattererVedtaksbrev.parseTekst("_Hovedoverskrift i brevet\n\nBrødtekst første avsnitt\n\nBrødtekst andre avsnitt\n\n_underoverskrift\n\nBrødtekst tredje avsnitt\n\n_Avsluttende overskrift uten etterfølgende tekst\n\\\\//", new Avsnitt.Builder(), null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift i brevet");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt).hasSize(4);
        assertThat(underavsnitt.get(0).getOverskrift()).isNull();
        assertThat(underavsnitt.get(0).getBrødtekst()).isEqualTo("Brødtekst første avsnitt");
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(1).getOverskrift()).isNull();
        assertThat(underavsnitt.get(1).getBrødtekst()).isEqualTo("Brødtekst andre avsnitt");
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(2).getOverskrift()).isEqualTo("underoverskrift");
        assertThat(underavsnitt.get(2).getBrødtekst()).isEqualTo("Brødtekst tredje avsnitt");
        assertThat(underavsnitt.get(2).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(3).getOverskrift()).isEqualTo("Avsluttende overskrift uten etterfølgende tekst");
        assertThat(underavsnitt.get(3).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(3).isFritekstTillatt()).isTrue();
    }

    @Test
    public void skal_plassere_fritekstfelt_etter_første_avsnitt_når_det_er_valgt() {
        Avsnitt resultat = TekstformattererVedtaksbrev.parseTekst("_Hovedoverskrift i brevet\n\nBrødtekst første avsnitt\n\\\\//\n\nBrødtekst andre avsnitt\n\n_underoverskrift\n\nBrødtekst tredje avsnitt\n\n_Avsluttende overskrift uten etterfølgende tekst", new Avsnitt.Builder(), null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift i brevet");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
            assertThat(underavsnitt).hasSize(4);
        assertThat(underavsnitt.get(0).getOverskrift()).isNull();
        assertThat(underavsnitt.get(0).getBrødtekst()).isEqualTo("Brødtekst første avsnitt");
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(1).getOverskrift()).isNull();
        assertThat(underavsnitt.get(1).getBrødtekst()).isEqualTo("Brødtekst andre avsnitt");
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(2).getOverskrift()).isEqualTo("underoverskrift");
        assertThat(underavsnitt.get(2).getBrødtekst()).isEqualTo("Brødtekst tredje avsnitt");
        assertThat(underavsnitt.get(2).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(3).getOverskrift()).isEqualTo("Avsluttende overskrift uten etterfølgende tekst");
        assertThat(underavsnitt.get(3).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(3).isFritekstTillatt()).isFalse();
    }

    @Test
    public void skal_plassere_fritekstfelt_etter_overskriften_når_det_er_valgt() {
        Avsnitt.Builder avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        Avsnitt resultat = TekstformattererVedtaksbrev.parseTekst("_underoverskrift 1\n\\\\//\n\nBrødtekst første avsnitt\n\n_underoverskrift 2\n\nBrødtekst andre avsnitt", avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt).hasSize(3);
        assertThat(underavsnitt.get(0).getOverskrift()).isEqualTo("underoverskrift 1");
        assertThat(underavsnitt.get(0).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(1).getOverskrift()).isNull();
        assertThat(underavsnitt.get(1).getBrødtekst()).isEqualTo("Brødtekst første avsnitt");
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(2).getOverskrift()).isEqualTo("underoverskrift 2" );
        assertThat(underavsnitt.get(2).getBrødtekst()).isEqualTo("Brødtekst andre avsnitt");
        assertThat(underavsnitt.get(2).isFritekstTillatt()).isFalse();
    }

}
