package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

/**
 * Opprettet for å forenkle unit-tester
 * Setter opp repoer og tjenester.
 * feltnavn angir om repo/tjeneste er mock eller ikke.
 */
@CdiDbAwareTest
public abstract class FellesTestOppsett {

    protected static final LocalDate FOM = LocalDate.of(2016, 3, 10);
    protected static final String BEHANDLENDE_ENHET_ID = "4833";
    protected static final String BEHANDLENDE_ENHET_NAVN = "NAV Familie- og pensjonsytelser Oslo 1";

    protected BehandlingskontrollTjeneste behandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
    protected PersoninfoAdapter mockTpsTjeneste = mock(PersoninfoAdapter.class);
    protected FagsystemKlient mockFagsystemKlient = mock(FagsystemKlient.class);

    protected BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(behandlingskontrollTjeneste, mock(BehandlingskontrollAsynkTjeneste.class));

    protected BehandlingRepositoryProvider repoProvider;
    protected NavBrukerRepository brukerRepository;
    protected KravgrunnlagRepository grunnlagRepository;
    protected HistorikkRepository historikkRepository;
    protected BehandlingRepository behandlingRepository;
    protected ProsessTaskTjeneste taskTjeneste;
    protected BehandlingRevurderingTjeneste revurderingTjeneste;

    protected EntityManager entityManager;

    protected AktørId aktørId;

    protected Saksnummer saksnummer;
    protected Long fagsakId;
    protected Long internBehandlingId;
    protected Henvisning henvisning;
    protected UUID eksternBehandlingUuid;
    protected Behandling behandling;
    protected FagsakTjeneste fagsakTjeneste;

    protected BehandlingTjeneste behandlingTjeneste;

    protected TestUtility testUtility;

    //BeforeEach kalles både her og i subklasse
    @BeforeEach
    public final void init(EntityManager entityManager) {
        this.entityManager = entityManager;
        repoProvider = new BehandlingRepositoryProvider(entityManager);
        brukerRepository = new NavBrukerRepository(entityManager);
        grunnlagRepository = repoProvider.getGrunnlagRepository();
        historikkRepository = repoProvider.getHistorikkRepository();
        behandlingRepository = repoProvider.getBehandlingRepository();
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        revurderingTjeneste = new BehandlingRevurderingTjeneste(repoProvider, behandlingskontrollTjeneste);
        fagsakTjeneste = new FagsakTjeneste(mockTpsTjeneste, repoProvider.getFagsakRepository(), brukerRepository);
        behandlingTjeneste = new BehandlingTjeneste(repoProvider, behandlingskontrollProvider,
                fagsakTjeneste, mock(HistorikkinnslagTjeneste.class), mockFagsystemKlient);
        testUtility = new TestUtility(behandlingTjeneste);
        aktørId = testUtility.genererAktørId();
        when(mockTpsTjeneste.hentBrukerForAktør(aktørId)).thenReturn(testUtility.lagPersonInfo(aktørId));
        EksternBehandlingsinfoDto behandlingsinfoDto = lagEksternBehandlingInfoDto();
        Optional<EksternBehandlingsinfoDto> optBehandlingsinfo = Optional.of(behandlingsinfoDto);
        when(mockFagsystemKlient.hentBehandlingOptional(any(UUID.class))).thenReturn(optBehandlingsinfo);
        when(mockFagsystemKlient.hentBehandling(any(UUID.class))).thenReturn(behandlingsinfoDto);
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class))).thenReturn(
                lagSamletEksternBehandlingInfo(behandlingsinfoDto));

        TestUtility.SakDetaljer sakDetaljer = testUtility.opprettFørstegangsBehandling(aktørId);
        mapSakDetaljer(sakDetaljer);
    }

    private void mapSakDetaljer(TestUtility.SakDetaljer sakDetaljer) {
        aktørId = sakDetaljer.getAktørId();
        saksnummer = sakDetaljer.getSaksnummer();
        fagsakId = sakDetaljer.getFagsakId();
        internBehandlingId = sakDetaljer.getInternBehandlingId();
        henvisning = sakDetaljer.getHenvisning();
        eksternBehandlingUuid = sakDetaljer.getEksternUuid();
        behandling = sakDetaljer.getBehandling();
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingInfoDto() {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(10001L));
        eksternBehandlingsinfoDto.setBehandlendeEnhetId(BEHANDLENDE_ENHET_ID);
        eksternBehandlingsinfoDto.setBehandlendeEnhetNavn(BEHANDLENDE_ENHET_NAVN);
        return eksternBehandlingsinfoDto;
    }

    private PersonopplysningDto lagPersonOpplysningDto() {
        PersonopplysningDto personopplysningDto = new PersonopplysningDto();
        personopplysningDto.setAktoerId(aktørId.getId());
        return personopplysningDto;
    }

    private SamletEksternBehandlingInfo lagSamletEksternBehandlingInfo(EksternBehandlingsinfoDto behandlingsinfoDto) {
        return SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER)
                .setGrunninformasjon(behandlingsinfoDto)
                .setPersonopplysninger(lagPersonOpplysningDto())
                .build();
    }
}
