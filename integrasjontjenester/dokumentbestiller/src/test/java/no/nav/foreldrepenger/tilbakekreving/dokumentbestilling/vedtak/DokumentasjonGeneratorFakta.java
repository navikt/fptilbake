package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;


@Ignore("Kjøres ved behov for å regenerere dokumentasjon")
public class DokumentasjonGeneratorFakta {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Rule
    public UnittestRepositoryRule unittestRepositoryRule = new UnittestRepositoryRule();

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(unittestRepositoryRule.getEntityManager());

    @Test
    public void list_ut_permutasjoner_for_FP() {
        HbVedtaksbrevFelles felles = lagFellesBuilder()
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medErFødsel(true)
            .medAntallBarn(1)
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Ignore("Må implementere fakta for SVP helt ferdig før")
    @Test
    public void list_ut_permutasjoner_for_SVP() {
        HbVedtaksbrevFelles felles = lagFellesBuilder()
            .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
            .medErFødsel(true)
            .medAntallBarn(1)
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    public void list_ut_permutasjoner_for_ES() {
        HbVedtaksbrevFelles felles = lagFellesBuilder()
            .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
            .medErFødsel(true)
            .medAntallBarn(1)
            .build();
        Map<HendelseMedUndertype, String> resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    private void prettyPrint(Map<HendelseMedUndertype, String> resultat) {
        for (Map.Entry<HendelseMedUndertype, String> entry : resultat.entrySet()) {
            HendelseMedUndertype typer = entry.getKey();
            System.out.println("[" + typer.getHendelseType().getNavn() + " - " + typer.getHendelseUnderType().getNavn() + "]");
            String generertTekst = entry.getValue();
            String parametrisertTekst = generertTekst.replaceAll(" 10000 kroner", " <feilutbetalt beløp> kroner");
            System.out.println(parametrisertTekst);
            System.out.println();
        }
    }

    private Map<HendelseMedUndertype, String> lagFaktatekster(HbVedtaksbrevFelles felles) {
        Map<HendelseMedUndertype, String> resultat = new LinkedHashMap<>();
        for (HendelseMedUndertype undertype : getFeilutbetalingsårsaker(felles.getYtelsetype())) {
            HbVedtaksbrevPeriode periode = lagPeriodeBuilder()
                .medHendelsetype(undertype.getHendelseType())
                .medHendelseUndertype(undertype.getHendelseUnderType())
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
            .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
            .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
            .medFeilutbetaltBeløp(BigDecimal.valueOf(10000))
            .medTilbakekrevesBeløp(BigDecimal.valueOf(5000))
            .medRenterBeløp(BigDecimal.ZERO)
            .medVilkårResultat(VilkårResultat.GOD_TRO)
            .medBeløpIBehold(BigDecimal.valueOf(5000));
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
            .medKlagefristUker(6);
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

        Collections.sort(resultat, new HendelseMedUndertypeComparator());
        return resultat;
    }

    static class HendelseMedUndertypeComparator implements Comparator<HendelseMedUndertype> {

        @Override
        public int compare(HendelseMedUndertype o1, HendelseMedUndertype o2) {
            int hendelseCompare = o1.getHendelseType().getNavn().compareTo(o2.getHendelseType().getNavn());
            if (hendelseCompare != 0) {
                return hendelseCompare;
            }
            return Long.compare(
                Long.parseLong(o1.getHendelseUnderType().getEkstraData()),
                Long.parseLong(o2.getHendelseUnderType().getEkstraData())
            );
        }
    }

}
