package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling.FordelRestTjeneste.UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class FordelRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private static final Saksnummer SAKSNUMMER = new Saksnummer("123456");
    private static final AktørId AKTØR_ID = new AktørId("123456");
    private static final String JOURNAL_POST_ID = "12345";
    private static final String FORSENDELSE_ID = UUID.randomUUID().toString();

    private GjenopptaBehandlingTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(repoRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private FordelRestTjeneste fordelRestTjeneste = new FordelRestTjeneste(repositoryProvider.getBehandlingRepository(), mockGjenopptaBehandlingTjeneste);

    @Test
    public void mottaJournalpost_når_saksnummer_ikke_finnes() {
        Long behandlingId = lagBehandling();
        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto("10000", JOURNAL_POST_ID, FORSENDELSE_ID,
            UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID, LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, never()).fortsettBehandlingManuelt(behandlingId, HistorikkAktør.SØKER);
    }

    @Test
    public void mottaJournalpost_når_dokument_type_id_ikke_gyldig() {
        Long behandlingId = lagBehandling();
        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto(SAKSNUMMER.getVerdi(), JOURNAL_POST_ID, FORSENDELSE_ID,
            "XYZS", LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, never()).fortsettBehandlingManuelt(behandlingId, HistorikkAktør.SØKER);
    }

    @Test
    public void mottaJournalpost_når_behandling_er_avsluttet() {
        Long behandlingId = lagBehandling();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandling.avsluttBehandling();

        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto(SAKSNUMMER.getVerdi(), JOURNAL_POST_ID, FORSENDELSE_ID,
            UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID, LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, never()).fortsettBehandlingManuelt(behandlingId, HistorikkAktør.SØKER);
    }

    @Test
    public void mottaJournalpost_når_behandling_er_på_vent() {
        Long behandlingId = lagBehandling();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.FAKTA_FEILUTBETALING);

        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto(SAKSNUMMER.getVerdi(), JOURNAL_POST_ID, FORSENDELSE_ID,
            UTTALSE_TILBAKEKREVING_DOKUMENT_TYPE_ID, LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, atLeastOnce()).fortsettBehandlingManuelt(behandlingId, HistorikkAktør.SØKER);
    }

    private Long lagBehandling() {
        NavBruker navBruker = NavBruker.opprettNy(AKTØR_ID, Språkkode.nb);
        Fagsak fagsak = Fagsak.opprettNy(SAKSNUMMER, navBruker);
        repositoryProvider.getFagsakRepository().lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        return behandlingRepository.lagre(behandling, behandlingLås);
    }
}
