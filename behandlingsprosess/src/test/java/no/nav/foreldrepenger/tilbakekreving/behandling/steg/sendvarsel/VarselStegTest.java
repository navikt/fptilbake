package no.nav.foreldrepenger.tilbakekreving.behandling.steg.sendvarsel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@CdiDbAwareTest
class VarselStegTest {

    @Inject
    private ProsessTaskTjeneste taskTjeneste;
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private HistorikkRepository historikkRepository;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private VarselRepository varselRepository;

    private final VarselresponsTjeneste varselresponsTjeneste = mock(VarselresponsTjeneste.class);
    private Fagsak fagsak;
    private Behandling behandling;

    @BeforeAll
    static void setupAlle() {
        System.setProperty("app.name", "fptilbake");
    }

    @AfterAll
    static void teardown() {
        System.clearProperty("app.name");
    }

    @BeforeEach
    void setup() {
        fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        behandling = lagBehandling(fagsak, false);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);
    }

    @Test
    void skal_sette_behandling_på_vent() {

        varselRepository.lagre(behandling.getId(), "hello", 23000l);
        KontekstHolder.setKontekst(BasisKontekst.forProsesstaskUtenSystembruker());

        //act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));


        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        Aksjonspunkt ap = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        assertThat(ap.getFristTid().toLocalDate()).isEqualTo(LocalDate.now().plus(Frister.BEHANDLING_TILSVAR).plusDays(1));

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslager).isNotEmpty();
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BEH_VENT);
        KontekstHolder.fjernKontekst();
    }

    @Test
    void skal_ikke_sette_behandling_på_vent_når_varseltekst_ikke_finnes() {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));
        assertThat(stegResultat.getAksjonspunktListe()).isEmpty();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    @Test
    void skal_ikke_sette_behandling_på_vent_når_behandling_er_manuelt_opprettet() {
        Behandling behandling = lagBehandling(fagsak, true);
        varselRepository.lagre(behandling.getId(), "hello", 23000l);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getAksjonspunktListe()).isEmpty();
        assertThat(behandling.isBehandlingPåVent()).isFalse();
    }

    private Behandling lagBehandling(Fagsak fagsak, boolean manueltOpprettet) {
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).medManueltOpprettet(manueltOpprettet).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private VarselSteg steg() {
        return new VarselSteg(
                repositoryProvider,
                behandlingskontrollTjeneste,
                varselresponsTjeneste,
                taskTjeneste
        );
    }
}
