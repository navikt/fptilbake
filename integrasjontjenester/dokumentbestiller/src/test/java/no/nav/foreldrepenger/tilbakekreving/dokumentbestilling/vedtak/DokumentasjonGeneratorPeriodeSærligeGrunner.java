package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

/**
 * Brukes for å generere tekster for særlige grunner for perioder. Resultatet er tekster med markup, som med
 * "Insert markup"-macroen kan limes inn i Confluence, og dermed bli formattert tekst.
 *
 * Confluence:
 * FP/SVP/ES: https://confluence.adeo.no/display/TVF/Generert+dokumentasjon
 * FRISINN: https://confluence.adeo.no/display/MODNAV/Generert+dokumentasjon
 */
@Disabled("Kjøres ved behov for å regenerere dokumentasjon")
public class DokumentasjonGeneratorPeriodeSærligeGrunner {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    public void list_ut_særlige_grunner_simpel_uaktsomhet_forstod() {
        HbVedtaksbrevFelles felles = lagFellesdel(null);
        lagSærligeGrunnerTekster(felles, VilkårResultat.FORSTO_BURDE_FORSTÅTT, Aktsomhet.SIMPEL_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_simpel_uaktsomhet_forstod_nynorsk() {
        HbVedtaksbrevFelles felles = lagFellesdel(Språkkode.nn);
        lagSærligeGrunnerTekster(felles, VilkårResultat.FORSTO_BURDE_FORSTÅTT, Aktsomhet.SIMPEL_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_grov_uaktsomhet_forstod() {
        HbVedtaksbrevFelles felles = lagFellesdel(null);
        lagSærligeGrunnerTekster(felles, VilkårResultat.FORSTO_BURDE_FORSTÅTT, Aktsomhet.GROVT_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_grov_uaktsomhet_forstod_nynorsk() {
        HbVedtaksbrevFelles felles = lagFellesdel(Språkkode.nn);
        lagSærligeGrunnerTekster(felles, VilkårResultat.FORSTO_BURDE_FORSTÅTT, Aktsomhet.GROVT_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_simpel_uaktsomhet_feil_mangelfulle_opplysninger() {
        HbVedtaksbrevFelles felles = lagFellesdel(null);
        lagSærligeGrunnerTekster(felles, VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, Aktsomhet.SIMPEL_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_simpel_uaktsomhet_feil_mangelfulle_opplysninger_nynorsk() {
        HbVedtaksbrevFelles felles = lagFellesdel(Språkkode.nn);
        lagSærligeGrunnerTekster(felles, VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, Aktsomhet.SIMPEL_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_grov_uaktsomhet_feil_mangelfulle_opplysninger() {
        HbVedtaksbrevFelles felles = lagFellesdel(null);
        lagSærligeGrunnerTekster(felles, VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER, Aktsomhet.GROVT_UAKTSOM);
    }

    @Test
    public void list_ut_særlige_grunner_grov_uaktsomhet_feil_mangelfulle_opplysninger_nynorsk() {
        HbVedtaksbrevFelles felles = lagFellesdel(Språkkode.nn);
        lagSærligeGrunnerTekster(felles, VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER, Aktsomhet.GROVT_UAKTSOM);
    }

    private void lagSærligeGrunnerTekster(HbVedtaksbrevFelles felles, VilkårResultat forstoBurdeForstått, Aktsomhet simpelUaktsom) {
        boolean[] boolske = {false, true};

        for (boolean sgNav : boolske) {
            for (boolean sgBeløp : boolske) {
                for (boolean sgTid : boolske) {
                    for (boolean reduksjon : boolske) {
                        for (boolean sgAnnet : boolske) {
                            lagSærligeGrunnerTekster(felles, forstoBurdeForstått, simpelUaktsom, sgNav, sgBeløp, sgTid, reduksjon, sgAnnet);
                        }
                    }
                }
            }
        }
    }

    private void lagSærligeGrunnerTekster(HbVedtaksbrevFelles felles,
                                          VilkårResultat vilkårResultat,
                                          Aktsomhet aktsomhet,
                                          boolean sgNav,
                                          boolean sgBeløp,
                                          boolean sgTid,
                                          boolean reduksjon,
                                          boolean sgAnnet) {
        HbVedtaksbrevPeriode periode = lagPeriodeDel(vilkårResultat, aktsomhet, sgNav, sgBeløp, sgTid, sgAnnet, reduksjon);
        String s = TekstformatererVedtaksbrev.lagSærligeGrunnerTekst(felles, periode);
        String overskrift = overskrift(sgNav, sgBeløp, sgTid, sgAnnet, reduksjon);
        String prettyprint = prettyprint(s, overskrift);

        System.out.println();
        System.out.println(prettyprint);
    }

    private String overskrift(boolean sgNav, boolean sgBeløp, boolean sgTid, boolean sgAnnet, boolean reduksjon) {
        List<String> deler = new ArrayList<>();
        deler.add("grad av uaktsomhet");
        if (sgNav) {
            deler.add("NAV helt/delvis skyld");
        }
        if (sgBeløp) {
            deler.add("størrelsen på beløpet");
        }
        if (sgTid) {
            deler.add("hvor lang tid har det gått");
        }
        if (reduksjon) {
            deler.add("reduksjon");
        }
        if (sgAnnet) {
            deler.add("annet");
        }
        return "*[" + String.join(" - ", deler) + "]*";
    }

    private HbVedtaksbrevPeriode lagPeriodeDel(VilkårResultat vilkårResultat, Aktsomhet aktsomhet, boolean sgNav, boolean sgBeløp, boolean sgTid, boolean sgAnnet, boolean reduksjon) {
        List<SærligGrunn> sg = new ArrayList<>();
        if (sgNav) {
            sg.add(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL);
        }
        if (sgBeløp) {
            sg.add(SærligGrunn.STØRRELSE_BELØP);
        }
        if (sgTid) {
            sg.add(SærligGrunn.TID_FRA_UTBETALING);
        }
        if (sgAnnet) {
            sg.add(SærligGrunn.ANNET);
        }

        String fritekstSærligeGrunnerAnnet = "[fritekst her]";
        return HbVedtaksbrevPeriode.builder()
            .medPeriode(januar)
            .medVurderinger(HbVurderinger.builder()
                .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                .medVilkårResultat(vilkårResultat)
                .medAktsomhetResultat(aktsomhet)
                .medSærligeGrunner(sg, null, fritekstSærligeGrunnerAnnet)
                .build())
            .medKravgrunnlag(HbKravgrunnlag.builder()
                .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                .build())
            .medResultat(HbResultat.builder()
                .medTilbakekrevesBeløp(BigDecimal.valueOf(reduksjon ? 500 : 1000))
                .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(reduksjon ? 400 : 800))
                .medRenterBeløp(BigDecimal.ZERO)
                .build())
            .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
            .build();
    }

    private HbVedtaksbrevFelles lagFellesdel(Språkkode språkkode) {
        return HbVedtaksbrevFelles.builder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErFødsel(true)
                .medAntallBarn(1)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1100))
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(900))
                .medTotaltRentebeløp(BigDecimal.valueOf(100))
                .build())
            .medLovhjemmelVedtak("foo")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(1000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(4)
                .build())
            .medSøker(HbPerson.builder()
                .medNavn("Søker Søkersen")
                .medErGift(true)
                .build())
            .medSpråkkode(språkkode != null ? språkkode : Språkkode.nb)
            .build();
    }

    private String prettyprint(String s, String overskrift) {
        return s.replace("_Er det særlige grunner til å redusere beløpet?", overskrift)
            .replace("_Er det særlege grunnar til å redusere beløpet?", overskrift)
            .replace(" 500\u00A0kroner", " <kravbeløp> kroner")
            .replaceAll("\\[", "[ ")
            .replaceAll("]", " ]");
    }
}
