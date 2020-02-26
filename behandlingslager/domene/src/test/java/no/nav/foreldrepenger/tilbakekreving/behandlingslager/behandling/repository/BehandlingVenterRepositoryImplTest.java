package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class BehandlingVenterRepositoryImplTest {

    @Rule
    public final UnittestRepositoryRule reporule = new UnittestRepositoryRule();
    private final EntityManager entityManager = reporule.getEntityManager();

    private final FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories = new FellesQueriesForBehandlingRepositories(entityManager);
    private final BehandlingVenterRepositoryImpl repository = new BehandlingVenterRepositoryImpl(fellesQueriesForBehandlingRepositories);
    private final BehandlingRepositoryProviderImpl repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);

    private final AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();

    @Before
    public void setUp() {
    }

    @Test
    public void test_skalHenteBehandlingHvisVenterPåBrukerrespons() {
        Behandling behandling1 = opprettBehandling("325235", "5235235", BehandlingType.TILBAKEKREVING);
        Behandling behandling2 = opprettBehandling("423523", "523543", BehandlingType.TILBAKEKREVING);

        Long behandlingId1 = behandling1.getId();
        Long behandlingId2 = behandling2.getId();

        settVenterPåBrukerRespons(behandling1, LocalDateTime.now().plusWeeks(2));

        Optional<Behandling> resultat1 = repository.hentBehandlingPåVent(behandlingId1);
        Optional<Behandling> resultat2 = repository.hentBehandlingPåVent(behandlingId2);

        assertThat(resultat1).isPresent();
        assertThat(resultat1).contains(behandling1);
        assertThat(resultat2).isNotPresent();
    }

    @Test
    public void test_skalHenteBehandlingHvisVenterPåØkonomiGrunnlag() {
        Behandling behandling1 = opprettBehandling("325235", "5235235", BehandlingType.TILBAKEKREVING);
        Behandling behandling2 = opprettBehandling("423523", "523543", BehandlingType.TILBAKEKREVING);

        Long behandlingId1 = behandling1.getId();
        Long behandlingId2 = behandling2.getId();

        settVenterPåTilbakekrevinggrunnlag(behandling1);

        Optional<Behandling> resultat1 = repository.hentBehandlingPåVent(behandlingId1);
        Optional<Behandling> resultat2 = repository.hentBehandlingPåVent(behandlingId2);

        assertThat(resultat1).isPresent();
        assertThat(resultat1).contains(behandling1);
        assertThat(resultat2).isNotPresent();
    }

    @Test
    public void test_skalHenteBehandlingHvisVenterPåØkonomiGrunnlagOgPåBrukerSamtidig() {
        Behandling behandling = opprettBehandling("325235", "5235235", BehandlingType.TILBAKEKREVING);

        Long behandlingId = behandling.getId();

        settVenterPåTilbakekrevinggrunnlag(behandling);
        settVenterPåBrukerRespons(behandling, LocalDateTime.now().plusWeeks(2));

        Optional<Behandling> resultat = repository.hentBehandlingPåVent(behandlingId);
        assertThat(resultat).isPresent();
        assertThat(resultat).contains(behandling);
    }

    private Long lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        return behandlingRepository.lagre(behandling, lås);
    }

    private Behandling opprettBehandling(String aktørId, String saksnummer, BehandlingType behandlingType) {
        // opprett fagsak
        Fagsak fagsak = opprettFagsak(aktørId, saksnummer);

        // opprett behandling
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, behandlingType)
                .medOpprettetDato(LocalDateTime.now()).build();

        // lagre behandling
        Long id = lagreBehandling(behandling);
        return behandlingRepository.hentBehandling(id);
    }

    private Fagsak opprettFagsak(String aktørId, String saksnummer) {
        NavBruker b = NavBruker.opprettNy(new AktørId(aktørId), Språkkode.nb);
        Fagsak fagsak = TestFagsakUtil.opprettFagsak(new Saksnummer(saksnummer), b);
        Long fagsakId = fagsakRepository.lagre(fagsak);
        return fagsakRepository.finnEksaktFagsak(fagsakId);
    }

    private void settVenterPåBrukerRespons(Behandling behandling, LocalDateTime frist) {
        aksjonspunktRepository.settBehandlingPåVent(behandling,
                AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
                BehandlingStegType.VARSEL,
                frist,
                Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        lagreBehandling(behandling);
    }

    private void settVenterPåTilbakekrevinggrunnlag(Behandling behandling) {
        aksjonspunktRepository.settBehandlingPåVent(behandling,
                AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                BehandlingStegType.VARSEL,
                null,
                Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        lagreBehandling(behandling);
    }

}
