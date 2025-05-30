package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class TekstformatererVedtaksbrevVedleggTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
    private final Periode mars = Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31));

    @Test
    void skal_generere_vedlegg_med_en_periode_uten_renter() throws Exception {
        var data = getVedtaksbrevData(null);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(data);
        var fasit = les("/vedtaksbrev/vedlegg/vedlegg_uten_renter.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedlegg_med_en_periode_uten_renter_nynorsk() throws Exception {
        var data = getVedtaksbrevData(Språkkode.NN);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(data);
        var fasit = les("/vedtaksbrev/vedlegg/vedlegg_uten_renter_nn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedlegg_med_en_periode_uten_skatt() throws Exception {
        var data = getVedtaksbrevData(Språkkode.NB, 10000, 30001, 30001, 0, 0);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(data);
        var fasit = les("/vedtaksbrev/vedlegg/vedlegg_uten_skatt.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    private HbVedtaksbrevData getVedtaksbrevData(Språkkode språkkode) {
        return getVedtaksbrevData(språkkode, 33001, 30001, 20002, 0, 20002 - 16015);
    }

    private HbVedtaksbrevData getVedtaksbrevData(Språkkode språkkode, int varslet, int feilutbetalt, int tilbakekreves, int renter, int skatt) {
        var vedtaksbrevData = lagTestBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(2)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(tilbakekreves))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(tilbakekreves + renter))
                        .medTotaltRentebeløp(BigDecimal.valueOf(renter))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(tilbakekreves + renter - skatt))
                        .build())
                .medLovhjemmelVedtak("Folketrygdloven § 22-15")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(varslet))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .medSpråkkode(språkkode != null ? språkkode : Språkkode.NB)
                .build();
        var perioder = List.of(
                HbVedtaksbrevPeriode.builder()
                        .medPeriode(januar)
                        .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(feilutbetalt)))
                        .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.REFUSJON_ARBEIDSGIVER)
                        .medVurderinger(HbVurderinger.builder()
                                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                                .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                                .medSærligeGrunner(List.of(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                                .build())
                        .medResultat(HbResultat.builder()
                                .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(tilbakekreves - skatt))
                                .medTilbakekrevesBeløp(BigDecimal.valueOf(tilbakekreves))
                                .medRenterBeløp(BigDecimal.valueOf(renter))
                                .build())
                        .build()
        );
        return new HbVedtaksbrevData(vedtaksbrevData, perioder);
    }

    @Test
    void skal_generere_vedlegg_med_flere_perioder_og_med_renter() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(2)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23302))
                        .medTotaltRentebeløp(BigDecimal.valueOf(300))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(18537))
                        .build())
                .medLovhjemmelVedtak("Folketrygdloven § 22-15")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(33001))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .build();
        var perioder = List.of(
                HbVedtaksbrevPeriode.builder()
                        .medPeriode(januar)
                        .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(30001)))
                        .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.REFUSJON_ARBEIDSGIVER)
                        .medVurderinger(HbVurderinger.builder()
                                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                                .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                                .medSærligeGrunner(Arrays.asList(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                                .build())
                        .medResultat(HbResultat.builder()
                                .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(16015))
                                .medTilbakekrevesBeløp(BigDecimal.valueOf(20002))
                                .medRenterBeløp(BigDecimal.ZERO)
                                .build())
                        .build(),
                HbVedtaksbrevPeriode.builder()
                        .medPeriode(februar)
                        .medKravgrunnlag(HbKravgrunnlag.builder()
                                .medFeilutbetaltBeløp(BigDecimal.valueOf(3000))
                                .medRiktigBeløp(BigDecimal.valueOf(3000))
                                .medUtbetaltBeløp(BigDecimal.valueOf(6000))
                                .build())
                        .medFakta(HendelseType.ØKONOMI_FEIL, HendelseUnderType.DOBBELTUTBETALING)
                        .medVurderinger(HbVurderinger.builder()
                                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                                .medVilkårResultat(VilkårResultat.GOD_TRO)
                                .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                                .medBeløpIBehold(BigDecimal.ZERO)
                                .build())
                        .medResultat(HbResultat.builder()
                                .medTilbakekrevesBeløpUtenSkatt(BigDecimal.ZERO)
                                .medTilbakekrevesBeløp(BigDecimal.ZERO)
                                .medRenterBeløp(BigDecimal.ZERO)
                                .build())
                        .build(),
                HbVedtaksbrevPeriode.builder()
                        .medPeriode(mars)
                        .medKravgrunnlag(HbKravgrunnlag.builder()
                                .medFeilutbetaltBeløp(BigDecimal.valueOf(3000))
                                .medRiktigBeløp(BigDecimal.valueOf(3000))
                                .medUtbetaltBeløp(BigDecimal.valueOf(6000))
                                .build())
                        .medFakta(HendelseType.ØKONOMI_FEIL, HendelseUnderType.DOBBELTUTBETALING)
                        .medVurderinger(HbVurderinger.builder()
                                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                                .medAktsomhetResultat(Aktsomhet.FORSETT)
                                .build())
                        .medResultat(
                                HbResultat.builder()
                                        .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(2222))
                                        .medTilbakekrevesBeløp(BigDecimal.valueOf(3000))
                                        .medRenterBeløp(BigDecimal.valueOf(300))
                                        .build()
                        )
                        .build()
        );
        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(data);
        var fasit = les("/vedtaksbrev/vedlegg/vedlegg_med_og_uten_renter.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    private HbVedtaksbrevFelles.Builder lagTestBuilder() {
        return HbVedtaksbrevFelles.builder()
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(6)
                        .build())
                .medSøker(HbPerson.builder()
                        .medNavn("Søker Søkersen")
                        .medDødsdato(LocalDate.of(2018, 3, 1))
                        .medErGift(true)
                        .build());
    }

    private String les(String filnavn) throws IOException {
        try (var resource = getClass().getResourceAsStream(filnavn); var scanner = new Scanner(resource, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
    }

}
