package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class VarselresponsTjenesteTest {

    private static Long BEHANDLING_ID;
    private static final ResponsKanal RESPONS_KANAL = ResponsKanal.SELVBETJENING;

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private HistorikkinnslagRepository historikkinnslagRepository;
    private VarselresponsTjeneste varselresponsTjeneste;

    @BeforeEach
    void setup(EntityManager entityManager) {
        behandlingRepository = new BehandlingRepository(entityManager);
        fagsakRepository = new FagsakRepository(entityManager);
        historikkinnslagRepository = new HistorikkinnslagRepository(entityManager);
        VarselresponsRepository repository = new VarselresponsRepository(entityManager);
        varselresponsTjeneste = new VarselresponsTjeneste(repository, historikkinnslagRepository, behandlingRepository);

        BEHANDLING_ID = opprettBehandling();
    }

    private Long opprettBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        return behandlingRepository.lagre(behandling, lås);
    }

    @Test
    void test_skal_lagre_respons() {
        Boolean akseptertFaktagrunnlag = true;

        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, RESPONS_KANAL, akseptertFaktagrunnlag);

        Optional<Varselrespons> result = varselresponsTjeneste.hentRespons(BEHANDLING_ID);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(result.get().getAkseptertFaktagrunnlag()).isTrue();
    }

    @Test
    void test_skal_ikke_kunne_oppdatere_eksisterende_respons() {
        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, RESPONS_KANAL, false);
        Optional<Varselrespons> result1 = varselresponsTjeneste.hentRespons(BEHANDLING_ID);

        assertThat(result1).isNotEmpty();
        assertThat(result1.get().getAkseptertFaktagrunnlag()).isFalse();

        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, RESPONS_KANAL, true);
        Optional<Varselrespons> result2 = varselresponsTjeneste.hentRespons(BEHANDLING_ID);

        assertThat(result2).isNotEmpty();
        assertThat(result2.get().getAkseptertFaktagrunnlag()).isFalse();
    }

    @Test
    void test_skal_returnere_tom_optional_ved_ukjent_saksnummer() {
        Optional<Varselrespons> result = varselresponsTjeneste.hentRespons(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void skal_opprette_historikkinnslag_når_bruker_har_uttalt_seg() {
        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, ResponsKanal.SELVBETJENING, true);

        var historikkinnslag = historikkinnslagRepository.hent(BEHANDLING_ID);

        assertThat(historikkinnslag).hasSize(1);
        assertThat(historikkinnslag.get(0).getTittel()).isEqualTo(VarselresponsTjeneste.HISTORIKK_TITTEL_UTTALELSE);
        assertThat(historikkinnslag.get(0).getAktør()).isEqualTo(HistorikkAktør.SØKER);
    }

    @Test
    void skal_opprette_historikkinnslag_for_hver_uttalelse_selv_om_kun_første_respons_lagres() {
        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, ResponsKanal.SELVBETJENING, true);
        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, ResponsKanal.SELVBETJENING, false);

        assertThat(historikkinnslagRepository.hent(BEHANDLING_ID)).hasSize(2);
        assertThat(varselresponsTjeneste.hentRespons(BEHANDLING_ID).orElseThrow().getAkseptertFaktagrunnlag()).isTrue();
    }

    @Test
    void skal_opprette_historikkinnslag_på_fagsak_når_det_ikke_finnes_behandling() {
        var behandling = behandlingRepository.hentBehandling(BEHANDLING_ID);

        varselresponsTjeneste.opprettHistorikkinnslagForUttalelseUtenBehandling(behandling.getFagsakId());

        var historikkinnslag = historikkinnslagRepository.hent(behandling.getFagsak().getSaksnummer());
        assertThat(historikkinnslag).hasSize(1);
        assertThat(historikkinnslag.get(0).getTittel()).isEqualTo(VarselresponsTjeneste.HISTORIKK_TITTEL_UTTALELSE);
        assertThat(historikkinnslag.get(0).getAktør()).isEqualTo(HistorikkAktør.SØKER);
        assertThat(historikkinnslag.get(0).getBehandlingId()).isNull();
    }
}
