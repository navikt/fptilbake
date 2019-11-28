package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.util.FPDateUtil;

public class ForvaltningBehandlingRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, mock(BehandlingModellRepository.class), mock(BehandlingskontrollEventPubliserer.class));
    private ForvaltningBehandlingRestTjeneste forvaltningBehandlingRestTjeneste = new ForvaltningBehandlingRestTjeneste(behandlingRepository, prosessTaskRepository);

    @Test
    public void skal_ikke_tvinge_henlegg_behandling_når_behandling_er_allerede_avsluttet() {
        Behandling behandling = lagBehandling();
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_tvinge_henlegg_behandling() {
        Behandling behandling = lagBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(TvingHenlegglBehandlingTask.TASKTYPE);
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_er_avsluttet() {
        Behandling behandling = lagBehandling();
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_ikke_er_på_vent() {
        Behandling behandling = lagBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, FPDateUtil.nå().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(GjenopptaBehandlingTask.TASKTYPE);
    }

    private Behandling lagBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }
}
