package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class FagsakRepositoryTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private FagsakRepository fagsakRepository = new FagsakRepository(repoRule.getEntityManager());

    @Test
    public void skal_finne_eksakt_fagsak_gitt_id() {
        AktørId aktørId = new AktørId("100");
        Saksnummer saksnummer = new Saksnummer("200");
        Fagsak fagsak = opprettFagsak(saksnummer, aktørId);

        Fagsak resultat = fagsakRepository.finnEksaktFagsak(fagsak.getId());

        Assertions.assertThat(resultat).isNotNull();
    }

    @Test
    public void skal_finne_unik_fagsak_gitt_id() {
        AktørId aktørId = new AktørId("100");
        Saksnummer saksnummer = new Saksnummer("200");
        Fagsak fagsak = opprettFagsak(saksnummer, aktørId);

        Optional<Fagsak> resultat = fagsakRepository.finnUnikFagsak(fagsak.getId());

        Assertions.assertThat(resultat).isPresent();
    }

    @Test
    public void skal_finne_fagsak_gitt_saksnummer() {
        AktørId aktørId = new AktørId("100");
        Saksnummer saksnummer = new Saksnummer("200");

        opprettFagsak(saksnummer, aktørId);
        Optional<Fagsak> optional = fagsakRepository.hentSakGittSaksnummer(saksnummer);

        Assertions.assertThat(optional).isPresent();
    }

    @Test
    public void skal_finne_fagsak_gitt_aktør_id() {
        AktørId aktørId = new AktørId("1000");
        Saksnummer saksnummer = new Saksnummer("200");

        opprettFagsak(saksnummer, aktørId, 1234L, Fagsystem.FPSAK);
        List<Fagsak> list = fagsakRepository.hentForBrukerAktørId(aktørId);

        Assertions.assertThat(list).hasSize(1);
    }


    private Fagsak opprettFagsak(Saksnummer saksnummer, AktørId aktørId) {
        NavBruker bruker = NavBruker.opprettNy(aktørId, Språkkode.nb);

        // Opprett fagsak
        Fagsak fagsak = TestFagsakUtil.opprettFagsak(saksnummer, bruker);
        repository.lagre(fagsak);
        repository.flushAndClear();
        return fagsak;
    }


    private Fagsak opprettFagsak(Saksnummer saksnummer, AktørId aktørId, Long eksternSystemId, Fagsystem eksternFagsakSystem) {
        NavBruker bruker = NavBruker.opprettNy(aktørId, Språkkode.nb);

        // Opprett fagsak
        Fagsak fagsak = TestFagsakUtil.opprettFagsak(saksnummer, bruker);
        fagsak.setSaksnummer(saksnummer);
        repository.lagre(fagsak);
        repository.flushAndClear();
        return fagsak;
    }
}
