package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler.DokumentBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevTask;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.InnhentDokumentasjonbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.ManueltVarselBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.SendManueltVarselbrevTask;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

class DokumentBehandlingTjenesteTest extends DokumentBestillerTestOppsett {

    private ProsessTaskTjeneste taskTjeneste;

    private ManueltVarselBrevTjeneste mockManueltVarselBrevTjeneste = mock(ManueltVarselBrevTjeneste.class);
    private InnhentDokumentasjonbrevTjeneste mockInnhentDokumentasjonbrevTjeneste = mock(InnhentDokumentasjonbrevTjeneste.class);

    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste;

    @BeforeEach
    void setup() {
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        var historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, null);
        dokumentBehandlingTjeneste = new DokumentBehandlingTjeneste(repositoryProvider, taskTjeneste, historikkinnslagTjeneste,
                mockManueltVarselBrevTjeneste, mockInnhentDokumentasjonbrevTjeneste);
    }

    @Test
    void skal_henteBrevMal_for_behandling_som_har_ikke_sendt_varsel() {
        var brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.INNHENT_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.VARSEL_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.KORRIGERT_VARSEL_DOK)).isEmpty();
    }

    @Test
    void skal_henteBrevMal_for_behandling_som_har_sendt_varsel() {
        lagreInfoOmVarselbrev(behandling.getId(), "123", "1243");

        var brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.INNHENT_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.VARSEL_DOK)).isEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.KORRIGERT_VARSEL_DOK)).isPresent();
    }

    @Test
    void skal_henteBrevMal_for_behandling_som_har_avsluttet() {
        behandling.avsluttBehandling();

        var brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        var innhentBrevMal = hentSpesifiskBrevMal(brevMaler, DokumentMalType.INNHENT_DOK);
        assertThat(innhentBrevMal).isPresent();
        assertThat(innhentBrevMal.get().tilgjengelig()).isTrue();

        var fritekstBrevMal = hentSpesifiskBrevMal(brevMaler, DokumentMalType.FRITEKST_DOK);
        assertThat(fritekstBrevMal).isNotPresent();

        var varselBrevMal = hentSpesifiskBrevMal(brevMaler, DokumentMalType.VARSEL_DOK);
        assertThat(varselBrevMal).isPresent();
        assertThat(varselBrevMal.get().tilgjengelig()).isFalse();

        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.kode().equals(DokumentMalType.KORRIGERT_VARSEL_DOK)).findFirst()).isEmpty();
    }

    @Test
    void skal_kunne_bestille_varselbrev_når_grunnlag_finnes() {
        var behandlingId = opprettOgLagreKravgrunnlagPåBehandling();

        dokumentBehandlingTjeneste.bestillBrev(behandlingId, DokumentMalType.VARSEL_DOK, "Bestilt varselbrev");

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosesser = captor.getValue();
        assertThat(prosesser.getTaskType()).isEqualTo(ProsessTaskData.forProsessTask(SendManueltVarselbrevTask.class).getTaskType());

        var historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(1);
        var historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_BESTILT);
    }

    @Test
    void skal_ikke_kunne_bestille_varselbrev_når_grunnlag_ikke_finnes() {
        assertThatThrownBy(() -> dokumentBehandlingTjeneste.bestillBrev(behandling.getId(), DokumentMalType.VARSEL_DOK, "Bestilt varselbrev"))
                .hasMessageContaining("FPT-612900");
    }

    @Test
    void skal_kunne_bestille_innhent_dokumentasjon_brev_når_grunnlag_finnes() {
        var behandlingId = opprettOgLagreKravgrunnlagPåBehandling();

        dokumentBehandlingTjeneste.bestillBrev(behandlingId, DokumentMalType.INNHENT_DOK, "Bestilt innhent dokumentasjon");

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosesser = captor.getAllValues();
        assertThat(prosesser.stream().filter(t -> TaskType.forProsessTask(InnhentDokumentasjonbrevTask.class).equals(t.taskType())).collect(Collectors.toList())).isNotEmpty();
        var historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(1);
        var historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_BESTILT);
    }

    @Test
    void skal_ikke_kunne_bestille_innhent_dokumentasjonbrev_når_grunnlag_ikke_finnes() {
        assertThatThrownBy(() -> dokumentBehandlingTjeneste.bestillBrev(behandling.getId(), DokumentMalType.INNHENT_DOK, "Bestilt innhent dokumentasjon"))
                .hasMessageContaining("FPT-612901");
    }

    private Long opprettOgLagreKravgrunnlagPåBehandling() {
        var kravgrunnlag431 = Kravgrunnlag431.builder().medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
                .medVedtakId(12342l)
                .medEksternKravgrunnlagId("1234")
                .medKravStatusKode(KravStatusKode.NYTT)
                .medFagSystemId("1234")
                .medUtbetalesTilId("11323432111")
                .medUtbetIdType(GjelderType.PERSON)
                .medGjelderVedtakId("11323432111")
                .medGjelderType(GjelderType.PERSON)
                .medAnsvarligEnhet("enhet")
                .medBostedEnhet("enhet")
                .medBehandlendeEnhet("enhet")
                .medFeltKontroll("132323")
                .medSaksBehId("23454334").build();
        var periode = KravgrunnlagPeriode432.builder()
                .medPeriode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 31))
                .medKravgrunnlag431(kravgrunnlag431).build();
        var ytelBeløp = KravgrunnlagBelop433.builder().medKlasseType(KlasseType.YTEL)
                .medKlasseKode(KlasseKode.FPADATORD)
                .medNyBelop(BigDecimal.ZERO)
                .medTilbakekrevesBelop(BigDecimal.valueOf(1000))
                .medOpprUtbetBelop(BigDecimal.valueOf(1000))
                .medKravgrunnlagPeriode432(periode).build();

        var feilBeløp = KravgrunnlagBelop433.builder().medKlasseType(KlasseType.FEIL)
                .medKlasseKode(KlasseKode.FPADATORD)
                .medNyBelop(BigDecimal.valueOf(1000))
                .medTilbakekrevesBelop(BigDecimal.ZERO)
                .medOpprUtbetBelop(BigDecimal.ZERO)
                .medKravgrunnlagPeriode432(periode).build();
        periode.leggTilBeløp(ytelBeløp);
        periode.leggTilBeløp(feilBeløp);
        kravgrunnlag431.leggTilPeriode(periode);

        var behandlingId = behandling.getId();
        repositoryProvider.getGrunnlagRepository().lagre(behandlingId, kravgrunnlag431);
        return behandlingId;
    }

    private Optional<BrevmalDto> hentSpesifiskBrevMal(List<BrevmalDto> brevMaler, DokumentMalType malType) {
        return brevMaler.stream().filter(brevmalDto -> malType.getKode().equals(brevmalDto.kode())).findFirst();
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, String journalpostId, String dokumentId) {
        var brevSporing = new BrevSporing.Builder()
                .medBehandlingId(behandlingId)
                .medDokumentId(dokumentId)
                .medJournalpostId(new JournalpostId(journalpostId))
                .medBrevType(BrevType.VARSEL_BREV)
                .build();
        brevSporingRepository.lagre(brevSporing);
        entityManager.flush();
    }

}
