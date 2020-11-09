package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Ignore;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Vurdering;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Ignore
public class DokumentasjonGeneratorPeriodeVilkår {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 16));

    private static VilkårResultat[] vilkårResultat = new VilkårResultat[] {
        VilkårResultat.FORSTO_BURDE_FORSTÅTT,
        VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
        VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
    };
    private static ForeldelseVurderingType[] foreldelseVurderinger = new ForeldelseVurderingType[] {
        ForeldelseVurderingType.IKKE_VURDERT,
        ForeldelseVurderingType.IKKE_FORELDET,
        ForeldelseVurderingType.TILLEGGSFRIST
    };
    private static boolean[] trueFalse = new boolean[] { true, false};

    @Test
    public void generer_vilkår_for_fp() {
        lagVilkårstekster(FagsakYtelseType.FORELDREPENGER, Språkkode.nb);
    }

    @Test
    public void generer_vilkår_for_fp_nynorsk() {
        lagVilkårstekster(FagsakYtelseType.FORELDREPENGER, Språkkode.nn);
    }

    @Test
    public void generer_vilkår_for_svp() {
        lagVilkårstekster(FagsakYtelseType.SVANGERSKAPSPENGER, Språkkode.nb);
    }

    @Test
    public void generer_vilkår_for_svp_nynorsk() {
        lagVilkårstekster(FagsakYtelseType.SVANGERSKAPSPENGER, Språkkode.nn);
    }

    @Test
    public void generer_vilkår_for_es() {
        lagVilkårstekster(FagsakYtelseType.ENGANGSTØNAD, Språkkode.nb);
    }

    @Test
    public void generer_vilkår_for_es_nynorsk() {
        lagVilkårstekster(FagsakYtelseType.ENGANGSTØNAD, Språkkode.nn);
    }

    @Test
    public void generer_vilkår_for_frisinn() {
        lagVilkårstekster(FagsakYtelseType.FRISINN, Språkkode.nb);
    }

    @Test
    public void generer_vilkår_for_frisinn_nynorsk() {
        lagVilkårstekster(FagsakYtelseType.FRISINN, Språkkode.nn);
    }

    private void lagVilkårstekster(FagsakYtelseType ytelsetype, Språkkode språkkode) {
        for (VilkårResultat resultat : vilkårResultat) {
            for (Vurdering vurdering : Aktsomhet.values()) {
                for (ForeldelseVurderingType foreldelseVurdering : foreldelseVurderinger) {
                    lagResultatOgVurderingTekster(ytelsetype, språkkode, resultat, vurdering, foreldelseVurdering, false, false, false);
                    lagResultatOgVurderingTekster(ytelsetype, språkkode, resultat, vurdering, foreldelseVurdering, true, false, false);

                    if (vurdering == Aktsomhet.SIMPEL_UAKTSOM) {
                        lagResultatOgVurderingTekster(ytelsetype, språkkode, resultat, vurdering, foreldelseVurdering, false, false, true);
                    }
                }
            }
        }

        for (ForeldelseVurderingType foreldelseVurdering : foreldelseVurderinger) {
            for (boolean fritekst : trueFalse) {
                for (boolean pengerIBehold : trueFalse) {
                    lagResultatOgVurderingTekster(ytelsetype, språkkode, VilkårResultat.GOD_TRO, AnnenVurdering.GOD_TRO, foreldelseVurdering, fritekst, pengerIBehold, false);
                }
            }
        }

        lagResultatOgVurderingTekster(ytelsetype, språkkode, VilkårResultat.UDEFINERT, AnnenVurdering.FORELDET, ForeldelseVurderingType.FORELDET, false, false, false);
    }

    private void lagResultatOgVurderingTekster(FagsakYtelseType ytelsetype, Språkkode språkkode, VilkårResultat resultat, Vurdering vurdering, ForeldelseVurderingType foreldelsevurdering, boolean fritekst, boolean pengerIBehold, boolean lavtBeløp) {
        HbVedtaksbrevPeriodeOgFelles periodeOgFelles = lagPeriodeOgFelles(ytelsetype, språkkode, resultat,
            vurdering, lavtBeløp, foreldelsevurdering, fritekst, pengerIBehold);
        String vilkårTekst = TekstformatererVedtaksbrev.lagVilkårTekst(periodeOgFelles);
        String overskrift = overskrift(resultat, vurdering, lavtBeløp, fritekst, pengerIBehold, foreldelsevurdering);
        String prettyprint = prettyprint(vilkårTekst, overskrift);

        System.out.println();
        System.out.println(prettyprint);
    }

    private HbVedtaksbrevPeriodeOgFelles lagPeriodeOgFelles(FagsakYtelseType ytelsetype,
                                                            Språkkode språkkode,
                                                            VilkårResultat vilkårResultat,
                                                            Vurdering vurdering,
                                                            boolean lavtBeløp,
                                                            ForeldelseVurderingType foreldelsevurdering,
                                                            boolean fritekst,
                                                            boolean pengerIBehold) {
        HbVedtaksbrevFelles.Builder fellesBuilder = HbVedtaksbrevFelles.builder()
            .medSak(HbSak.build()
                .medYtelsetype(ytelsetype)
                .medAntallBarn(1)
                .medErFødsel(true)
                .medDatoFagsakvedtak(LocalDate.now())
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medFireRettsgebyr(BigDecimal.valueOf(4321))
                .medKlagefristUker(4)
                .medHalvtGrunnbeløp(BigDecimal.ZERO)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("ikke relevant for testen")
            .medSøker(HbPerson.builder()
                .medNavn("Ikke relevant")
                .build())
            .medSpråkkode(språkkode);

        HbVurderinger.Builder vurderingerBuilder = HbVurderinger.builder()
            .medForeldelsevurdering(foreldelsevurdering)
            .medAktsomhetResultat(vurdering)
            .medUnntasInnkrevingPgaLavtBeløp(lavtBeløp);
        if (fritekst) {
            vurderingerBuilder
                .medFritekstVilkår("[fritekst her]");
        }
        if (vilkårResultat != null) {
            vurderingerBuilder
                .medVilkårResultat(vilkårResultat);
        }
        if (AnnenVurdering.GOD_TRO == vurdering) {
            vurderingerBuilder
                .medBeløpIBehold(pengerIBehold ? BigDecimal.valueOf(3999) : BigDecimal.ZERO);
        }
        HbVurderinger vurderinger = vurderingerBuilder.build();
        HbVedtaksbrevPeriode.Builder periodeBuilder = HbVedtaksbrevPeriode.builder()
            .medPeriode(januar)
            .medResultat(HbResultat.builder()
                .medTilbakekrevesBeløp(BigDecimal.valueOf(9999))
                .medRenterBeløp(BigDecimal.ZERO)
                .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(9999))
                .medForeldetBeløp(BigDecimal.valueOf(2999))
                .build())
            .medVurderinger(vurderinger)
            .medKravgrunnlag(HbKravgrunnlag.builder()
                .medFeilutbetaltBeløp(BigDecimal.ZERO)
                .build())
            .medFakta(HendelseType.MEDLEMSKAP_TYPE, HendelseUnderType.IKKE_BOSATT)
            ;

        return new HbVedtaksbrevPeriodeOgFelles(fellesBuilder.build(), periodeBuilder.build());
    }

    private String overskrift(VilkårResultat resultat, Vurdering vurdering, boolean lavtBeløp, boolean fritekst, boolean pengerIBehold, ForeldelseVurderingType foreldelsevurdering) {
        return "*[ " + hentVilkårresultatOverskriftDel(resultat)
            + (vurdering != null ? " - " + vurdering.getNavn() : "")
            + (fritekst ? " - med fritekst" : " - uten fritekst")
            + hentVIlkårsvurderingOverskriftDel(foreldelsevurdering)
            + (pengerIBehold ? " - penger i behold" : "")
            + (lavtBeløp ? " - lavt beløp" : "")
            + " ]*";
    }

    private String hentVilkårresultatOverskriftDel(VilkårResultat resultat) {
        switch (resultat) {
            case UDEFINERT:
                return "Foreldelse";
            case FORSTO_BURDE_FORSTÅTT:
                return "Forsto/Burde forstått";
            case FEIL_OPPLYSNINGER_FRA_BRUKER:
                return "Feilaktive opplysninger";
            case MANGELFULLE_OPPLYSNINGER_FRA_BRUKER:
                return "Mangelfull opplysninger";
            case GOD_TRO:
                return "God tro";
            default:
                throw new IllegalArgumentException("VilkårResultat ikke støttet. Resultat: " + resultat);
        }
    }

    private String hentVIlkårsvurderingOverskriftDel(ForeldelseVurderingType foreldelsevurdering) {
        switch (foreldelsevurdering) {
            case UDEFINERT:
                return "";
            case IKKE_VURDERT:
                return " - automatisk vurdert";
            case IKKE_FORELDET:
                return " - ikke foreldet";
            case FORELDET:
                return " - foreldet";
            case TILLEGGSFRIST:
                return " - med tilleggsfrist";
            default:
                throw new IllegalArgumentException("ForeldelseVurderingType ikke støttet. Type: " + foreldelsevurdering);
        }
    }

    private String prettyprint(String vilkårTekst, String overskrift) {
        return vilkårTekst
            .replace("_Hvordan har vi kommet fram til at du må betale tilbake?", overskrift)
            .replace("_Hvordan har vi kommet fram til at du ikke må betale tilbake?", overskrift)
            .replace("_Korleis har vi kome fram til at du må betale tilbake?", overskrift)
            .replace("_Korleis har vi kome fram til at du ikkje må betale tilbake?", overskrift)
            .replaceAll(" 4\u00A0321\u00A0kroner", " <4 rettsgebyr> kroner")
            .replaceAll(" 2\u00A0999\u00A0kroner", " <foreldet beløp> kroner")
            .replaceAll(" 3\u00A0999\u00A0kroner", " <beløp i behold> kroner")
            .replaceAll("1. januar 2019", "<periode start>")
            .replaceAll("16. januar 2019", "<periode slutt>")

            .replaceAll("\\[", "[ ")
            .replaceAll("]", " ]");
    }
}
