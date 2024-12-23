package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

import org.junit.jupiter.api.BeforeEach;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

@CdiDbAwareTest
public abstract class DokumentBestillerTestOppsett {

    protected static final long FPSAK_BEHANDLING_ID = 99051L;
    protected static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID);
    protected static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();
    protected static final String DUMMY_FØDSELSNUMMER = "00000000000";

    @Inject
    protected BehandlingRepositoryProvider repositoryProvider;
    @Inject
    protected BehandlingRepository behandlingRepository;
    @Inject
    protected EksternBehandlingRepository eksternBehandlingRepository;
    @Inject
    protected BrevSporingRepository brevSporingRepository;
    @Inject
    protected HistorikkinnslagRepository historikkRepository;
    @Inject
    protected VergeRepository vergeRepository;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    protected EntityManager entityManager;

    protected Fagsak fagsak;
    protected Behandling behandling;
    protected EksternBehandling eksternBehandling;

    //BeforeEach kalles både her og i subklasse
    @BeforeEach
    public void init() {
        entityManager.setFlushMode(FlushModeType.AUTO);
        fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        var behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        eksternBehandling = new EksternBehandling(behandling, HENVISNING, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    protected YtelseNavn lagYtelseNavn(String navnPåBrukersSpråk, String navnPåBokmål) {
        var ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk(navnPåBrukersSpråk);
        ytelseNavn.setNavnPåBokmål(navnPåBokmål);
        return ytelseNavn;
    }

    protected Personinfo byggStandardPerson(String navn, String personnummer, Språkkode språkkode) {
        return new Personinfo.Builder()
                .medPersonIdent(PersonIdent.fra(personnummer))
                .medNavn(navn)
                .medAktørId(new AktørId(1000000000000L))
                .medFødselsdato(LocalDate.of(1990, 2, 2))
                .build();
    }

    protected Adresseinfo lagStandardNorskAdresse() {
        return new Adresseinfo.Builder(new PersonIdent("12345678901"), "Jens Trallala")
                .build();
    }

    protected VergeEntitet lagVerge() {
        return VergeEntitet.builder()
                .medVergeType(VergeType.ADVOKAT)
                .medOrganisasjonnummer("1232456")
                .medBegrunnelse("test")
                .medKilde(KildeType.FPTILBAKE.name())
                .medNavn("John Doe")
                .medGyldigPeriode(LocalDate.now().minusDays(30), LocalDate.now()).build();
    }
}
