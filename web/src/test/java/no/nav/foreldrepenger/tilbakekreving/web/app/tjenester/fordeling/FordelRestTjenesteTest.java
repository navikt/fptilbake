package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordeling.FordelRestTjeneste.UTTALELSE_TILBAKEKREVING_DOKUMENT_TYPE_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktTestSupport;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;

@ExtendWith(JpaExtension.class)
class FordelRestTjenesteTest {

    private static final Saksnummer SAKSNUMMER = new Saksnummer("123456");
    private static final AktørId AKTØR_ID = new AktørId("123456");
    private static final String JOURNAL_POST_ID = "12345";
    private static final String FORSENDELSE_ID = UUID.randomUUID().toString();

    private GjenopptaBehandlingTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;

    private FordelRestTjeneste fordelRestTjeneste;
    private VarselresponsTjeneste varselresponsTjeneste;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        varselresponsTjeneste = new VarselresponsTjeneste(new VarselresponsRepository(entityManager));
        fordelRestTjeneste = new FordelRestTjeneste(repositoryProvider.getBehandlingRepository(), mockGjenopptaBehandlingTjeneste,
            varselresponsTjeneste);
    }

    @Test
    void mottaJournalpost_når_saksnummer_ikke_finnes() {
        Long behandlingId = lagBehandling();
        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto("10000", JOURNAL_POST_ID, FORSENDELSE_ID,
                UTTALELSE_TILBAKEKREVING_DOKUMENT_TYPE_ID, LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, never()).fortsettBehandlingManuelt(behandlingId, behandling.getFagsakId(), HistorikkAktør.SØKER);
    }

    @Test
    void mottaJournalpost_når_dokument_type_id_ikke_gyldig() {
        Long behandlingId = lagBehandling();
        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto(SAKSNUMMER.getVerdi(), JOURNAL_POST_ID, FORSENDELSE_ID,
                "XYZS", LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, never()).fortsettBehandlingManuelt(behandlingId, behandling.getFagsakId(), HistorikkAktør.SØKER);
    }

    @Test
    void mottaJournalpost_når_behandling_er_avsluttet() {
        Long behandlingId = lagBehandling();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandling.avsluttBehandling();

        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto(SAKSNUMMER.getVerdi(), JOURNAL_POST_ID, FORSENDELSE_ID,
                UTTALELSE_TILBAKEKREVING_DOKUMENT_TYPE_ID, LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, never()).fortsettBehandlingManuelt(behandlingId, behandling.getFagsakId(), HistorikkAktør.SØKER);
    }

    @Test
    void mottaJournalpost_når_behandling_er_på_vent() {
        Long behandlingId = lagBehandling();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        AksjonspunktTestSupport.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.FAKTA_FEILUTBETALING);

        AbacJournalpostMottakDto abacJournalpostMottakDto = new AbacJournalpostMottakDto(SAKSNUMMER.getVerdi(), JOURNAL_POST_ID, FORSENDELSE_ID,
                UTTALELSE_TILBAKEKREVING_DOKUMENT_TYPE_ID, LocalDateTime.now(), null);
        fordelRestTjeneste.mottaJournalpost(abacJournalpostMottakDto);
        verify(mockGjenopptaBehandlingTjeneste, atLeastOnce()).fortsettBehandlingManuelt(behandlingId, behandling.getFagsakId(), HistorikkAktør.SØKER);
        assertThat(varselresponsTjeneste.hentRespons(behandlingId)).isPresent();
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
