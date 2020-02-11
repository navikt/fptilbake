package no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.testutilities.Whitebox;

/**
 * Default test scenario builder for å definere opp testdata med enkle defaults.
 * <p>
 * Oppretter en default behandling, inkludert default grunnlag med søknad + tomt innangsvilkårresultat.
 * <p>
 * Kan bruke settere (evt. legge til) for å tilpasse utgangspunktet.
 * <p>
 * Mer avansert bruk er ikke gitt at kan bruke denne
 * klassen.
 */
public abstract class AbstractTestScenario<S extends AbstractTestScenario<S>> {

    private static final AtomicLong FAKE_ID = new AtomicLong(100999L);
    private static final AtomicLong FAKE_SN = new AtomicLong(10099L);

    private ArgumentCaptor<Behandling> behandlingCaptor = ArgumentCaptor.forClass(Behandling.class);
    private ArgumentCaptor<Fagsak> fagsakCaptor = ArgumentCaptor.forClass(Fagsak.class);
    private Behandling behandling;
    private Fagsak fagsak;
    private BehandlingStegType startSteg;

    private Long fagsakId = nyId();
    private Saksnummer eksternSaksnummer = new Saksnummer(String.valueOf(nySN()));
    private BehandlingRepository mockBehandlingRepository;
    private BehandlingType behandlingType = BehandlingType.TILBAKEKREVING;
    private BehandlingRepositoryProvider repositoryProvider;
    private Map<AksjonspunktDefinisjon, BehandlingStegType> aksjonspunktDefinisjoner = new HashMap<>();
    private Map<Periode, List<KravgrunnlagTestBuilder.KgBeløp>> kravgrunnlag;
    private VilkårVurderingEntitet vilkårsvurdering;
    private BehandlingResultatType behandlingResultatType;
    private LocalDate vedtaksdato;
    private VedtakResultatType vedtakResultatType;

    protected AbstractTestScenario() {
        AktørId aktørId = new AktørId(nyId());
        NavBruker bruker = NavBruker.opprettNy(aktørId, Språkkode.nb);
        fagsak = lagFagsak(bruker);
    }

    protected AbstractTestScenario(AktørId aktørId) {
        NavBruker bruker = NavBruker.opprettNy(aktørId, Språkkode.nb);
        fagsak = lagFagsak(bruker);
    }

    protected AbstractTestScenario(NavBruker navBruker) {
        fagsak = lagFagsak(navBruker);
    }

    private Fagsak lagFagsak(NavBruker bruker) {
        return Fagsak.opprettNy(eksternSaksnummer, bruker);
    }

    public S medBehandlingResultatType(BehandlingResultatType behandlingResultatType) {
        this.behandlingResultatType = behandlingResultatType;
        return (S) this;
    }

    public S medVedtak(LocalDate vedtaksdato, VedtakResultatType vedtakResultatType) {
        this.vedtaksdato = vedtaksdato;
        this.vedtakResultatType = vedtakResultatType;
        return (S) this;
    }

    public S medDefaultKravgrunnlag() {
        Periode april2019 = Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
        return medKravgrunnlag(Map.of(april2019, Arrays.asList(
            KravgrunnlagTestBuilder.KgBeløp.feil(23000),
            KravgrunnlagTestBuilder.KgBeløp.ytelse(KlasseKode.FPATORD).medUtbetBeløp(23000).medTilbakekrevBeløp(23000)
        )));
    }

    public S medKravgrunnlag(Map<Periode, List<KravgrunnlagTestBuilder.KgBeløp>> kravgrunnlag) {
        this.kravgrunnlag = kravgrunnlag;
        return (S) this;
    }

    public S medFullInnkreving() {
        Periode april2019 = Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet periode = VilkårVurderingPeriodeEntitet.builder()
            .medVurderinger(vurdering)
            .medPeriode(april2019)
            .medBegrunnelse("foo")
            .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
            .build();
        VilkårVurderingAktsomhetEntitet aktsomhet = VilkårVurderingAktsomhetEntitet.builder()
            .medPeriode(periode)
            .medSærligGrunnerTilReduksjon(false)
            .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
            .medProsenterSomTilbakekreves(BigDecimal.valueOf(100))
            .medBegrunnelse("foo")
            .build();
        periode.setAktsomhet(aktsomhet);
        vurdering.leggTilPeriode(periode);
        return medVilkårsvurdering(vurdering);
    }

    public S medIngenInnkreving() {
        Periode april2019 = Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet periode = VilkårVurderingPeriodeEntitet.builder()
            .medVurderinger(vurdering)
            .medPeriode(april2019)
            .medBegrunnelse("foo")
            .medVilkårResultat(VilkårResultat.GOD_TRO)
            .build();
        VilkårVurderingGodTroEntitet godTro = VilkårVurderingGodTroEntitet.builder()
            .medPeriode(periode)
            .medBeløpErIBehold(false)
            .medBegrunnelse("foo")
            .build();
        periode.setGodTro(godTro);
        vurdering.leggTilPeriode(periode);
        return medVilkårsvurdering(vurdering);
    }

    public S medVilkårsvurdering(VilkårVurderingEntitet vilkårsvurdering) {
        this.vilkårsvurdering = vilkårsvurdering;
        return (S) this;
    }


    public S medBehandlingStegStart(BehandlingStegType startSteg) {
        this.startSteg = startSteg;
        return (S) this;
    }

    public S medBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
        return (S) this;
    }

    public void leggTilAksjonspunkt(AksjonspunktDefinisjon apDef, BehandlingStegType stegType) {
        aksjonspunktDefinisjoner.put(apDef, stegType);
    }

    static long nyId() {
        return FAKE_ID.getAndIncrement();
    }

    static long nySN() {
        return FAKE_SN.getAndIncrement();
    }

    private BehandlingRepository lagBasicMockBehandlingRepository(BehandlingRepositoryProvider repositoryProvider) {
        BehandlingRepository behandlingRepository = mock(BehandlingRepository.class);

        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);

        AksjonspunktRepository aksjonspunktRepository = Mockito.spy(new AksjonspunktRepositoryImpl(null));

        // støtter ikke denne, da behandling mulig ikke har aksjonspunkt
        Mockito.doAnswer(new Answer<AksjonspunktDefinisjon>() {
            private List<AksjonspunktDefinisjon> defs;

            @Override
            public AksjonspunktDefinisjon answer(InvocationOnMock invocation) {
                String kode = invocation.getArgument(0);
                if (defs == null) {
                    defs = new KodeverkFraJson().lesKodeverkFraFil(AksjonspunktDefinisjon.class);
                }
                return defs.stream().filter(a -> a.getKode().equals(kode))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ukjent AksjonspunktDefinisjon kode=" + kode));
            }
        }).when(aksjonspunktRepository).finnAksjonspunktDefinisjon(Mockito.any());

        FagsakRepository mockFagsakRepository = mockFagsakRepository();
        KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        // ikke ideelt å la mocks returnere mocks, men forenkler enormt mye test kode, forhindrer feil oppsett, så det
        // blir enklere å refactorere

        when(repositoryProvider.getKodeverkRepository()).thenReturn(kodeverkRepository);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
        when(repositoryProvider.getFagsakRepository()).thenReturn(mockFagsakRepository);
        when(repositoryProvider.getAksjonspunktRepository()).thenReturn(aksjonspunktRepository);

        return behandlingRepository;
    }


    /**
     * Hjelpe metode for å håndtere mock repository.
     */
    public BehandlingRepository mockBehandlingRepository() {
        if (mockBehandlingRepository != null) {
            return mockBehandlingRepository;
        }
        repositoryProvider = mock(BehandlingRepositoryProvider.class);
        BehandlingRepository behandlingRepository = lagBasicMockBehandlingRepository(repositoryProvider);

        when(behandlingRepository.hentBehandling(Mockito.any(Long.class))).thenAnswer(a -> {
            return behandling;
        });
        when(behandlingRepository.hentSisteBehandlingForFagsakId(Mockito.any(), Mockito.any(BehandlingType.class)))
            .thenAnswer(a -> Optional.of(behandling));
        when(behandlingRepository.taSkriveLås(behandlingCaptor.capture())).thenAnswer((Answer<BehandlingLås>) invocation -> {
            Behandling beh = invocation.getArgument(0);
            return new BehandlingLås(beh.getId()) {
            };
        });

        when(behandlingRepository.lagre(behandlingCaptor.capture(), Mockito.any()))
            .thenAnswer((Answer<Long>) invocation -> {
                Behandling beh = invocation.getArgument(0);
                Long id = beh.getId();
                if (id == null) {
                    id = nyId();
                    Whitebox.setInternalState(beh, "id", id);
                }

                beh.getAksjonspunkter().forEach(punkt -> Whitebox.setInternalState(punkt, "id", nyId()));

                return id;
            });

        mockBehandlingRepository = behandlingRepository;
        return behandlingRepository;
    }

    public BehandlingRepositoryProvider mockBehandlingRepositoryProvider() {
        mockBehandlingRepository();
        return repositoryProvider;
    }


    public FagsakRepository mockFagsakRepository() {
        FagsakRepository fagsakRepository = mock(FagsakRepository.class);
        when(fagsakRepository.finnEksaktFagsak(Mockito.anyLong())).thenAnswer(a -> fagsak);
        when(fagsakRepository.finnUnikFagsak(Mockito.anyLong())).thenAnswer(a -> Optional.of(fagsak));
        when(fagsakRepository.hentSakGittSaksnummer(Mockito.any(Saksnummer.class))).thenAnswer(a -> Optional.of(fagsak));
        when(fagsakRepository.hentForBruker(Mockito.any(AktørId.class))).thenAnswer(a -> singletonList(fagsak));
        when(fagsakRepository.lagre(fagsakCaptor.capture())).thenAnswer(invocation -> {
            Fagsak fagsak = invocation.getArgument(0); // NOSONAR
            Long id = fagsak.getId();
            if (id == null) {
                id = fagsakId;
                Whitebox.setInternalState(fagsak, "id", id);
            }
            return id;
        });

        // oppdater fagsakstatus
        Mockito.doAnswer(invocation -> {
            FagsakStatus status = invocation.getArgument(1);
            Whitebox.setInternalState(fagsak, "fagsakStatus", status);
            return null;
        }).when(fagsakRepository)
            .oppdaterFagsakStatus(eq(fagsakId), Mockito.any(FagsakStatus.class));

        return fagsakRepository;
    }

    public Fagsak lagreFagsak(BehandlingRepositoryProvider repositoryProvider) {
        lagFagsak(repositoryProvider.getFagsakRepository());
        return fagsak;
    }

    public Behandling lagre(BehandlingRepositoryProvider repositoryProvider) {
        build(repositoryProvider.getBehandlingRepository(), repositoryProvider);
        if (kravgrunnlag != null || vilkårsvurdering != null) {
            repositoryProvider.getGrunnlagRepository().getEntityManager().flush();
        }
        return behandling;
    }

    BehandlingRepository lagMockedRepositoryForOpprettingAvBehandlingInternt() {
        if (mockBehandlingRepository != null && behandling != null) {
            return mockBehandlingRepository;
        }
        mockBehandlingRepository = mockBehandlingRepository();

        lagre(repositoryProvider); // NOSONAR //$NON-NLS-1$
        Whitebox.setInternalState(behandling.getType(), "ekstraData", "{ \"behandlingstidFristUker\" : 6, \"behandlingstidVarselbrev\" : \"N\" }");
        return mockBehandlingRepository;
    }

    public Behandling lagMocked() {
        lagMockedRepositoryForOpprettingAvBehandlingInternt();
        return behandling;
    }

    private void build(BehandlingRepository behandlingRepo, BehandlingRepositoryProvider repositoryProvider) {
        if (behandling != null) {
            throw new IllegalStateException("build allerede kalt.  Hent Behandling via getBehandling eller opprett nytt scenario.");
        }

        Behandling.Builder behandlingBuilder = grunnBuild(repositoryProvider);

        this.behandling = behandlingBuilder.build();

        if (startSteg != null) {
            new InternalManipulerBehandlingImpl(repositoryProvider).forceOppdaterBehandlingSteg(behandling, startSteg);
        }
        leggTilAksjonspunkter(behandling, repositoryProvider);

        BehandlingLås lås = behandlingRepo.taSkriveLås(behandling);
        behandlingRepo.lagre(behandling, lås);

        if (kravgrunnlag != null) {
            KravgrunnlagTestBuilder.medRepo(repositoryProvider.getGrunnlagRepository()).lagreKravgrunnlag(behandling.getId(), kravgrunnlag);
        }
        if (vilkårsvurdering != null) {
            repositoryProvider.getVilkårsvurderingRepository().lagre(behandling.getId(), vilkårsvurdering);
        }
        if (behandlingResultatType != null) {
            Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
                .medBehandling(behandling)
                .medBehandlingResultatType(behandlingResultatType)
                .build();
            repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);

            if (vedtaksdato != null) {
                BehandlingVedtak vedtak = BehandlingVedtak.builder()
                    .medBehandlingsresultat(behandlingsresultat)
                    .medVedtakResultat(vedtakResultatType)
                    .medVedtaksdato(vedtaksdato)
                    .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
                    .medAnsvarligSaksbehandler("Z111111")
                    .build();
                repositoryProvider.getBehandlingVedtakRepository().lagre(vedtak);
            }
        }
    }


    private Behandling.Builder grunnBuild(BehandlingRepositoryProvider repositoryProvider) {
        FagsakRepository fagsakRepo = repositoryProvider.getFagsakRepository();
        lagFagsak(fagsakRepo);

        // oppprett og lagre behandling
        Behandling.Builder behandlingBuilder;
        behandlingBuilder = Behandling.nyBehandlingFor(fagsak, behandlingType);
        return behandlingBuilder;

    }

    protected void lagFagsak(FagsakRepository fagsakRepo) {
        fagsakRepo.lagre(fagsak); // NOSONAR //$NON-NLS-1$
    }


    public Fagsak getFagsak() {
        if (fagsak == null) {
            throw new IllegalStateException("Kan ikke hente Fagsak før denne er bygd");
        }
        return fagsak;
    }

    public Behandling getBehandling() {
        if (behandling == null) {
            throw new IllegalStateException("Kan ikke hente Behandling før denne er bygd");
        }
        return behandling;
    }

    private void leggTilAksjonspunkter(Behandling behandling, BehandlingRepositoryProvider repositoryProvider) {
        aksjonspunktDefinisjoner.forEach(
            (apDef, stegType) -> {
                if (stegType != null) {
                    repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef, stegType);
                } else {
                    repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef);
                }
            });
    }


}
