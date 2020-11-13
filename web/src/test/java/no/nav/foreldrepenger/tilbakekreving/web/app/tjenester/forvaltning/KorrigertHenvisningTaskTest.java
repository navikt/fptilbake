package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
class KorrigertHenvisningTaskTest {

    private BehandlingRepositoryProvider repositoryProvider;
    private EksternBehandlingRepository eksternBehandlingRepository;

    private FagsystemKlient mockFagsystemKlient = Mockito.mock(FagsystemKlient.class);
    private KorrigertHenvisningTask korrigertHenvisningTask;
    private Behandling behandling;
    private UUID uuid = UUID.randomUUID();

    @BeforeEach
    void setUp(EntityManager entityManager){
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        korrigertHenvisningTask = new KorrigertHenvisningTask(repositoryProvider, mockFagsystemKlient);
    }

    @Test
    void skal_utføre_korrigert_henvisning_task(){
        when(mockFagsystemKlient.hentBehandlingsinfoOpt(any(UUID.class), any(Tillegsinformasjon.class)))
            .thenReturn(Optional.of(lagEksternBehandlingInfo(behandling.getFagsak().getSaksnummer())));
        korrigertHenvisningTask.doTask(lagProsessTaskData());

        assertTrue(eksternBehandlingRepository.finnesAktivtEksternBehandling(behandling.getId()));
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getHenvisning()).isNotNull().isEqualTo(Henvisning.fraEksternBehandlingId(123l));
    }

    @Test
     void skal_ikke_utføre_korrigert_henvisning_task_når_ekstern_behandling_uuid_ikke_finnes(){
        when(mockFagsystemKlient.hentBehandlingsinfoOpt(any(UUID.class), any(Tillegsinformasjon.class)))
            .thenReturn(Optional.empty());
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(TekniskException.class, () -> korrigertHenvisningTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-7728492");
    }

    @Test
    void skal_ikke_utføre_korrigert_henvisning_task_når_ekstern_behandling_uuid_ikke_har_samme_saksnummer(){
        when(mockFagsystemKlient.hentBehandlingsinfoOpt(any(UUID.class), any(Tillegsinformasjon.class)))
            .thenReturn(Optional.of(lagEksternBehandlingInfo(new Saksnummer("1245454"))));
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        var e = assertThrows(TekniskException.class, () -> korrigertHenvisningTask.doTask(prosessTaskData));
        assertThat(e.getMessage()).contains("FPT-7728493");
    }

    private ProsessTaskData lagProsessTaskData(){
        ProsessTaskData prosessTaskData = new ProsessTaskData(KorrigertHenvisningTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(KorrigertHenvisningTask.PROPERTY_EKSTERN_UUID, uuid.toString());
        return prosessTaskData;
    }

    private SamletEksternBehandlingInfo lagEksternBehandlingInfo(Saksnummer saksnummer){
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(123l));
        eksternBehandlingsinfoDto.setUuid(uuid);
        FagsakDto fagsakDto = new FagsakDto();
        fagsakDto.setAktoerId(behandling.getAktørId().getId());
        fagsakDto.setSaksnummer(saksnummer.getVerdi());
        fagsakDto.setSakstype(FagsakYtelseType.FORELDREPENGER);
        SamletEksternBehandlingInfo eksternBehandlingInfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.FAGSAK)
            .setGrunninformasjon(eksternBehandlingsinfoDto)
            .setFagsak(fagsakDto).build();
        return eksternBehandlingInfo;
    }
}
