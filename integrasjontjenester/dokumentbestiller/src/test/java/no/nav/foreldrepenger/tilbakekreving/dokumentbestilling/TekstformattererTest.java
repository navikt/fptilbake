package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.PeriodeMedBrevtekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VedtaksbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.vedtak.util.FPDateUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TekstformattererTest {

    private KodeDto foreldrepengerkode = new KodeDto("FAGSAK_YTELSE", "FP", "Foreldrepenger");
    private KodeDto engangsstønadkode = new KodeDto("FAGSAK_YTELSE", "ES", "Engangsstønad");
    private KodeDto svangerskapspengerkode = new KodeDto("FAGSAK_YTELSE", "SVP", "Svangerskapspenger");

    private final LocalDate JANUAR_1_2019 = LocalDate.of(2019, 1, 1);
    private final LocalDate JANUAR_30_2019 = LocalDate.of(2019, 1, 30);
    private final LocalDate FEBRUAR_1_2019 = LocalDate.of(2019, 2, 1);
    private final LocalDate FEBRUAR_15_2019 = LocalDate.of(2019, 2, 15);
    private final LocalDate FEBRUAR_20_2018 = LocalDate.of(2018, 2, 20);

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

        String varselbrevMedFlerePerioder = Tekstformatterer.lagVarselbrevFritekst(varselbrevSamletInfo);
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

        String varselbrevForEngangsstønad = Tekstformatterer.lagVarselbrevFritekst(varselbrevSamletInfo);
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

        String varselbrevForForeldrepenger = Tekstformatterer.lagVarselbrevFritekst(varselbrevSamletInfo);
        System.out.println(varselbrevForForeldrepenger);
    }

    @Test
    public void skal_generere_brødtekst_i_vedtaksbrev() {
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medFagsaktype(svangerskapspengerkode)
            .medSprakkode(Språkkode.nb)
            .medFagsaktypenavnPåSpråk("eingongsstønad")
            .build();

        VedtaksbrevSamletInfo vedtaksbrevSamletInfo = new VedtaksbrevSamletInfo.Builder()
            .medSumFeilutbetaling(595959L)
            .medSumBeløpSomSkalTilbakekreves(777L)
            .medVarselbrevSendtUt(LocalDate.of(2020, 4, 4))
            .medBrevMetadata(brevMetadata)
            .medPerioderMedBrevtekst(lagPerioderMedTekst())
            .build();

        String varselbrevMedFlerePerioder = Tekstformatterer.lagVedtaksbrevFritekst(vedtaksbrevSamletInfo);
        System.out.println(varselbrevMedFlerePerioder);
    }

    private List<PeriodeMedBrevtekst> lagPerioderMedTekst() {
        PeriodeMedBrevtekst periode1 = new PeriodeMedBrevtekst.Builder()
            .medFom(JANUAR_1_2019)
            .medTom(JANUAR_30_2019)
            .medGenerertFaktaAvsnitt("Personen har handlet i god tro. ")
            .medGenerertVilkårAvsnitt("vilkår her")
            .medGenerertSærligeGrunnerAvsnitt("særlige grunner her")
            .medFritekstFakta("fritekst fakta")
            .medFritekstVilkår("fritekst vilkår")
            .build();
        PeriodeMedBrevtekst periode2 = new PeriodeMedBrevtekst.Builder()
            .medFom(FEBRUAR_1_2019)
            .medTom(FEBRUAR_15_2019)
            .medGenerertFaktaAvsnitt("Personen har handlet i god tro. ")
            .medGenerertVilkårAvsnitt("vilkår her")
            .medGenerertSærligeGrunnerAvsnitt("særlige grunner her")
            .medFritekstFakta("fritekst fakta")
            .medFritekstSærligeGrunner("fritekst særlige grunner")
            .build();
        return Arrays.asList(periode1, periode2);
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
        VarselbrevDokument varselbrev = Tekstformatterer.mapTilVarselbrevDokument(varselbrevSamletInfo, dagensDato);

        Assertions.assertThat(varselbrev.getEndringsdato()).isEqualTo(LocalDate.of(2018, 5, 6));
        Assertions.assertThat(varselbrev.getFristdatoForTilbakemelding()).isEqualTo(LocalDate.of(2018, 5, 27));
        Assertions.assertThat(varselbrev.getVarseltekstFraSaksbehandler()).isEqualTo("Dette er fritekst skrevet av saksbehandler.");
        Assertions.assertThat(varselbrev.getDatoerHvisSammenhengendePeriode().getFom()).isEqualTo(LocalDate.of(2019, 3, 3));
        Assertions.assertThat(varselbrev.getDatoerHvisSammenhengendePeriode().getTom()).isEqualTo(LocalDate.of(2020, 3, 3));
        Assertions.assertThat(varselbrev.getFagsaktypeNavn()).isEqualTo("foreldrepenger");
        Assertions.assertThat(varselbrev.getBelop()).isEqualTo(595959L);
        Assertions.assertThat(varselbrev.getFeilutbetaltePerioder()).isNotNull();
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

        VarselbrevDokument varselbrev = Tekstformatterer.mapTilVarselbrevDokument(varselbrevSamletInfo, FPDateUtil.nå());
        Assertions.assertThat(varselbrev.getDatoerHvisSammenhengendePeriode()).isNull();
    }

    private List<Periode> mockFeilutbetalingerMedFlerePerioder() {
        Periode periode1 = new Periode(LocalDate.of(2019, 3, 3), LocalDate.of(2020, 3, 3));
        Periode periode2 = new Periode(LocalDate.of(2022, 3, 3), LocalDate.of(2024, 3, 3));
        return List.of(periode1, periode2);
    }

    @Test
    public void skal_finne_riktig_språk() {
        Assertions.assertThat(Tekstformatterer.finnRiktigSpråk(Språkkode.en)).isEqualTo(BaseDokument.Lokale.BOKMÅL);
        Assertions.assertThat(Tekstformatterer.finnRiktigSpråk(Språkkode.nn)).isEqualTo(BaseDokument.Lokale.NYNORSK);
        Assertions.assertThat(Tekstformatterer.finnRiktigSpråk(Språkkode.nb)).isEqualTo(BaseDokument.Lokale.BOKMÅL);
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

    private List<Periode> mockUtbetalingFlerePerioderMedManglendeFomDato() {
        Periode periode1 = new Periode(FEBRUAR_1_2019, null);
        Periode periode2 = new Periode(FEBRUAR_15_2019, FEBRUAR_20_2018);
        return List.of(periode1, periode2);
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
