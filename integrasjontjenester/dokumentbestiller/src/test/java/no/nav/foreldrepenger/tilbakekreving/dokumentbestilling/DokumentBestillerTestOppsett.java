package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.time.LocalDate;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class DokumentBestillerTestOppsett {

    protected static final long FPSAK_BEHANDLING_ID = 99051L;
    protected static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID);
    protected static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();
    protected static final String DUMMY_FØDSELSNUMMER = "31018143212";

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    protected BehandlingRepositoryProvider repositoryProvider;
    @Inject
    protected BehandlingRepository behandlingRepository;
    @Inject
    protected EksternBehandlingRepository eksternBehandlingRepository;
    @Inject
    protected BrevSporingRepository brevSporingRepository;
    @Inject
    protected HistorikkRepository historikkRepository;
    @Inject
    protected VergeRepository vergeRepository;
    @Inject
    private FagsakRepository fagsakRepository;

    protected Fagsak fagsak;
    protected Behandling behandling;
    protected EksternBehandling eksternBehandling;

    @Before
    public void init() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        eksternBehandling = new EksternBehandling(behandling, HENVISNING, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    protected YtelseNavn lagYtelseNavn(String navnPåBrukersSpråk, String navnPåBokmål) {
        YtelseNavn ytelseNavn = new YtelseNavn();
        ytelseNavn.setNavnPåBrukersSpråk(navnPåBrukersSpråk);
        ytelseNavn.setNavnPåBokmål(navnPåBokmål);
        return ytelseNavn;
    }

    protected JournalpostIdOgDokumentId lagJournalOgDokument() {
        JournalpostId journalpostId = new JournalpostId(12344l);
        return new JournalpostIdOgDokumentId(journalpostId, "qwr12334");
    }

    protected Personinfo byggStandardPerson(String navn, String personnummer, Språkkode språkkode) {
        return new Personinfo.Builder()
            .medPersonIdent(PersonIdent.fra(personnummer))
            .medNavn(navn)
            .medAktørId(new AktørId(9000000030014L))
            .medFødselsdato(LocalDate.of(1990, 2, 2))
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .build();
    }

    protected Adresseinfo lagStandardNorskAdresse() {
        return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, new PersonIdent("12345678901"), "Jens Trallala")
            .medAdresselinje1("adresselinje 1")
            .medAdresselinje2("adresselinje 2")
            .medAdresselinje3("adresselinje 3")
            .medLand("NOR")
            .medPostNr("0688")
            .medPoststed("OSLO")
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
