package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fpoversikt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;

@CdiDbAwareTest
class FpOversiktDtoTjenesteTest {

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private VarselresponsTjeneste varselresponsTjeneste;
    @Inject
    private EntityManager entityManager;

    @Test
    void uten_åpen_tilbakekreving_returnerer_empty() {
        var tjeneste = tjeneste();
        var scenario = new ScenarioSimple();
        var behandling = scenario.medVedtak(LocalDate.now())
            .medBehandlingType(BehandlingType.TILBAKEKREVING)
            .medBehandlingResultatType(BehandlingResultatType.FULL_TILBAKEBETALING)
            .lagre(repositoryProvider);
        avsluttBehandling(behandling);

        var sak = tjeneste.hentSak(behandling.getFagsak().getSaksnummer());
        assertThat(sak).isEmpty();
    }

    @Test
    void returnerer_med_verge() {
        var tjeneste = tjeneste();
        var behandling = åpenTilbakekreving();
        lagVerge(behandling);

        var sak = tjeneste.hentSak(behandling.getFagsak().getSaksnummer());
        assertThat(sak).isPresent();
        assertThat(sak.get().saksnummer()).isEqualTo(behandling.getFagsak().getSaksnummer().getVerdi());
        assertThat(sak.get().harVerge()).isTrue();
        assertThat(sak.get().varsel().sendt()).isFalse();
        assertThat(sak.get().varsel().besvart()).isFalse();
    }

    @Test
    void returnerer_med_varsel() {
        var tjeneste = tjeneste();
        var behandling = åpenTilbakekreving();

        lagVarsel(behandling);
        lagVarselRespons(behandling);

        var sak = tjeneste.hentSak(behandling.getFagsak().getSaksnummer());

        assertThat(sak).isPresent();
        assertThat(sak.get().harVerge()).isFalse();
        assertThat(sak.get().varsel().sendt()).isTrue();
        assertThat(sak.get().varsel().besvart()).isTrue();
    }

    private void lagVarselRespons(Behandling behandling) {
        varselresponsTjeneste.lagreRespons(behandling.getId(), ResponsKanal.MANUELL, null);
    }

    private void lagVarsel(Behandling behandling) {
        var brev = new BrevSporing.Builder()
            .medBehandlingId(behandling.getId())
            .medJournalpostId(new JournalpostId(1L))
            .medBrevType(BrevType.VARSEL_BREV)
            .medDokumentId("1")
            .build();
        repositoryProvider.getBrevSporingRepository().lagre(brev);
        entityManager.flush();
    }

    private void lagVerge(Behandling behandling) {
        var verge = new VergeEntitet.Builder()
            .medNavn("abc")
            .medBegrunnelse("")
            .medKilde(ResponsKanal.SELVBETJENING.getDbKode())
            .medVergeAktørId(new AktørId(1L))
            .medGyldigPeriode(LocalDate.now().minusYears(1), LocalDate.now().plusYears(5))
            .build();
        var vergeRepository = repositoryProvider.getVergeRepository();
        vergeRepository.lagreVergeInformasjon(behandling.getId(), verge);
        entityManager.flush(); //repo flusher ikke
    }

    private void avsluttBehandling(Behandling behandling) {
        behandling.avsluttBehandling();
        repositoryProvider.getBehandlingRepository().lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));
    }

    private Behandling åpenTilbakekreving() {
        var scenario = new ScenarioSimple();
        return scenario
            .medBehandlingType(BehandlingType.TILBAKEKREVING)
            .medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT)
            .lagre(repositoryProvider);
    }

    private FpOversiktDtoTjeneste tjeneste() {
        return new FpOversiktDtoTjeneste(varselresponsTjeneste, repositoryProvider.getBrevSporingRepository(),
            repositoryProvider.getBehandlingRepository(),
            repositoryProvider.getVergeRepository());
    }


}