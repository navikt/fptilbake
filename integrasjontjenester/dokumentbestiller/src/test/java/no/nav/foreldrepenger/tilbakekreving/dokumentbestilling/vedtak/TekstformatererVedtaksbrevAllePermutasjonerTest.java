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

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.EsHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;


public class TekstformatererVedtaksbrevAllePermutasjonerTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Rule
    public UnittestRepositoryRule unittestRepositoryRule = new UnittestRepositoryRule();

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(unittestRepositoryRule.getEntityManager());

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_FP() {
        HbVedtaksbrevFelles felles = lagFellesBuilder()
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medErFødsel(true)
            .medAntallBarn(1)
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);
        sjekkVerdier(resultat);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_SVP() {
        HbVedtaksbrevFelles felles = lagFellesBuilder()
            .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
            .medErFødsel(true)
            .medAntallBarn(1)
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);

        Set<HendelseMedUndertype> unntak1 = Set.of(new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, SvpHendelseUnderTyper.SVP_TILRETTELEGGING_FULLT_MULIG), new HendelseMedUndertype(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, SvpHendelseUnderTyper.SVP_TILRETTELEGGING_DELVIS_MULIG));

        sjekkVerdier(resultat, unntak1);
    }

    @Test
    public void skal_støtte_alle_permutasjoner_av_fakta_for_ES() {
        HbVedtaksbrevFelles felles = lagFellesBuilder()
            .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
            .medErFødsel(true)
            .medAntallBarn(1)
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);

        Set<HendelseMedUndertype> unntak1 = Set.of(new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, EsHendelseUnderTyper.ES_IKKE_TILDELT), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, EsHendelseUnderTyper.ES_ANDRE_FORELDRE_DODD));
        Set<HendelseMedUndertype> unntak2 = Set.of(new HendelseMedUndertype(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, EsHendelseUnderTyper.ES_BARN_OVER_15), new HendelseMedUndertype(HendelseType.ES_FORELDREANSVAR_TYPE, EsHendelseUnderTyper.ES_FORELDREANSVAR_BARN_OVER_15));

        sjekkVerdier(resultat, unntak1, unntak2);
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
                .build());
    }

    private HbVedtaksbrevFelles.Builder lagFellesBuilder() {
        return builder()
            .medLovhjemmelVedtak("Folketrygdloven")
            .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
            .medTotaltRentebeløp(BigDecimal.valueOf(1000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(10000))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(11000))
            .medVarsletBeløp(BigDecimal.valueOf(10000))
            .medVarsletDato(LocalDate.now().minusDays(100))
            .medKlagefristUker(6)
            .skruAvMidlertidigTekst();
    }

    private List<HendelseMedUndertype> getFeilutbetalingsårsaker(FagsakYtelseType ytelseType) {
        Set<HendelseType> hendelseTyper = kodeverkRepository.hentKodeRelasjonForKodeverk(FagsakYtelseType.class, HendelseType.class).get(ytelseType);
        Map<HendelseType, Set<HendelseUnderType>> hendelseUndertypePrHendelseType = kodeverkRepository.hentKodeRelasjonForKodeverk(HendelseType.class, HendelseUnderType.class);

        List<HendelseMedUndertype> resultat = new ArrayList<>();
        for (HendelseType hendelseType : hendelseTyper) {
            for (HendelseUnderType hendelseUnderType : hendelseUndertypePrHendelseType.get(hendelseType)) {
                resultat.add(new HendelseMedUndertype(hendelseType, hendelseUnderType));
            }
        }
        return resultat;
    }

}
