package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

/*
 * Brukes for å liste ut alle alternativer som kan genereres for særlige grunner
 */
@Ignore("Kjøres ved behov for å regenerere dokumentasjon")
public class DokumentasjonGeneratorSærligeGrunner {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    public void list_ut_særlige_grunner_simpel_uaktsomhet() {
        HbVedtaksbrevFelles felles = lagFellesdel();

        boolean[] boolske = {false, true};

        for (boolean sgNav : boolske) {
            for (boolean sgBeløp : boolske) {
                for (boolean sgTid : boolske) {
                    for (boolean reduksjon : boolske) {
                        for (boolean sgAnnet : boolske) {
                            HbVedtaksbrevPeriode periode = lagPeriodeDel(Aktsomhet.SIMPEL_UAKTSOM, sgNav, sgBeløp, sgTid, sgAnnet, reduksjon);
                            String s = TekstformatererVedtaksbrev.lagSærligeGrunnerTekst(felles, periode);
                            String overskrift = overskrift(sgNav, sgBeløp, sgTid, sgAnnet, reduksjon);
                            String prettyprint = s.replace("_Er det særlige grunner til å redusere beløpet?", overskrift)
                                .replace(" 500 kroner", " <kravbeløp> kroner");

                            System.out.println();
                            System.out.println(prettyprint);
                        }
                    }
                }
            }
        }
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
        return "[" + String.join(" - ", deler) + "]";
    }

    private HbVedtaksbrevPeriode lagPeriodeDel(Aktsomhet aktsomhet, boolean sgNav, boolean sgBeløp, boolean sgTid, boolean sgAnnet, boolean reduksjon) {
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
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medAktsomhetResultat(aktsomhet)
                .medSærligeGrunner(sg, null, fritekstSærligeGrunnerAnnet)
                .build()            )
            .medKravgrunnlag(HbKravgrunnlag.builder()
                .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                .build()            )
            .medResultat(HbResultat.builder()
                .medTilbakekrevesBeløp(BigDecimal.valueOf(reduksjon ? 500 : 1000))
                .medRenterBeløp(BigDecimal.ZERO)
                .build())
            .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, FpHendelseUnderTyper.GRADERT_UTTAK)
            .build();
    }

    private HbVedtaksbrevFelles lagFellesdel() {
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
                .medTotaltRentebeløp(BigDecimal.valueOf(100))
                .build())
            .medLovhjemmelVedtak("foo")
            .medVarsletBeløp(BigDecimal.valueOf(1000))
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(4)
            .build();
    }
}
