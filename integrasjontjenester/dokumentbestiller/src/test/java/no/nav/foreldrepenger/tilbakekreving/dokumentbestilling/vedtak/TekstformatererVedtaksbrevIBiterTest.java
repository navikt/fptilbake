package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.ØkonomiUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TekstformatererVedtaksbrevIBiterTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

    @Test
    public void skal_generere_brev_delt_i_avsnitt_og_underavsnitt() {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .skruAvMidlertidigTekst()
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
                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
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
                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                .medHendelsetype(HendelseType.ØKONOMI_FEIL)
                .medHendelseUndertype(ØkonomiUndertyper.DOBBELTUTBETALING)
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

        List<Avsnitt> resultat = TekstformatererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(data, "Du må betale tilbake foreldrepengene");
        //FIXME fullfør test
    }

    @Test
    public void skal_generere_tekst_for_faktaperiode() {
        HbVedtaksbrevFelles felles = HbVedtaksbrevFelles.builder()
            .skruAvMidlertidigTekst()
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
            .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
            .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
            .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
            .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
            .medSærligeGrunner(Arrays.asList(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP))
            .medRiktigBeløp(BigDecimal.valueOf(10000))
            .medFeilutbetaltBeløp(BigDecimal.valueOf(30001))
            .medTilbakekrevesBeløp(BigDecimal.valueOf(20002))
            .build();
        HbVedtaksbrevPeriodeOgFelles data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);

        String generertTekst = TekstformatererVedtaksbrev.lagFaktaTekst(data);
        assertThat(generertTekst).isEqualTo("Du har jobbet samtidig som at du har fått utbetalt foreldrepenger. Fordi du har fått endret hvor mye du skal jobbe og hvor mye du tar ut i foreldrepenger, er deler av beløpet du har fått utbetalt feil. Du har derfor fått 30 001 kroner for mye utbetalt.");
    }

    @Test
    public void skal_si_at_du_ikke_trenger_betale_tilbake_når_det_er_god_tro_og_beløp_ikke_er_i_behold() {
        HbVedtaksbrevFelles felles = HbVedtaksbrevFelles.builder()
            .skruAvMidlertidigTekst()
            .medErFødsel(true)
            .medAntallBarn(1)
            .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medLovhjemmelVedtak("foo")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(1000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
            .medTotaltRentebeløp(BigDecimal.ZERO)
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(4)
            .build();
        HbVedtaksbrevPeriode periode = HbVedtaksbrevPeriode.builder()
            .medPeriode(januar)
            .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
            .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
            .medVilkårResultat(VilkårResultat.GOD_TRO)
            .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
            .medRiktigBeløp(BigDecimal.ZERO)
            .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
            .medTilbakekrevesBeløp(BigDecimal.ZERO)
            .build();
        HbVedtaksbrevPeriodeOgFelles data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);

        String generertTekst = TekstformatererVedtaksbrev.lagVilkårTekst(data);
        assertThat(generertTekst).contains("_Hvordan har vi kommet fram til at du ikke må betale tilbake?");
    }

    @Test
    public void skal_ha_riktig_tekst_for_særlige_grunner_når_det_ikke_er_reduksjon_av_beløp() {
        HbVedtaksbrevFelles felles = HbVedtaksbrevFelles.builder()
            .skruAvMidlertidigTekst()
            .medErFødsel(true)
            .medAntallBarn(1)
            .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
            .medLovhjemmelVedtak("foo")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(1000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1100))
            .medTotaltRentebeløp(BigDecimal.valueOf(100))
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(4)
            .build();
        HbVedtaksbrevPeriode periode = HbVedtaksbrevPeriode.builder()
            .medPeriode(januar)
            .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
            .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
            .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
            .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
            .medRiktigBeløp(BigDecimal.ZERO)
            .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
            .medTilbakekrevesBeløp(BigDecimal.valueOf(1000))
            .medRenterBeløp(BigDecimal.valueOf(100))
            .medSærligeGrunner(Collections.singletonList(SærligGrunn.GRAD_AV_UAKTSOMHET))
            .build();

        String generertTekst = TekstformatererVedtaksbrev.lagSærligeGrunnerTekst(felles, periode);
        assertThat(generertTekst).contains("Vi har vurdert om det er grunner til å redusere beløpet. Vi har lagt vekt på at du må ha forstått at beløpet du fikk utbetalt var feil, og det er ingen bestemte grunner til å redusere beløpet. Derfor må du betale tilbake hele beløpet.");
    }

    @Test
    public void skal_ha_riktig_tekst_for_særlige_grunner_når_det_er_reduksjon_av_beløp() {
        HbVedtaksbrevFelles felles = HbVedtaksbrevFelles.builder()
            .skruAvMidlertidigTekst()
            .medErFødsel(true)
            .medAntallBarn(1)
            .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
            .medLovhjemmelVedtak("foo")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(1000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1100))
            .medTotaltRentebeløp(BigDecimal.valueOf(100))
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(4)
            .build();
        HbVedtaksbrevPeriode periode = HbVedtaksbrevPeriode.builder()
            .medPeriode(januar)
            .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
            .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
            .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
            .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
            .medRiktigBeløp(BigDecimal.ZERO)
            .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
            .medTilbakekrevesBeløp(BigDecimal.valueOf(500))
            .medRenterBeløp(BigDecimal.valueOf(0))
            .medSærligeGrunner(Collections.singletonList(SærligGrunn.GRAD_AV_UAKTSOMHET))
            .build();

        String generertTekst = TekstformatererVedtaksbrev.lagSærligeGrunnerTekst(felles, periode);
        assertThat(generertTekst)
            .contains("Vi har lagt vekt på at du må ha forstått at du fikk penger du ikke har rett til. Vi vurderer likevel at uaktsomheten din har vært så liten at vi har redusert beløpet du må betale tilbake.")
            .contains("Du må betale 500 kroner");
    }

    @Test
    public void skal_parse_tekst_til_avsnitt() {
        Avsnitt resultat = TekstformatererVedtaksbrev.parseTekst("_Hovedoverskrift i brevet\n\nBrødtekst første avsnitt\n\nBrødtekst andre avsnitt\n\n_underoverskrift\n\nBrødtekst tredje avsnitt\n\n_Avsluttende overskrift uten etterfølgende tekst\n" + VedtaksbrevFritekst.markerValgfriFritekst(null), new Avsnitt.Builder(), null).build();
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
        Avsnitt resultat = TekstformatererVedtaksbrev.parseTekst("_Hovedoverskrift i brevet\n\nBrødtekst første avsnitt\n" + VedtaksbrevFritekst.markerValgfriFritekst(null) + "\nBrødtekst andre avsnitt\n\n_underoverskrift\n\nBrødtekst tredje avsnitt\n\n_Avsluttende overskrift uten etterfølgende tekst", new Avsnitt.Builder(), null).build();
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
        Avsnitt resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n" + VedtaksbrevFritekst.markerValgfriFritekst(null) + "\nBrødtekst første avsnitt\n\n_underoverskrift 2\n\nBrødtekst andre avsnitt", avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt).hasSize(3);
        assertThat(underavsnitt.get(0).getOverskrift()).isEqualTo("underoverskrift 1");
        assertThat(underavsnitt.get(0).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(1).getOverskrift()).isNull();
        assertThat(underavsnitt.get(1).getBrødtekst()).isEqualTo("Brødtekst første avsnitt");
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isFalse();
        assertThat(underavsnitt.get(2).getOverskrift()).isEqualTo("underoverskrift 2");
        assertThat(underavsnitt.get(2).getBrødtekst()).isEqualTo("Brødtekst andre avsnitt");
        assertThat(underavsnitt.get(2).isFritekstTillatt()).isFalse();
    }

    @Test
    public void skal_parse_fritekstfelt_med_eksisterende_fritekst() {
        Avsnitt.Builder avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        Avsnitt resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n"
                + VedtaksbrevFritekst.markerValgfriFritekst("fritekst linje 1\n\nfritekst linje2")
            , avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
        //assertThat(underavsnitt).hasSize(1);
        assertThat(underavsnitt.get(0).getOverskrift()).isEqualTo("underoverskrift 1");
        assertThat(underavsnitt.get(0).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(0).getFritekst()).isEqualTo("fritekst linje 1\n\nfritekst linje2");
    }

    @Test
    public void skal_skille_mellom_påkrevet_og_valgfritt_fritekstfelt() {
        Avsnitt.Builder avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        Avsnitt resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n"
                + VedtaksbrevFritekst.markerPåkrevetFritekst(null, null)
                + "\n_underoverskrift 2\n"
                + VedtaksbrevFritekst.markerValgfriFritekst(null)
            , avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt).hasSize(2);
        assertThat(underavsnitt.get(0).getOverskrift()).isEqualTo("underoverskrift 1");
        assertThat(underavsnitt.get(0).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(0).isFritekstPåkrevet()).isTrue();
        assertThat(underavsnitt.get(0).getFritekst()).isNull();

        assertThat(underavsnitt.get(1).getOverskrift()).isEqualTo("underoverskrift 2");
        assertThat(underavsnitt.get(1).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(1).isFritekstPåkrevet()).isFalse();
        assertThat(underavsnitt.get(1).getFritekst()).isNull();
    }

    @Test
    public void skal_utlede_underavsnittstype_fra_fritekstmarkering_slik_at_det_er_mulig_å_skille_mellom_særlige_grunner_og_andre_særlige_grunner() {
        Avsnitt.Builder avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        Avsnitt resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n"
                + VedtaksbrevFritekst.markerValgfriFritekst(null, Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER)
                + "\n_underoverskrift 2\n"
                + "brødtekst " + VedtaksbrevFritekst.markerValgfriFritekst(null, Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET)
                + "\n_underoverskrift 3\n"
            , avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        List<Underavsnitt> underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt).hasSize(3);
        assertThat(underavsnitt.get(0).getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER);
        assertThat(underavsnitt.get(1).getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET);
        assertThat(underavsnitt.get(1).getBrødtekst()).isEqualTo("brødtekst ");
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(2).getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET);
        assertThat(underavsnitt.get(2).isFritekstTillatt()).isFalse();
    }
}
