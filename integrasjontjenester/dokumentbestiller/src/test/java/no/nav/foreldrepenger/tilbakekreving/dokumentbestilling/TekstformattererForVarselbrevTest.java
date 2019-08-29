package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.vedtak.util.FPDateUtil;

public class TekstformattererForVarselbrevTest {

    private KodeDto foreldrepengerkode = new KodeDto("FAGSAK_YTELSE", "FP", "Foreldrepenger");
    private KodeDto engangsstønadkode = new KodeDto("FAGSAK_YTELSE", "ES", "Engangsstønad");
    private KodeDto svangerskapspengerkode = new KodeDto("FAGSAK_YTELSE", "SVP", "Svangerskapspenger");

    private final LocalDate JANUAR_1_2019 = LocalDate.of(2019, 1, 1);
    private final LocalDate JANUAR_30_2019 = LocalDate.of(2019, 1, 30);

    @Test
    public void skal_generere_varseltekst_for_flere_perioder() {
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medFagsaktype(svangerskapspengerkode)
            .medSprakkode(Språkkode.nn)
            .medFagsaktypenavnPåSpråk("svangerskapspengar")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedFlerePerioder())
            .medFristdato(LocalDate.of(2020, 4, 4))
            .medMetadata(metadata)
            .build();

        String varselbrevMedFlerePerioder = TekstformattererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        System.out.println(varselbrevMedFlerePerioder);
    }


    @Test
    public void skal_generere_varseltekst_for_engangsstønad() {
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medFagsaktype(engangsstønadkode)
            .medSprakkode(Språkkode.nb)
            .medFagsaktypenavnPåSpråk("eingongsstønad")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedKunEnPeriode())
            .medFristdato(LocalDate.of(2020, 4, 4))
            .medMetadata(metadata)
            .build();

        String varselbrevForEngangsstønad = TekstformattererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        System.out.println(varselbrevForEngangsstønad);
    }

    @Test
    public void skal_generere_varseltekst_for_foreldrepenger_med_enkelt_periode() {
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medFagsaktype(foreldrepengerkode)
            .medSprakkode(Språkkode.nb)
            .medFagsaktypenavnPåSpråk("foreldrepenger")
            .build();

        VarselbrevSamletInfo varselbrevSamletInfo = new VarselbrevSamletInfo.Builder()
            .medFritekstFraSaksbehandler("Dette er fritekst skrevet av saksbehandler.")
            .medSumFeilutbetaling(595959L)
            .medFeilutbetaltePerioder(mockFeilutbetalingerMedKunEnPeriode())
            .medFristdato(LocalDate.of(2020, 4, 4))
            .medMetadata(metadata)
            .build();

        String varselbrevForForeldrepenger = TekstformattererVarselbrev.lagVarselbrevFritekst(varselbrevSamletInfo);
        System.out.println(varselbrevForForeldrepenger);
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
            .medMetadata(brevMetadata)
            .build();

        LocalDateTime dagensDato = LocalDateTime.of(2018, 5, 6, 1, 1);
        VarselbrevDokument varselbrev = TekstformattererVarselbrev.mapTilVarselbrevDokument(varselbrevSamletInfo, dagensDato);

        assertThat(varselbrev.getEndringsdato()).isEqualTo(LocalDate.of(2018, 5, 6));
        assertThat(varselbrev.getFristdatoForTilbakemelding()).isEqualTo(LocalDate.of(2018, 5, 27));
        assertThat(varselbrev.getVarseltekstFraSaksbehandler()).isEqualTo("Dette er fritekst skrevet av saksbehandler.");
        assertThat(varselbrev.getDatoerHvisSammenhengendePeriode().getFom()).isEqualTo(LocalDate.of(2019, 3, 3));
        assertThat(varselbrev.getDatoerHvisSammenhengendePeriode().getTom()).isEqualTo(LocalDate.of(2020, 3, 3));
        assertThat(varselbrev.getFagsaktypeNavn()).isEqualTo("foreldrepenger");
        assertThat(varselbrev.getBelop()).isEqualTo(595959L);
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
            .medMetadata(brevMetadata)
            .build();

        VarselbrevDokument varselbrev = TekstformattererVarselbrev.mapTilVarselbrevDokument(varselbrevSamletInfo, FPDateUtil.nå());
        assertThat(varselbrev.getDatoerHvisSammenhengendePeriode()).isNull();
    }

    private List<Periode> mockFeilutbetalingerMedFlerePerioder() {
        Periode periode1 = new Periode(LocalDate.of(2019, 3, 3), LocalDate.of(2020, 3, 3));
        Periode periode2 = new Periode(LocalDate.of(2022, 3, 3), LocalDate.of(2024, 3, 3));
        return List.of(periode1, periode2);
    }

    @Test
    public void skal_finne_riktig_språk() {
        assertThat(TekstformattererVarselbrev.finnRiktigSpråk(Språkkode.en)).isEqualTo(BaseDokument.Lokale.BOKMÅL);
        assertThat(TekstformattererVarselbrev.finnRiktigSpråk(Språkkode.nn)).isEqualTo(BaseDokument.Lokale.NYNORSK);
        assertThat(TekstformattererVarselbrev.finnRiktigSpråk(Språkkode.nb)).isEqualTo(BaseDokument.Lokale.BOKMÅL);
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
        varselbrevDokument.setEngangsstonad(true);
        varselbrevDokument.valider();
    }

    @Test
    public void skal_kaste_feil_dersom_ikke_nødvendige_verdier_for_komplett_brev_er_satt_for_engangsstønad() {
        VarselbrevDokument varselbrevDokument = lagTilbakekrevingvarselMedObligatoriskeVerdier();
        varselbrevDokument.setEngangsstonad(true);
        varselbrevDokument.setFeilutbetaltePerioder(mockUtbetalingEnPeriode());
        varselbrevDokument.setFagsaktypeNavn("engangsstønad");
        varselbrevDokument.setDatoerHvisSammenhengendePeriode(null);
        Assertions.assertThatNullPointerException().isThrownBy(() -> {
            varselbrevDokument.valider();
        });
    }

    private VarselbrevDokument lagTilbakekrevingvarselMedObligatoriskeVerdier() {
        VarselbrevDokument varselbrevDokument = new VarselbrevDokument();
        varselbrevDokument.setBelop(40L);
        varselbrevDokument.setEndringsdato(LocalDate.of(2020, 1, 30));
        varselbrevDokument.setFristdatoForTilbakemelding(LocalDate.of(2022, 2, 2));
        varselbrevDokument.setKontakttelefonnummer("99 99 99 99");
        return varselbrevDokument;
    }

    private List<Periode> mockUtbetalingEnPeriode() {
        return List.of(new Periode(JANUAR_1_2019, JANUAR_30_2019));
    }

    @Test
    public void skal_kontrollere_at_alle_nødvendige_verdier_for_komplett_brev_er_satt_for_foreldrepenger_sammenhengende_periode() {
        VarselbrevDokument varselbrevDokument = lagTilbakekrevingvarselMedObligatoriskeVerdier();
        varselbrevDokument.setFeilutbetaltePerioder(mockUtbetalingEnPeriode());
        varselbrevDokument.setFagsaktypeNavn("foreldrepenger");
        varselbrevDokument.setForeldrepenger(true);
        varselbrevDokument.setDatoerHvisSammenhengendePeriode(null);
        Assertions.assertThatNullPointerException().isThrownBy(() -> {
            varselbrevDokument.valider();
        });
    }
}
