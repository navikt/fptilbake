package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class VarselresponsTjenesteTest {

    private static Long BEHANDLING_ID;
    private static final ResponsKanal RESPONS_KANAL = ResponsKanal.SELVBETJENING;

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private VarselresponsTjeneste varselresponsTjeneste;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        behandlingRepository = new BehandlingRepository(entityManager);
        fagsakRepository = new FagsakRepository(entityManager);
        VarselresponsRepository repository = new VarselresponsRepository(entityManager);
        varselresponsTjeneste = new VarselresponsTjeneste(repository);

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
