package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseTypePrYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUndertypePrHendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class TekstformatererVedtaksbrevAllePermutasjonerAvFaktaTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    void skal_støtte_alle_permutasjoner_av_fakta_for_FP() {
        lagTeksterOgValider(FagsakYtelseType.FORELDREPENGER, Språkkode.NB);
    }

    @Test
    void skal_støtte_alle_permutasjoner_av_fakta_for_FP_nynorsk() {
        lagTeksterOgValider(FagsakYtelseType.FORELDREPENGER, Språkkode.NN);
    }

    @Test
    void skal_støtte_alle_permutasjoner_av_fakta_for_SVP() {
        var unntak1 = Set.of(new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG), new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG));

        lagTeksterOgValider(FagsakYtelseType.SVANGERSKAPSPENGER, Språkkode.NB, unntak1);
    }

    @Test
    void skal_støtte_alle_permutasjoner_av_fakta_for_SVP_nynorsk() {
        var unntak1 = Set.of(new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG), new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG));

        lagTeksterOgValider(FagsakYtelseType.SVANGERSKAPSPENGER, Språkkode.NN, unntak1);
    }

    @Test
    void skal_støtte_alle_permutasjoner_av_fakta_for_ES() {
        var unntak1 = Set.of(new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_IKKE_TILDELT), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_ANDRE_FORELDRE_DODD));
        var unntak2 = Set.of(new HendelseMedUndertype(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, HendelseUnderType.ES_BARN_OVER_15), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15));

        lagTeksterOgValider(FagsakYtelseType.ENGANGSTØNAD, Språkkode.NB, unntak1, unntak2);
    }

    @Test
    void skal_støtte_alle_permutasjoner_av_fakta_for_ES_nynorsk() {
        var unntak1 = Set.of(new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_IKKE_TILDELT), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_ANDRE_FORELDRE_DODD));
        var unntak2 = Set.of(new HendelseMedUndertype(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, HendelseUnderType.ES_BARN_OVER_15), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15));

        lagTeksterOgValider(FagsakYtelseType.ENGANGSTØNAD, Språkkode.NN, unntak1, unntak2);
    }

    @SafeVarargs
    private void lagTeksterOgValider(FagsakYtelseType ytelsetype, Språkkode språkkode, Set<HendelseMedUndertype>... unntak) {
        var felles = lagFellesBuilder(språkkode)
                .medSak(HbSak.build()
                        .medYtelsetype(ytelsetype)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .build();
        var resultat = lagFaktatekster(felles);
        sjekkVerdier(resultat, unntak);
    }

    void sjekkVerdier(Map<HendelseMedUndertype, String> verdier, Set<HendelseMedUndertype>... unntatUnikhet) {
        Map<String, Set<HendelseMedUndertype>> tekstTilHendelseType = new TreeMap<>();
        for (var entry : verdier.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (tekstTilHendelseType.containsKey(value)) {
                tekstTilHendelseType.get(value).add(key);
            } else {
                Set<HendelseMedUndertype> liste = new HashSet<>();
                liste.add(key);
                tekstTilHendelseType.put(value, liste);
            }
        }

        Map<Set<HendelseMedUndertype>, String> hendelseTypeTilTeskst = new HashMap<>();
        for (var entry : tekstTilHendelseType.entrySet()) {
            hendelseTypeTilTeskst.put(entry.getValue(), entry.getKey());
        }

        for (var unntak : unntatUnikhet) {
            hendelseTypeTilTeskst.remove(unntak);
        }

        var feilmelding = "";
        for (var entry : hendelseTypeTilTeskst.entrySet()) {
            if (entry.getKey().size() > 2) {
                feilmelding += entry.getValue() + " mapper alle til " + entry.getKey() + "\n";
            }
        }

        if (!feilmelding.isEmpty()) {
            throw new AssertionError(feilmelding);
        }
    }

    private Map<HendelseMedUndertype, String> lagFaktatekster(HbVedtaksbrevFelles felles) {
        Map<HendelseMedUndertype, String> resultat = new LinkedHashMap<>();
        for (var undertype : getFeilutbetalingsårsaker(felles.getYtelsetype())) {
            var periode = lagPeriodeBuilder()
                    .medFakta(undertype.hendelseType(), undertype.hendelseUnderType())
                    .build();
            var data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);
            var tekst = TekstformatererVedtaksbrev.lagFaktaTekst(data);
            resultat.put(undertype, tekst);
        }
        return resultat;
    }

    private HbVedtaksbrevPeriode.Builder lagPeriodeBuilder() {
        return HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                        .medFeilutbetaltBeløp(BigDecimal.valueOf(10000))
                        .medUtbetaltBeløp(BigDecimal.valueOf(33333))
                        .medRiktigBeløp(BigDecimal.valueOf(23333))
                        .build())
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                        .medVilkårResultat(VilkårResultat.GOD_TRO)
                        .medBeløpIBehold(BigDecimal.valueOf(10000))
                        .build())
                .medResultat(HbResultat.builder()
                        .medTilbakekrevesBeløp(BigDecimal.valueOf(10000))
                        .medRenterBeløp(BigDecimal.valueOf(1000))
                        .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(9000))
                        .build());
    }

    private HbVedtaksbrevFelles.Builder lagFellesBuilder(Språkkode språkkode) {
        return builder()
                .medLovhjemmelVedtak("Folketrygdloven")
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                        .medTotaltRentebeløp(BigDecimal.valueOf(1000))
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(10000))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(11000))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(11000))
                        .build())
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(10000))
                        .medVarsletDato(LocalDate.now().minusDays(100))
                        .build())
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(6)
                        .build())
                .medSpråkkode(språkkode)
                .medSøker(HbPerson.builder()
                        .medNavn("Søker Søkersen")
                        .medDødsdato(LocalDate.of(2018, 3, 1))
                        .medErGift(true)
                        .build())
                ;
    }

    private List<HendelseMedUndertype> getFeilutbetalingsårsaker(FagsakYtelseType ytelseType) {
        var hendelseTyper = HendelseTypePrYtelseType.getHendelsetyper(ytelseType);
        var hendelseUndertypePrHendelseType = HendelseUndertypePrHendelseType.getHendelsetypeHierarki();

        List<HendelseMedUndertype> resultat = new ArrayList<>();
        for (var hendelseType : hendelseTyper) {
            for (var hendelseUnderType : hendelseUndertypePrHendelseType.get(hendelseType)) {
                resultat.add(new HendelseMedUndertype(hendelseType, hendelseUnderType));
            }
        }
        return resultat;
    }

}
