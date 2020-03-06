package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TekstformatererVarselbrevTest {

    private FagsakYtelseType foreldrepengerkode = FagsakYtelseType.FORELDREPENGER;
    private FagsakYtelseType engangsstønadkode = FagsakYtelseType.ENGANGSTØNAD;
    private FagsakYtelseType svangerskapspengerkode = FagsakYtelseType.SVANGERSKAPSPENGER;

    private final LocalDate JANUAR_1_2019 = LocalDate.of(2019, 1, 1);
    private final LocalDate JANUAR_30_2019 = LocalDate.of(2019, 1, 30);
    private final LocalDate REVURDERING_VEDTAK_DATO = LocalDate.of(2019, 12, 18);
    private final LocalDate FRIST_DATO = LocalDate.of(2020, 4, 4);

    @Test
    public void skal_generere_varseltekst_for_flere_perioder() throws Exception {
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medFagsaktype(svangerskapspengerkode)
            .medSprakkode(Språkkode.nn)
            .medFagsaktypenavnPåSpråk("svangerskapspengar")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedFlerePerioder())
            .medFristdato(FRIST_DATO)
            .medRevurderingVedtakDato(REVURDERING_VEDTAK_DATO)
            .medMetadata(metadata)
            .build();

        String generertBrev = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);

        String fasit = les("/varselbrev/nn/SVP_flere_perioder.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_varseltekst_for_engangsstønad() throws IOException {
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medFagsaktype(engangsstønadkode)
            .medSprakkode(Språkkode.nb)
            .medFagsaktypenavnPåSpråk("eingongsstønad")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedKunEnPeriode())
            .medFristdato(FRIST_DATO)
            .medRevurderingVedtakDato(REVURDERING_VEDTAK_DATO)
            .medMetadata(metadata)
            .build();

        String generertBrev = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);

        String fasit = les("/varselbrev/nb/ES.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_varseltekst_for_foreldrepenger_med_enkelt_periode() throws IOException {
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medFagsaktype(foreldrepengerkode)
            .medSprakkode(Språkkode.nb)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedKunEnPeriode())
            .medFristdato(FRIST_DATO)
            .medRevurderingVedtakDato(REVURDERING_VEDTAK_DATO)
            .medMetadata(metadata)
            .build();

        String generertBrev = TekstformatererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);

        String fasit = les("/varselbrev/nb/FP_en_periode.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_mappe_verdier_fra_dtoer_til_komplett_tilbakekrevingsvarsel() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medSprakkode(Språkkode.nn)
            .medFagsaktype(foreldrepengerkode)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedKunEnPeriode())
            .medFristdato(LocalDate.of(2018, 5, 27))
            .medRevurderingVedtakDato(LocalDate.of(2018, 5, 6))
            .medMetadata(brevMetadata)
            .build();

        VarselbrevDokument varselbrev = TekstformatererVarselbrev.mapTilVarselbrevDokument(varselbrevSamletInfo);

        assertThat(varselbrev.getEndringsdato()).isEqualTo(LocalDate.of(2018, 5, 6));
        assertThat(varselbrev.getFristdatoForTilbakemelding()).isEqualTo(LocalDate.of(2018, 5, 27));
        assertThat(varselbrev.getVarseltekstFraSaksbehandler()).isEqualTo("Dette er fritekst skrevet av saksbehandler.");
        assertThat(varselbrev.getDatoerHvisSammenhengendePeriode().getFom()).isEqualTo(LocalDate.of(2019, 3, 3));
        assertThat(varselbrev.getDatoerHvisSammenhengendePeriode().getTom()).isEqualTo(LocalDate.of(2020, 3, 3));
        assertThat(varselbrev.getFagsaktypeNavn()).isEqualTo("foreldrepenger");
        assertThat(varselbrev.getBeløp()).isEqualTo(595959L);
        assertThat(varselbrev.getFeilutbetaltePerioder()).isNotNull();
    }

    @Test
    public void skal_ikke_sette_tidligste_og_seneste_dato_når_det_foreligger_flere_perioder() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(foreldrepengerkode)
            .medSprakkode(Språkkode.en)
            .medFagsaktypenavnPåSpråk("foreldrepengar")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedFlerePerioder())
            .medSumFeilutbetaling(595959L)
            .medFristdato(LocalDate.of(2020, 5, 5))
            .medRevurderingVedtakDato(LocalDate.now())
            .medMetadata(brevMetadata)
            .build();

        VarselbrevDokument varselbrev = TekstformatererVarselbrev.mapTilVarselbrevDokument(varselbrevSamletInfo);
        assertThat(varselbrev.getDatoerHvisSammenhengendePeriode()).isNull();
    }

    public List<Periode> mockFeilutbetalingerMedKunEnPeriode() {
        return List.of(new Periode(LocalDate.of(2019, 3, 3),
            LocalDate.of(2020, 3, 3)));
    }

    @Test
    public void skal_kontrollere_at_alle_nødvendige_verdier_for_komplett_brev_er_satt_for_engangsstønad() {
        VarselbrevDokument varselbrevDokument = lagTilbakekrevingvarselMedObligatoriskeVerdier();
        varselbrevDokument.setDatoerHvisSammenhengendePeriode(new Periode(LocalDate.of(2019, 12, 12), LocalDate.of(2020, 1, 1)));
        varselbrevDokument.setFeilutbetaltePerioder(mockUtbetalingEnPeriode());
        varselbrevDokument.setFagsaktypeNavn("engangsstønad");
        varselbrevDokument.setEngangsstønad(true);
        varselbrevDokument.valider();
    }

    @Test
    public void skal_kaste_feil_dersom_ikke_nødvendige_verdier_for_komplett_brev_er_satt_for_engangsstønad() {
        VarselbrevDokument varselbrevDokument = lagTilbakekrevingvarselMedObligatoriskeVerdier();
        varselbrevDokument.setEngangsstønad(true);
        varselbrevDokument.setFeilutbetaltePerioder(mockUtbetalingEnPeriode());
        varselbrevDokument.setFagsaktypeNavn("engangsstønad");
        varselbrevDokument.setDatoerHvisSammenhengendePeriode(null);
        Assertions.assertThatNullPointerException().isThrownBy(varselbrevDokument::valider);
    }

    @Test
    public void skal_kontrollere_at_alle_nødvendige_verdier_for_komplett_brev_er_satt_for_foreldrepenger_sammenhengende_periode() {
        VarselbrevDokument varselbrevDokument = lagTilbakekrevingvarselMedObligatoriskeVerdier();
        varselbrevDokument.setFeilutbetaltePerioder(mockUtbetalingEnPeriode());
        varselbrevDokument.setFagsaktypeNavn("foreldrepenger");
        varselbrevDokument.setForeldrepenger(true);
        varselbrevDokument.setDatoerHvisSammenhengendePeriode(null);
        Assertions.assertThatNullPointerException().isThrownBy(varselbrevDokument::valider);
    }

    @Test
    public void skal_generere_varselbrev_overskrift() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .build();

        String overskrift = TekstformatererVarselbrev.lagVarselbrevOverskrift(brevMetadata);
        String fasit = "NAV vurderer om du må betale tilbake foreldrepenger";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_varselbrev_overskrift_nynorsk() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepengar")
            .medSprakkode(Språkkode.nb)
            .build();

        String overskrift = TekstformatererVarselbrev.lagVarselbrevOverskrift(brevMetadata);
        String fasit = "NAV vurderer om du må betale tilbake foreldrepengar";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_korrigert_varselbrev_overskrift() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .medSprakkode(Språkkode.nb)
            .build();

        String overskrift = TekstformatererVarselbrev.lagKorrigertVarselbrevOverskrift(brevMetadata);
        String fasit = "Korrigert varsel om feilutbetalte foreldrepenger";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_korrigert_varselbrev_overskrift_nynorsk() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktypenavnPåSpråk("foreldrepengar")
            .medSprakkode(Språkkode.nn)
            .build();

        String overskrift = TekstformatererVarselbrev.lagKorrigertVarselbrevOverskrift(brevMetadata);
        String fasit = "Korrigert varsel om feilutbetalte foreldrepengar";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_korrigert_varselbrev_overskrift_engangstønad() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.ENGANGSTØNAD)
            .medFagsaktypenavnPåSpråk("engangstønad")
            .medSprakkode(Språkkode.nb)
            .build();

        String overskrift = TekstformatererVarselbrev.lagKorrigertVarselbrevOverskrift(brevMetadata);
        String fasit = "Korrigert varsel om feilutbetalt engangstønad";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_korrigert_varselbrev_overskrift_engangstønad_nynorsk() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(FagsakYtelseType.ENGANGSTØNAD)
            .medFagsaktypenavnPåSpråk("eingongstønad")
            .medSprakkode(Språkkode.nn)
            .build();

        String overskrift = TekstformatererVarselbrev.lagKorrigertVarselbrevOverskrift(brevMetadata);
        String fasit = "Korrigert varsel om feilutbetalt eingongstønad";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    private List<Periode> mockFeilutbetalingerMedFlerePerioder() {
        Periode periode1 = new Periode(LocalDate.of(2019, 3, 3), LocalDate.of(2020, 3, 3));
        Periode periode2 = new Periode(LocalDate.of(2022, 3, 3), LocalDate.of(2024, 3, 3));
        return List.of(periode1, periode2);
    }

    private VarselbrevDokument lagTilbakekrevingvarselMedObligatoriskeVerdier() {
        VarselbrevDokument varselbrevDokument = new VarselbrevDokument();
        varselbrevDokument.setBeløp(40L);
        varselbrevDokument.setEndringsdato(LocalDate.of(2020, 1, 30));
        varselbrevDokument.setFristdatoForTilbakemelding(LocalDate.of(2022, 2, 2));
        return varselbrevDokument;
    }

    private List<Periode> mockUtbetalingEnPeriode() {
        return List.of(new Periode(JANUAR_1_2019, JANUAR_30_2019));
    }

    private String les(String filnavn) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(filnavn);
             Scanner scanner = new Scanner(resource, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
    }

}
