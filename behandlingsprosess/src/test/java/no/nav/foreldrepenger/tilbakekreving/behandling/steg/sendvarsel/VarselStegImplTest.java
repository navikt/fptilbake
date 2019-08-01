package no.nav.foreldrepenger.tilbakekreving.behandling.steg.sendvarsel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class VarselStegImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private ProsessTaskRepository prosessTaskRepository;
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private HistorikkRepository historikkRepository = new HistorikkRepository(repoRule.getEntityManager());
    private VarselresponsTjeneste varselresponsTjeneste = mock(VarselresponsTjeneste.class);

    @Ignore //TODO: må fikses. Feiler på bygg av et visst klokkeslett. Brukerhistorie: PFP-7795
    @Test
    public void skal_sette_behandling_på_vent() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        Behandling behandling = lagBehandling(fagsak);


        //act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));


        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        Aksjonspunkt ap = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        assertThat(ap.getFristTid()).isEqualToIgnoringSeconds(LocalDateTime.now().plusWeeks(4).plusDays(1));

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslager).isNotEmpty();
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BEH_VENT);
    }

    private Behandling lagBehandling(Fagsak fagsak){
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private VarselSteg steg() {
        return new VarselStegImpl(
                behandlingRepository,
                behandlingskontrollTjeneste,
                varselresponsTjeneste,
                prosessTaskRepository,
                Period.ofWeeks(4));
    }
}