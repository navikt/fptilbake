package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskTjenesteImpl;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class MigrasjonRestTjenesteTest {

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private MigrasjonRestTjeneste migrasjonRestTjeneste;

    private Behandling behandling;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        entityManager.setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandling = scenarioSimple.lagre(repositoryProvider);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        EksternBehandlingRepository eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        eksternBehandlingRepository.lagre(eksternBehandling);
        økonomiMottattXmlRepository = new ØkonomiMottattXmlRepository(entityManager);
        taskTjeneste = new ProsessTaskTjenesteImpl(new ProsessTaskRepositoryImpl(entityManager, null, null));
        migrasjonRestTjeneste = new MigrasjonRestTjeneste(økonomiMottattXmlRepository, repositoryProvider, taskTjeneste);
    }

    @Test
    public void migrer_saksnummer_i_oko_xml_mottatt() throws Exception {
        økonomiMottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        økonomiMottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        assertThat(økonomiMottattXmlRepository.hentAlleMeldingerUtenSaksnummer()).isNotEmpty();

        Response response = migrasjonRestTjeneste.migrereSaksnummerIOkoXmlMottatt();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(økonomiMottattXmlRepository.hentAlleMeldingerUtenSaksnummer()).isEmpty();
    }

    @Test
    public void migrer_opprettelse_sakshendelser_til_dvh() {
        EventHendelseDto eventHendelseDto = new EventHendelseDto(DvhEventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        Response response = migrasjonRestTjeneste.sendSakshendelserTilDvhForAlleEksisterendeBehandlinger(eventHendelseDto);
        assertThat(response.getStatus()).isEqualTo(200);
        List<ProsessTaskData> prosesser = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosesser.size()).isEqualTo(1);
        ProsessTaskData prosessTaskData = prosesser.get(0);
        assertThat(prosessTaskData.getPropertyValue("eventHendelse")).isEqualTo(DvhEventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        assertThat(prosessTaskData.getPropertyValue("behandlingId")).isEqualTo(String.valueOf(behandling.getId()));
    }

    @Test
    public void migrer_avsluttelse_sakshendelser_til_dvh() {
        behandling.avsluttBehandling();
        EventHendelseDto eventHendelseDto = new EventHendelseDto(DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.name());
        Response response = migrasjonRestTjeneste.sendSakshendelserTilDvhForAlleEksisterendeBehandlinger(eventHendelseDto);
        assertThat(response.getStatus()).isEqualTo(200);
        List<ProsessTaskData> prosesser = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosesser.size()).isEqualTo(1);
        ProsessTaskData prosessTaskData = prosesser.get(0);
        assertThat(prosessTaskData.getPropertyValue("eventHendelse")).isEqualTo(DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.name());
        assertThat(prosessTaskData.getPropertyValue("behandlingId")).isEqualTo(String.valueOf(behandling.getId()));
    }

    private String getInputXML(String filename) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            return Files.readString(path);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }
}
