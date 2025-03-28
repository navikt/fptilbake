package no.nav.foreldrepenger.tilbakekreving;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAktsomhetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatAnnetDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårResultatGodTroDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår.VilkårsvurderingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.AutomatiskSaksbehandlingVurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingHistorikkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.GjenopptaBehandlingMedGrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurderForeldelseHistorikkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHistorikkInnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

/**
 * Opprettet for å forenkle unit-tester
 * Setter opp repoer og tjenester.
 * feltnavn angir om repo/tjeneste er mock eller ikke.
 */
@CdiDbAwareTest
public abstract class FellesTestOppsett {

    protected static final LocalDate FOM = LocalDate.of(2016, 3, 10);
    protected static final LocalDate TOM = LocalDate.of(2016, 5, 31);
    protected static final Long SUM_INNTREKK = 1000L;
    protected static final HendelseType HENDELSE_TYPE = HendelseType.FP_UTTAK_UTSETTELSE_TYPE;
    protected static final HendelseUnderType HENDELSE_UNDERTYPE = HendelseUnderType.ARBEID_HELTID;
    protected static final String BEGRUNNELSE = "ABC";
    protected static final String BEHANDLENDE_ENHET_ID = "4833";
    protected static final String BEHANDLENDE_ENHET_NAVN = "Nav familie- og pensjonsytelser Oslo 1";
    protected static final BehandlingType REVURDERING_BEHANDLING_TYPE = BehandlingType.REVURDERING_TILBAKEKREVING;
    protected static final String SÆRLIG_GRUNNER_BEGRUNNELSE = "særlig grunner begrunnelse";

    protected BehandlingskontrollTjeneste behandlingskontrollTjeneste;;
    protected GjenopptaBehandlingMedGrunnlagTjeneste gjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingMedGrunnlagTjeneste.class);
    protected BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste = mock(BehandlingskontrollAsynkTjeneste.class);
    protected PersoninfoAdapter mockTpsTjeneste = mock(PersoninfoAdapter.class);
    protected FagsystemKlient mockFagsystemKlient = mock(FagsystemKlient.class);
    protected SlettGrunnlagEventPubliserer mockSlettGrunnlagEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);

    protected BehandlingskontrollProvider behandlingskontrollProvider;

    protected BehandlingRepositoryProvider repoProvider;
    protected NavBrukerRepository brukerRepository;
    protected KravgrunnlagRepository grunnlagRepository;
    protected HistorikkinnslagRepository historikkinnslagRepository;
    protected FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    protected VurdertForeldelseRepository vurdertForeldelseRepository;
    protected VilkårsvurderingRepository vilkårsvurderingRepository;
    protected TotrinnRepository totrinnRepository;
    protected BehandlingRepository behandlingRepository;
    protected VarselRepository varselRepository;
    protected ProsessTaskTjeneste taskTjeneste;
    protected KravgrunnlagTjeneste kravgrunnlagTjeneste;
    protected KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste;

    protected HistorikkTjeneste historikkTjeneste;
    protected VurdertForeldelseTjeneste vurdertForeldelseTjeneste;

    protected VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste;

    protected VilkårsvurderingTjeneste vilkårsvurderingTjeneste;

    protected BehandlingRevurderingTjeneste revurderingTjeneste;

    protected FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;

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
        var behandlinskontrollServiceProvider = new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(behandlinskontrollServiceProvider);
        behandlingskontrollProvider =  new BehandlingskontrollProvider(behandlingskontrollTjeneste, behandlingskontrollAsynkTjeneste);
        repoProvider = new BehandlingRepositoryProvider(entityManager);
        brukerRepository = new NavBrukerRepository(entityManager);
        grunnlagRepository = repoProvider.getGrunnlagRepository();
        historikkinnslagRepository = repoProvider.getHistorikkinnslagRepository();
        faktaFeilutbetalingRepository = repoProvider.getFaktaFeilutbetalingRepository();
        vurdertForeldelseRepository = repoProvider.getVurdertForeldelseRepository();
        vilkårsvurderingRepository = new VilkårsvurderingRepository(entityManager);
        totrinnRepository = new TotrinnRepository(entityManager);
        behandlingRepository = repoProvider.getBehandlingRepository();
        varselRepository = repoProvider.getVarselRepository();
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        var halvtGebyrTjeneste = new AutomatiskSaksbehandlingVurderingTjeneste(grunnlagRepository, varselRepository);
        kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repoProvider, gjenopptaBehandlingTjeneste, behandlingskontrollTjeneste,
            mockSlettGrunnlagEventPubliserer, halvtGebyrTjeneste, entityManager);
        kravgrunnlagBeregningTjeneste = new KravgrunnlagBeregningTjeneste(grunnlagRepository);
        historikkTjeneste = new HistorikkTjeneste(behandlingRepository, historikkinnslagRepository);
        var vurderForeldelseHistorikkTjeneste = new VurderForeldelseHistorikkTjeneste(historikkinnslagRepository);
        vurdertForeldelseTjeneste = new VurdertForeldelseTjeneste(repoProvider, vurderForeldelseHistorikkTjeneste, kravgrunnlagBeregningTjeneste);
        vilkårsvurderingHistorikkInnslagTjeneste = new VilkårsvurderingHistorikkInnslagTjeneste(historikkinnslagRepository);
        vilkårsvurderingTjeneste = new VilkårsvurderingTjeneste(vurdertForeldelseTjeneste, repoProvider, vilkårsvurderingHistorikkInnslagTjeneste, kravgrunnlagBeregningTjeneste);
        revurderingTjeneste = new BehandlingRevurderingTjeneste(repoProvider, behandlingskontrollTjeneste);
        faktaFeilutbetalingTjeneste = new FaktaFeilutbetalingTjeneste(repoProvider, kravgrunnlagTjeneste, mockFagsystemKlient);
        fagsakTjeneste = new FagsakTjeneste(mockTpsTjeneste, repoProvider.getFagsakRepository(), brukerRepository);
        behandlingTjeneste = new BehandlingTjeneste(repoProvider, behandlingskontrollProvider,
                fagsakTjeneste, new BehandlingHistorikkTjeneste(historikkinnslagRepository, historikkTjeneste), mockFagsystemKlient);
        testUtility = new TestUtility(behandlingTjeneste);
        aktørId = testUtility.genererAktørId();
        when(mockTpsTjeneste.hentBrukerForAktør(any(), eq(aktørId))).thenReturn(testUtility.lagPersonInfo(aktørId));
        EksternBehandlingsinfoDto behandlingsinfoDto = lagEksternBehandlingInfoDto();
        Optional<EksternBehandlingsinfoDto> optBehandlingsinfo = Optional.of(behandlingsinfoDto);
        when(mockFagsystemKlient.hentBehandlingOptional(any(UUID.class))).thenReturn(optBehandlingsinfo);
        when(mockFagsystemKlient.hentBehandling(any(UUID.class))).thenReturn(behandlingsinfoDto);
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class)))
            .thenReturn(lagSamletEksternBehandlingInfo(behandlingsinfoDto));
        when(mockFagsystemKlient.hentBehandlingsinfo(any(UUID.class), any(Tillegsinformasjon.class), any(Tillegsinformasjon.class)))
            .thenReturn(lagSamletEksternBehandlingInfo(behandlingsinfoDto));
        TestUtility.SakDetaljer sakDetaljer = testUtility.opprettFørstegangsBehandling(aktørId);
        mapSakDetaljer(sakDetaljer);
    }

    protected FaktaFeilutbetaling lagFaktaFeilutbetaling() {
        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();
        FaktaFeilutbetalingPeriode periodeÅrsak = FaktaFeilutbetalingPeriode.builder()
                .medHendelseType(HENDELSE_TYPE)
                .medHendelseUndertype(HENDELSE_UNDERTYPE)
                .medPeriode(FOM, TOM)
                .medFeilutbetalinger(faktaFeilutbetaling)
                .build();
        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(periodeÅrsak);
        faktaFeilutbetaling.setBegrunnelse("begrunnelse");
        return faktaFeilutbetaling;
    }

    protected VilkårsvurderingPerioderDto formVilkårsvurderingPerioderDto(VilkårResultat resultat,
                                                                          LocalDate fom,
                                                                          LocalDate tom,
                                                                          Aktsomhet aktsomhet) {
        VilkårsvurderingPerioderDto perioderDto = new VilkårsvurderingPerioderDto();
        perioderDto.setVilkårResultat(resultat);
        perioderDto.setFom(fom);
        perioderDto.setTom(tom);
        perioderDto.setBegrunnelse("begrunnelse");
        if (resultat == VilkårResultat.GOD_TRO) {
            perioderDto.setVilkarResultatInfo(
                    new VilkårResultatGodTroDto("godTro begrunnelse", true, BigDecimal.valueOf(1000.00)));
        } else {
            VilkårResultatAktsomhetDto aktsomhetDto = new VilkårResultatAktsomhetDto();
            aktsomhetDto.setSærligeGrunner(
                List.of(SærligGrunn.GRAD_AV_UAKTSOMHET, SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL));
            aktsomhetDto.setSærligGrunnerBegrunnelse(SÆRLIG_GRUNNER_BEGRUNNELSE);
            if (aktsomhet == Aktsomhet.GROVT_UAKTSOM) {
                aktsomhetDto.setHarGrunnerTilReduksjon(false);
                aktsomhetDto.setIleggRenter(true);
                aktsomhetDto.setAndelTilbakekreves(BigDecimal.valueOf(
                        100)); //feil verdi, andelTilbakekreves alltid være null når harGrunnerTilReduksjon er true
            } else if (aktsomhet == Aktsomhet.SIMPEL_UAKTSOM) {
                aktsomhetDto.setHarGrunnerTilReduksjon(true);
                aktsomhetDto.setTilbakekrevesBelop(BigDecimal.valueOf(2000.00));
            }
            perioderDto.setVilkarResultatInfo(new VilkårResultatAnnetDto("annet begrunnelse", aktsomhet, aktsomhetDto));
        }
        return perioderDto;
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
        var fagsak = new FagsakDto();
        fagsak.setSaksnummer("123");
        fagsak.setFagsakYtelseType(FagsakYtelseType.FORELDREPENGER);
        return SamletEksternBehandlingInfo.builder(Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.FAGSAK)
                .setGrunninformasjon(behandlingsinfoDto)
                .setPersonopplysninger(lagPersonOpplysningDto())
                .setFagsak(fagsak)
                .build();
    }
}
