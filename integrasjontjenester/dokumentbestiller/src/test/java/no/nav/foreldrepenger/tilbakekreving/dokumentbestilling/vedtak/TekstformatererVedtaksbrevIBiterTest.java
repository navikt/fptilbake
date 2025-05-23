package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbBehandling;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultatTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class TekstformatererVedtaksbrevIBiterTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

    @Test
    void skal_generere_brev_delt_i_avsnitt_og_underavsnitt() {
        var vedtaksbrevData = lagTestBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(2)
                        .build())
                .medBehandling(HbBehandling.builder()
                        .medErRevurdering(false)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
                        .medTotaltRentebeløp(BigDecimal.ZERO)
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(23002))
                        .build())
                .medLovhjemmelVedtak("Folketrygdloven § 22-15")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(33001))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
                .build();
        var perioder = List.of(
                HbVedtaksbrevPeriode.builder()
                        .medPeriode(januar)
                        .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(30001)))
                        .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                        .medVurderinger(HbVurderinger.builder()
                                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                                .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                                .medSærligeGrunner(List.of(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                                .build())
                        .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(20002))
                        .build(),
                HbVedtaksbrevPeriode.builder()
                        .medPeriode(februar)
                        .medVurderinger(HbVurderinger.builder()
                                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                                .medSærligeGrunner(List.of(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP), null, null)
                                .build())
                        .medFakta(HendelseType.ØKONOMI_FEIL, HendelseUnderType.DOBBELTUTBETALING)
                        .medKravgrunnlag(HbKravgrunnlag.builder()
                                .medFeilutbetaltBeløp(BigDecimal.valueOf(3000))
                                .medRiktigBeløp(BigDecimal.valueOf(3000))
                                .medUtbetaltBeløp(BigDecimal.valueOf(6000))
                                .build())
                        .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(3000))
                        .build()
        );
        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var resultat = TekstformatererVedtaksbrev.lagVedtaksbrevDeltIAvsnitt(data, "Du må betale tilbake foreldrepengene");
        //FIXME fullfør test
    }

    @Test
    void skal_generere_tekst_for_faktaperiode() {
        var felles = lagTestBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(2)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(23002))
                        .medTotaltRentebeløp(BigDecimal.ZERO)
                        .build())
                .medLovhjemmelVedtak("foo")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(33001))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .build();
        var periode = HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(30001)))
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                        .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                        .medSærligeGrunner(List.of(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                        .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(20002))
                .build();
        var data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);

        var generertTekst = TekstformatererVedtaksbrev.lagFaktaTekst(data);
        assertThat(generertTekst).isEqualTo("Du har jobbet samtidig som at du har fått utbetalt foreldrepenger. Fordi du har fått endret hvor mye du skal jobbe og hvor mye du tar ut i foreldrepenger, er deler av beløpet du har fått utbetalt feil. Du har derfor fått 30 001 kroner for mye utbetalt.");
    }

    private HbVedtaksbrevFelles.Builder lagTestBuilder() {
        return HbVedtaksbrevFelles.builder()
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(4)
                        .build())
                .medSøker(HbPerson.builder()
                        .medNavn("Søker Søkersen")
                        .medDødsdato(LocalDate.of(2018, 3, 1))
                        .medErGift(true)
                        .build());
    }

    @Test
    void skal_si_at_du_ikke_trenger_betale_tilbake_når_det_er_god_tro_og_beløp_ikke_er_i_behold() {
        var felles = lagTestBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                        .medTotaltRentebeløp(BigDecimal.ZERO)
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                        .build())
                .medLovhjemmelVedtak("foo")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(1000))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .build();
        var periode = HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medVilkårResultat(VilkårResultat.GOD_TRO)
                        .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                        .medBeløpIBehold(BigDecimal.ZERO)
                        .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build();
        var data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);

        var generertTekst = TekstformatererVedtaksbrev.lagVilkårTekst(data);
        assertThat(generertTekst).contains("_Hvordan har vi kommet fram til at du ikke må betale tilbake?");
    }

    @Test
    void skal_ha_riktig_tekst_for_særlige_grunner_når_det_ikke_er_reduksjon_av_beløp() {
        var felles = lagTestBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1100))
                        .medTotaltRentebeløp(BigDecimal.valueOf(100))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(1100))
                        .build())
                .medLovhjemmelVedtak("foo")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(1000))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .build();
        var periode = HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                        .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
                        .medSærligeGrunner(Collections.singletonList(SærligGrunn.GRAD_AV_UAKTSOMHET), null, null)
                        .build())
                .medResultat(HbResultat.builder()
                        .medTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                        .medRenterBeløp(BigDecimal.valueOf(100))
                        .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(1000))
                        .build())
                .build();

        var generertTekst = TekstformatererVedtaksbrev.lagSærligeGrunnerTekst(felles, periode);
        assertThat(generertTekst).contains("Vi har vurdert om det er grunner til å redusere beløpet. Vi har lagt vekt på at du ikke har gitt oss alle nødvendige opplysninger tidsnok til at vi kunne unngå feilutbetalingen. Derfor må du betale tilbake hele beløpet.");
    }

    @Test
    void skal_ha_riktig_tekst_for_særlige_grunner_når_det_er_reduksjon_av_beløp() {
        var felles = lagTestBuilder()
                .medSpråkkode(Språkkode.NN)
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1100))
                        .medTotaltRentebeløp(BigDecimal.valueOf(100))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(1100))
                        .build())
                .medLovhjemmelVedtak("foo")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(1000))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .build();
        var periode = HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                        .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                        .medSærligeGrunner(Collections.singletonList(SærligGrunn.GRAD_AV_UAKTSOMHET), null, null)
                        .build())
                .medResultat(HbResultat.builder()
                        .medTilbakekrevesBeløp(BigDecimal.valueOf(500))
                        .medRenterBeløp(BigDecimal.valueOf(0))
                        .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(500))
                        .build())
                .build();

        var generertTekst = TekstformatererVedtaksbrev.lagSærligeGrunnerTekst(felles, periode);
        assertThat(generertTekst)
                .contains("Vi har lagt vekt på at du ikkje har gitt oss alle nødvendige opplysningar tidsnok til at vi kunne unngå feilutbetalinga. Vi vurderer likevel at aktløysa di har vore så lita at vi har redusert beløpet du må betale tilbake.")
                .contains("Du må betale 500 kroner");
    }

    @Test
    void skal_parse_tekst_til_avsnitt() {
        var resultat = TekstformatererVedtaksbrev.parseTekst("_Hovedoverskrift i brevet\n\nBrødtekst første avsnitt\n\nBrødtekst andre avsnitt\n\n_underoverskrift\n\nBrødtekst tredje avsnitt\n\n_Avsluttende overskrift uten etterfølgende tekst\n" + VedtaksbrevFritekst.markerValgfriFritekst(null), new Avsnitt.Builder(), null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift i brevet");
        var underavsnitt = resultat.getUnderavsnittsliste();
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
    void skal_plassere_fritekstfelt_etter_første_avsnitt_når_det_er_valgt() {
        var resultat = TekstformatererVedtaksbrev.parseTekst("_Hovedoverskrift i brevet\n\nBrødtekst første avsnitt\n" + VedtaksbrevFritekst.markerValgfriFritekst(null) + "\nBrødtekst andre avsnitt\n\n_underoverskrift\n\nBrødtekst tredje avsnitt\n\n_Avsluttende overskrift uten etterfølgende tekst", new Avsnitt.Builder(), null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift i brevet");
        var underavsnitt = resultat.getUnderavsnittsliste();
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
    void skal_plassere_fritekstfelt_etter_overskriften_når_det_er_valgt() {
        var avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        var resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n" + VedtaksbrevFritekst.markerValgfriFritekst(null) + "\nBrødtekst første avsnitt\n\n_underoverskrift 2\n\nBrødtekst andre avsnitt", avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        var underavsnitt = resultat.getUnderavsnittsliste();
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
    void skal_parse_fritekstfelt_med_eksisterende_fritekst() {
        var avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        var resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n"
                        + VedtaksbrevFritekst.markerValgfriFritekst("fritekst linje 1\n\nfritekst linje2")
                , avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        var underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt.get(0).getOverskrift()).isEqualTo("underoverskrift 1");
        assertThat(underavsnitt.get(0).getBrødtekst()).isNull();
        assertThat(underavsnitt.get(0).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(0).getFritekst()).isEqualTo("fritekst linje 1\n\nfritekst linje2");
    }

    @Test
    void skal_skille_mellom_påkrevet_og_valgfritt_fritekstfelt() {
        var avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        var resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n"
                        + VedtaksbrevFritekst.markerPåkrevetFritekst(null, null)
                        + "\n_underoverskrift 2\n"
                        + VedtaksbrevFritekst.markerValgfriFritekst(null)
                , avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        var underavsnitt = resultat.getUnderavsnittsliste();
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
    void skal_utlede_underavsnittstype_fra_fritekstmarkering_slik_at_det_er_mulig_å_skille_mellom_særlige_grunner_og_andre_særlige_grunner() {
        var avsnittbuilder = new Avsnitt.Builder().medOverskrift("Hovedoverskrift");
        var resultat = TekstformatererVedtaksbrev.parseTekst("_underoverskrift 1\n"
                        + VedtaksbrevFritekst.markerValgfriFritekst(null, Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER)
                        + "\n_underoverskrift 2\n"
                        + "brødtekst " + VedtaksbrevFritekst.markerValgfriFritekst(null, Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET)
                        + "\n_underoverskrift 3\n"
                , avsnittbuilder, null).build();
        assertThat(resultat.getOverskrift()).isEqualTo("Hovedoverskrift");
        var underavsnitt = resultat.getUnderavsnittsliste();
        assertThat(underavsnitt).hasSize(3);
        assertThat(underavsnitt.get(0).getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER);
        assertThat(underavsnitt.get(1).getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET);
        assertThat(underavsnitt.get(1).getBrødtekst()).isEqualTo("brødtekst ");
        assertThat(underavsnitt.get(1).isFritekstTillatt()).isTrue();
        assertThat(underavsnitt.get(2).getUnderavsnittstype()).isEqualTo(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET);
        assertThat(underavsnitt.get(2).isFritekstTillatt()).isFalse();
    }
}
