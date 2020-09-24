package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.FlushModeType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class MigrasjonRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository = new ØkonomiMottattXmlRepository(repositoryRule.getEntityManager());
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private EksternBehandlingRepository eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
    private BehandlingresultatRepository behandlingresultatRepository = repositoryProvider.getBehandlingresultatRepository();
    private ProsessTaskRepository taskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
    private MigrasjonRestTjeneste migrasjonRestTjeneste = new MigrasjonRestTjeneste(økonomiMottattXmlRepository, repositoryProvider, taskRepository);

    private Behandling behandling;

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
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
        List<ProsessTaskData> prosesser = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
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
        List<ProsessTaskData> prosesser = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosesser.size()).isEqualTo(1);
        ProsessTaskData prosessTaskData = prosesser.get(0);
        assertThat(prosessTaskData.getPropertyValue("eventHendelse")).isEqualTo(DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.name());
        assertThat(prosessTaskData.getPropertyValue("behandlingId")).isEqualTo(String.valueOf(behandling.getId()));
    }

    @Test
    public void migrer_vedtakresultattype_til_behandlingsresultat(){
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.FASTSATT)
            .medBehandling(behandling).build();
        behandlingresultatRepository.lagre(behandlingsresultat);
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtakResultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medAnsvarligSaksbehandler("VL").build();
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak);
        behandling.avsluttBehandling();
        migrasjonRestTjeneste.migrerVedtakResultatTypeTilBehandlingResultat();
        Optional<Behandlingsresultat> resultat = behandlingresultatRepository.hent(behandling);
        assertThat(resultat).isPresent();
        behandlingsresultat = resultat.get();
        assertThat(behandlingsresultat.getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.fraVedtakResultatType(VedtakResultatType.DELVIS_TILBAKEBETALING));
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
