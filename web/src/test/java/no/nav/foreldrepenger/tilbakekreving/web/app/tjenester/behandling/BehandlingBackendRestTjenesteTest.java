package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.KlageTilbakekrevingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

public class BehandlingBackendRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, mock(ProsessTaskEventPubliserer.class));

    private BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(mock(BehandlingskontrollTjenesteImpl.class), mock(BehandlingskontrollAsynkTjeneste.class));

    private BehandlingTjeneste behandlingTjeneste = new BehandlingTjenesteImpl(repositoryProvider, prosessTaskRepository, behandlingskontrollProvider, null, null, null, Period.ZERO);

    private BehandlingBackendRestTjeneste behandlingBackendRestTjeneste = new BehandlingBackendRestTjeneste(behandlingTjeneste, repositoryProvider.getBehandlingVedtakRepository());

    private Behandling behandling;
    private Saksnummer saksnummer;
    private UUID uuid;

    @Before
    public void setup() {
        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        saksnummer = behandling.getFagsak().getSaksnummer();
        uuid = behandling.getUuid();
    }

    @Test
    public void skal_ha_åpen_tilbakekreving_hvis_tilbakekreving_ikke_er_avsluttet() {
        Response response = behandlingBackendRestTjeneste.harÅpenTilbakekrevingBehandling(new SaksnummerDto(saksnummer.getVerdi()));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getEntity()).isEqualTo(true);
    }

    @Test
    public void skal_ikke_ha_åpen_tilbakekreving_hvis_tilbakekreving_er_avsluttet() {
        behandling.avsluttBehandling();

        Response response = behandlingBackendRestTjeneste.harÅpenTilbakekrevingBehandling(new SaksnummerDto(saksnummer.getVerdi()));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getEntity()).isEqualTo(false);
    }

    @Test
    public void skal_ikke_returnere_vedtak_info_hvis_tilbakekreving_ikke_er_avsluttet() {
        assertThrows("FPT-763492", TekniskException.class, () -> behandlingBackendRestTjeneste.hentTilbakekrevingsVedtakInfo(new UuidDto(uuid)));
    }

    @Test
    public void skal_ikke_returnere_vedtak_info_hvis_vedtak_info_ikke_finnes() {
        behandling.avsluttBehandling();
        assertThrows("FPT-763492", TekniskException.class, () -> behandlingBackendRestTjeneste.hentTilbakekrevingsVedtakInfo(new UuidDto(uuid)));
    }

    @Test
    public void skal_returnere_vedtak_info_hvis_tilbakekreving_er_avsluttet_og_vedtak_info_finnes() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandling(behandling)
            .medBehandlingResultatType(BehandlingResultatType.FASTSATT).build();
        repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
        var vedtakDato = LocalDate.now();
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtaksdato(vedtakDato)
            .medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .medAnsvarligSaksbehandler("VL")
            .medVedtakResultat(VedtakResultatType.FULL_TILBAKEBETALING).build();
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak);
        behandling.avsluttBehandling();

        Response response = behandlingBackendRestTjeneste.hentTilbakekrevingsVedtakInfo(new UuidDto(uuid));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        KlageTilbakekrevingDto klageTilbakekrevingDto = (KlageTilbakekrevingDto) response.getEntity();
        assertThat(klageTilbakekrevingDto.getBehandlingType()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(klageTilbakekrevingDto.getBehandlingId()).isNotNull();
        assertThat(klageTilbakekrevingDto.getVedtakDato()).isEqualTo(vedtakDato);
    }
}
