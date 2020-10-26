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

import org.junit.Test;

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

public class TekstformatererVedtaksbrevAllePermutasjonerAvFaktaTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_FP() {
        lagTeksterOgValider(FagsakYtelseType.FORELDREPENGER, Språkkode.nb);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_FP_nynorsk() {
        lagTeksterOgValider(FagsakYtelseType.FORELDREPENGER, Språkkode.nn);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_SVP() {
        Set<HendelseMedUndertype> unntak1 = Set.of(new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG), new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG));

        lagTeksterOgValider(FagsakYtelseType.SVANGERSKAPSPENGER, Språkkode.nb, unntak1);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_SVP_nynorsk() {
        Set<HendelseMedUndertype> unntak1 = Set.of(new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG), new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG));

        lagTeksterOgValider(FagsakYtelseType.SVANGERSKAPSPENGER, Språkkode.nn, unntak1);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_ES() {
        Set<HendelseMedUndertype> unntak1 = Set.of(new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_IKKE_TILDELT), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_ANDRE_FORELDRE_DODD));
        Set<HendelseMedUndertype> unntak2 = Set.of(new HendelseMedUndertype(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, HendelseUnderType.ES_BARN_OVER_15), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15));

        lagTeksterOgValider(FagsakYtelseType.ENGANGSTØNAD, Språkkode.nb, unntak1, unntak2);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_ES_nynorsk() {
        Set<HendelseMedUndertype> unntak1 = Set.of(new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_IKKE_TILDELT), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_ANDRE_FORELDRE_DODD));
        Set<HendelseMedUndertype> unntak2 = Set.of(new HendelseMedUndertype(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, HendelseUnderType.ES_BARN_OVER_15), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15));

        lagTeksterOgValider(FagsakYtelseType.ENGANGSTØNAD, Språkkode.nn, unntak1, unntak2);
    }

    @SafeVarargs
    private void lagTeksterOgValider(FagsakYtelseType ytelsetype, Språkkode språkkode, Set<HendelseMedUndertype>... unntak) {
        HbVedtaksbrevFelles felles = lagFellesBuilder(språkkode)
            .medSak(HbSak.build()
                .medYtelsetype(ytelsetype)
                .medErFødsel(true)
                .medAntallBarn(1)
                .build())
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);
        sjekkVerdier(resultat, unntak);
    }

    void sjekkVerdier(Map<HendelseMedUndertype, String> verdier, Set<HendelseMedUndertype>... unntatUnikhet) {
        Map<String, Set<HendelseMedUndertype>> tekstTilHendelseType = new TreeMap<>();
        for (Map.Entry<HendelseMedUndertype, String> entry : verdier.entrySet()) {
            HendelseMedUndertype key = entry.getKey();
            String value = entry.getValue();
            if (tekstTilHendelseType.containsKey(value)) {
                tekstTilHendelseType.get(value).add(key);
            } else {
                Set<HendelseMedUndertype> liste = new HashSet<>();
                liste.add(key);
                tekstTilHendelseType.put(value, liste);
            }
        }

        Map<Set<HendelseMedUndertype>, String> hendelseTypeTilTeskst = new HashMap<>();
        for (Map.Entry<String, Set<HendelseMedUndertype>> entry : tekstTilHendelseType.entrySet()) {
            hendelseTypeTilTeskst.put(entry.getValue(), entry.getKey());
        }

        for (Set<HendelseMedUndertype> unntak : unntatUnikhet) {
            hendelseTypeTilTeskst.remove(unntak);
        }

        String feilmelding = "";
        for (Map.Entry<Set<HendelseMedUndertype>, String> entry : hendelseTypeTilTeskst.entrySet()) {
            if (entry.getKey().size() > 1) {
                feilmelding += entry.getValue() + " mapper alle til " + entry.getKey() + "\n";
            }
        }

        if (!feilmelding.isEmpty()) {
            throw new AssertionError(feilmelding);
        }
    }

    private Map<HendelseMedUndertype, String> lagFaktatekster(HbVedtaksbrevFelles felles) {
        Map<HendelseMedUndertype, String> resultat = new LinkedHashMap<>();
        for (HendelseMedUndertype undertype : getFeilutbetalingsårsaker(felles.getYtelsetype())) {
            HbVedtaksbrevPeriode periode = lagPeriodeBuilder()
                .medFakta(undertype.getHendelseType(), undertype.getHendelseUnderType())
                .build();
            HbVedtaksbrevPeriodeOgFelles data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);
            String tekst = TekstformatererVedtaksbrev.lagFaktaTekst(data);
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
        Set<HendelseType> hendelseTyper = HendelseTypePrYtelseType.getHendelsetyper(ytelseType);
        Map<HendelseType, Set<HendelseUnderType>> hendelseUndertypePrHendelseType = HendelseUndertypePrHendelseType.getHendelsetypeHierarki();

        List<HendelseMedUndertype> resultat = new ArrayList<>();
        for (HendelseType hendelseType : hendelseTyper) {
            for (HendelseUnderType hendelseUnderType : hendelseUndertypePrHendelseType.get(hendelseType)) {
                resultat.add(new HendelseMedUndertype(hendelseType, hendelseUnderType));
            }
        }
        return resultat;
    }

}
