package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class VarselresponsTjenesteImplTest {

    private static Long BEHANDLING_ID;
    private static final ResponsKanal RESPONS_KANAL = ResponsKanal.SELVBETJENING;

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private VarselresponsTjeneste varselresponsTjeneste;

    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private VarselresponsRepository repository;

    @Before
    public void setup() {
        behandlingRepository = new BehandlingRepositoryImpl(repositoryRule.getEntityManager());
        fagsakRepository = new FagsakRepositoryImpl(repositoryRule.getEntityManager());
        repository = new VarselresponsRepositoryImpl(repositoryRule.getEntityManager());
        varselresponsTjeneste = new VarselresponsTjenesteImpl(repository);

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
    public void test_skal_lagre_respons() {
        Boolean akseptertFaktagrunnlag = true;

        varselresponsTjeneste.lagreRespons(BEHANDLING_ID, RESPONS_KANAL, akseptertFaktagrunnlag);

        Optional<Varselrespons> result = varselresponsTjeneste.hentRespons(BEHANDLING_ID);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(result.get().getAkseptertFaktagrunnlag()).isTrue();
    }

    @Test
    public void test_skal_ikke_kunne_oppdatere_eksisterende_respons() {
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
    public void test_skal_returnere_tom_optional_ved_ukjent_saksnummer() {
        Optional<Varselrespons> result = varselresponsTjeneste.hentRespons(99999L);

        assertThat(result).isEmpty();
    }
}
