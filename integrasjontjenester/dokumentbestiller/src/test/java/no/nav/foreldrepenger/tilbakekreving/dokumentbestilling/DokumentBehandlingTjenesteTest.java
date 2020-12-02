package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
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
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendBeskjedUtsendtVarselTilSelvbetjeningTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class DokumentBehandlingTjenesteTest extends DokumentBestillerTestOppsett {

    private ProsessTaskRepository prosessTaskRepository;

    private ManueltVarselBrevTjeneste mockManueltVarselBrevTjeneste = mock(ManueltVarselBrevTjeneste.class);
    private InnhentDokumentasjonbrevTjeneste mockInnhentDokumentasjonbrevTjeneste = mock(InnhentDokumentasjonbrevTjeneste.class);

    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste;

    @Before
    public void setup() {
        prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, null);
        dokumentBehandlingTjeneste = new DokumentBehandlingTjeneste(repositoryProvider, prosessTaskRepository, historikkinnslagTjeneste,
            mockManueltVarselBrevTjeneste, mockInnhentDokumentasjonbrevTjeneste);
    }

    @Test
    public void skal_henteBrevMal_for_behandling_som_har_ikke_sendt_varsel() {
        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.INNHENT_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.VARSEL_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.KORRIGERT_VARSEL_DOK)).isEmpty();
    }

    @Test
    public void skal_henteBrevMal_for_behandling_som_har_sendt_varsel() {
        lagreInfoOmVarselbrev(behandling.getId(), "123", "1243");

        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.INNHENT_DOK)).isPresent();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.VARSEL_DOK)).isEmpty();
        assertThat(hentSpesifiskBrevMal(brevMaler, DokumentMalType.KORRIGERT_VARSEL_DOK)).isPresent();
    }

    @Test
    public void skal_henteBrevMal_for_behandling_som_har_avsluttet() {
        behandling.avsluttBehandling();

        List<BrevmalDto> brevMaler = dokumentBehandlingTjeneste.hentBrevmalerFor(behandling.getId());

        assertThat(brevMaler).isNotEmpty();
        Optional<BrevmalDto> innhentBrevMal = hentSpesifiskBrevMal(brevMaler, DokumentMalType.INNHENT_DOK);
        assertThat(innhentBrevMal).isPresent();
        assertThat(innhentBrevMal.get().getTilgjengelig()).isTrue();

        Optional<BrevmalDto> fritekstBrevMal = hentSpesifiskBrevMal(brevMaler, DokumentMalType.FRITEKST_DOK);
        assertThat(fritekstBrevMal).isNotPresent();

        Optional<BrevmalDto> varselBrevMal = hentSpesifiskBrevMal(brevMaler, DokumentMalType.VARSEL_DOK);
        assertThat(varselBrevMal).isPresent();
        assertThat(varselBrevMal.get().getTilgjengelig()).isFalse();

        assertThat(brevMaler.stream().filter(brevmalDto -> brevmalDto.getKode().equals(DokumentMalType.KORRIGERT_VARSEL_DOK)).findFirst()).isEmpty();
    }

    @Test
    public void skal_kunne_bestille_varselbrev_når_grunnlag_finnes() {
        Long behandlingId = opprettOgLagreKravgrunnlagPåBehandling();

        dokumentBehandlingTjeneste.bestillBrev(behandlingId, DokumentMalType.VARSEL_DOK, "Bestilt varselbrev");

        assertThat(prosessTaskRepository.finnProsessTaskType(SendManueltVarselbrevTask.TASKTYPE)).isPresent();
        assertThat(prosessTaskRepository.finnProsessTaskType(SendBeskjedUtsendtVarselTilSelvbetjeningTask.TASKTYPE)).isPresent();
        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_BESTILT);
    }

    @Test
    public void skal_ikke_kunne_bestille_varselbrev_når_grunnlag_ikke_finnes() {
        expectedException.expectMessage("FPT-612900");
        dokumentBehandlingTjeneste.bestillBrev(behandling.getId(), DokumentMalType.VARSEL_DOK, "Bestilt varselbrev");
    }

    @Test
    public void skal_kunne_bestille_innhent_dokumentasjon_brev_når_grunnlag_finnes() {
        Long behandlingId = opprettOgLagreKravgrunnlagPåBehandling();

        dokumentBehandlingTjeneste.bestillBrev(behandlingId, DokumentMalType.INNHENT_DOK, "Bestilt innhent dokumentasjon");

        assertThat(prosessTaskRepository.finnProsessTaskType(InnhentDokumentasjonbrevTask.TASKTYPE)).isPresent();
        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_BESTILT);
    }

    @Test
    public void skal_ikke_kunne_bestille_innhent_dokumentasjonbrev_når_grunnlag_ikke_finnes() {
        expectedException.expectMessage("FPT-612901");
        dokumentBehandlingTjeneste.bestillBrev(behandling.getId(), DokumentMalType.INNHENT_DOK, "Bestilt innhent dokumentasjon");
    }

    private Long opprettOgLagreKravgrunnlagPåBehandling() {
        Kravgrunnlag431 kravgrunnlag431 = Kravgrunnlag431.builder().medFagomraadeKode(FagOmrådeKode.FORELDREPENGER)
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
        KravgrunnlagPeriode432 periode = KravgrunnlagPeriode432.builder()
            .medPeriode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 31))
            .medKravgrunnlag431(kravgrunnlag431).build();
        KravgrunnlagBelop433 ytelBeløp = KravgrunnlagBelop433.builder().medKlasseType(KlasseType.YTEL)
            .medKlasseKode(KlasseKode.FPADATORD)
            .medNyBelop(BigDecimal.ZERO)
            .medTilbakekrevesBelop(BigDecimal.valueOf(1000))
            .medOpprUtbetBelop(BigDecimal.valueOf(1000))
            .medKravgrunnlagPeriode432(periode).build();

        KravgrunnlagBelop433 feilBeløp = KravgrunnlagBelop433.builder().medKlasseType(KlasseType.FEIL)
            .medKlasseKode(KlasseKode.FPADATORD)
            .medNyBelop(BigDecimal.valueOf(1000))
            .medTilbakekrevesBelop(BigDecimal.ZERO)
            .medOpprUtbetBelop(BigDecimal.ZERO)
            .medKravgrunnlagPeriode432(periode).build();
        periode.leggTilBeløp(ytelBeløp);
        periode.leggTilBeløp(feilBeløp);
        kravgrunnlag431.leggTilPeriode(periode);

        Long behandlingId = behandling.getId();
        repositoryProvider.getGrunnlagRepository().lagre(behandlingId, kravgrunnlag431);
        return behandlingId;
    }

    private Optional<BrevmalDto> hentSpesifiskBrevMal(List<BrevmalDto> brevMaler, DokumentMalType malType) {
        return brevMaler.stream().filter(brevmalDto -> malType.getKode().equals(brevmalDto.getKode())).findFirst();
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, String journalpostId, String dokumentId) {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentId)
            .medJournalpostId(new JournalpostId(journalpostId))
            .medBrevType(BrevType.VARSEL_BREV)
            .build();
        brevSporingRepository.lagre(brevSporing);
        repositoryRule.getEntityManager().flush();
    }

}
