package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegMockUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktTestSupport;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.TotrinnskontrollAksjonspunkterTjeneste;

@ExtendWith(JpaExtension.class)
class BehandlingDtoTjenesteTest {

    static final String GYLDIG_AKTØR_ID = "12345678901";
    static final String GYLDIG_SAKSNR = "123456";

    private BehandlingRepositoryProvider repositoryProvider;

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingTjeneste behandlingTjeneste;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingDtoTjeneste behandlingDtoTjeneste;

    private final Saksnummer saksnummer = new Saksnummer(GYLDIG_SAKSNR);
    private static final LocalDate FOM = LocalDate.now().minusMonths(1);
    private static final LocalDate TOM = LocalDate.now();

    @BeforeEach
    void init(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingTjeneste = mock(BehandlingTjeneste.class);
        var foreldelseTjeneste = mock(VurdertForeldelseTjeneste.class);
        faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        var behandlingModellRepository = new BehandlingModellRepository();
        behandlingDtoTjeneste = new BehandlingDtoTjeneste(behandlingTjeneste, mock(TotrinnTjeneste.class),
            mock(TotrinnskontrollAksjonspunkterTjeneste.class), mock(HenleggBehandlingTjeneste.class), foreldelseTjeneste,
            mock(BeregningsresultatTjeneste.class), repositoryProvider, behandlingModellRepository, Fagsystem.FPTILBAKE);

        entityManager.setFlushMode(FlushModeType.AUTO);
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medFaktaSteg() {
        var behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList());
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter", "feilutbetalingFakta",
            "feilutbetalingAarsak", "opprett-verge", "fjern-verge")));
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medForeldelseSteg() {
        var behandling = lagBehandling(BehandlingStegType.FORELDELSEVURDERINGSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter", "feilutbetalingFakta",
            "feilutbetalingAarsak", "perioderForeldelse", "vilkarvurderingsperioder", "opprett-verge", "fjern-verge")));
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medVilkårSteg() {
        var behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter", "feilutbetalingFakta",
            "feilutbetalingAarsak", "perioderForeldelse", "vilkarvurdering", "vilkarvurderingsperioder", "opprett-verge", "fjern-verge")));
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medVilkårSteg_når_vilkårsvurdering_finnes_allrede() {
        var behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());
        lagVilkårsVurdering(behandling.getId());

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter", "feilutbetalingFakta",
            "feilutbetalingAarsak", "perioderForeldelse", "vilkarvurderingsperioder", "vilkarvurdering", "opprett-verge", "fjern-verge")));
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medVedtakSteg() {
        var behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());
        lagVilkårsVurdering(behandling.getId());

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter", "feilutbetalingFakta",
            "feilutbetalingAarsak", "perioderForeldelse", "vilkarvurdering", "vilkarvurderingsperioder", "beregningsresultat", "vedtaksbrev",
            "opprett-verge", "fjern-verge")));
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medVedtakSteg_når_henlagt() {
        var behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.AVSLUTTET);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(behandlingTjeneste.erBehandlingHenlagt(any(Behandling.class))).thenReturn(true);

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter")));
        assertFalse(utvidetBehandlingDto.isKanHenleggeBehandling());
    }

    @Test
    void skal_hentUtvidetBehandlingResultat_medVergeAksjonspunkt() {
        var behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        AksjonspunktTestSupport.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE);

        var utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        var lenker = utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("bytt-behandlende-enhet", "opne-for-endringer", "henlegg-behandling", "gjenoppta-behandling",
            "sett-behandling-pa-vent", "endre-pa-vent", "lagre-aksjonspunkter", "beregne-feilutbetalt-belop", "aksjonspunkter", "feilutbetalingFakta",
            "feilutbetalingAarsak", "opprett-verge", "fjern-verge", "soeker-verge")));
    }

    @Test
    void skal_hentAlleBehandlinger_medFaktaSteg() {
        var behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(List.of(behandling));

        var behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        var behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.IKKE_FASTSATT);

        var lenker = behandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(List.of("totrinnskontroll-arsaker-readOnly", "brev-maler", "brev-bestill", "brev-forhandvis", "handling-rettigheter",
                "behandling-rettigheter", "finn-menyvalg-for-verge")));
    }

    @Test
    void skal_hentAlleBehandlinger_medFatteVedtakSteg() {
        var behandling = lagBehandling(BehandlingStegType.FATTE_VEDTAK, BehandlingStatus.FATTER_VEDTAK);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(List.of(behandling));

        var behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        var behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.IKKE_FASTSATT);

        var lenker = behandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(
            List.of("totrinnskontroll-arsaker", "bekreft-totrinnsaksjonspunkt", "brev-maler", "brev-bestill", "brev-forhandvis",
                "handling-rettigheter", "behandling-rettigheter", "finn-menyvalg-for-verge")));
    }

    @Test
    void skal_hentAlleBehandlinger_medVenterPåGrunnlagSteg_men_opprettet_nå() {
        var behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(List.of(behandling));
        when(behandlingTjeneste.erBehandlingHenlagt(any(Behandling.class))).thenReturn(false);

        var behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertFalse(behandlingDtoListe.isEmpty());
        assertEquals(1, behandlingDtoListe.size());

        var behandlingDto = behandlingDtoListe.get(0);
        assertEquals(behandlingDto.getFagsakId(), behandling.getFagsakId());
        assertEquals(BehandlingType.TILBAKEKREVING, behandlingDto.getType());

        var lenker = behandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(
            List.of("totrinnskontroll-arsaker-readOnly", "brev-maler", "brev-bestill", "brev-forhandvis", "handling-rettigheter",
                "behandling-rettigheter", "finn-menyvalg-for-verge")));

        assertFalse(behandlingDto.isKanHenleggeBehandling());
    }

    @Test
    void skal_hentAlleBehandlinger_medVenterPåGrunnlagSteg_men_opprettet_før_bestemte_dager() {
        var fagsak = ScenarioSimple.simple().lagreFagsak(repositoryProvider);
        var behandling = mock(Behandling.class);
        when(behandling.getOpprettetTidspunkt()).thenReturn(LocalDateTime.now().minusDays(8l));
        when(behandling.getFagsak()).thenReturn(fagsak);
        when(behandling.getType()).thenReturn(BehandlingType.TILBAKEKREVING);
        when(behandling.getStatus()).thenReturn(BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(List.of(behandling));
        when(behandlingTjeneste.erBehandlingHenlagt(any(Behandling.class))).thenReturn(false);

        var behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertFalse(behandlingDtoListe.isEmpty());
        assertEquals(1, behandlingDtoListe.size());

        var behandlingDto = behandlingDtoListe.get(0);
        assertEquals(behandlingDto.getFagsakId(), behandling.getFagsakId());
        assertEquals(BehandlingType.TILBAKEKREVING, behandlingDto.getType());

        var lenker = behandlingDto.getLinks().stream().map(ResourceLink::getRel).toList();
        assertTrue(lenker.containsAll(
            List.of("totrinnskontroll-arsaker-readOnly", "brev-maler", "brev-bestill", "brev-forhandvis", "handling-rettigheter",
                "behandling-rettigheter", "finn-menyvalg-for-verge")));

        assertTrue(behandlingDto.isKanHenleggeBehandling());
    }

    @Test
    void skal_hentAlleBehandlinger_når_behandling_er_henlagt() {
        var behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        var behandlingsresultat = Behandlingsresultat.builder()
            .medBehandling(behandling)
            .medBehandlingResultatType(
                no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD)
            .build();
        repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
        behandling.avsluttBehandling();
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(List.of(behandling));
        var behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.HENLAGT);
    }

    @Test
    void skal_hentAlleBehandlinger_når_behandling_er_avsluttet() {
        var behandling = lagBehandling(BehandlingStegType.IVERKSETT_VEDTAK, BehandlingStatus.IVERKSETTER_VEDTAK);
        var behandlingsresultat = Behandlingsresultat.builder()
            .medBehandling(behandling)
            .medBehandlingResultatType(BehandlingResultatType.DELVIS_TILBAKEBETALING)
            .build();
        repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
        var behandlingVedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler("VL")
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtaksdato(LocalDate.now())
            .medIverksettingStatus(IverksettingStatus.IVERKSATT)
            .build();
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak);
        behandling.avsluttBehandling();
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(List.of(behandling));

        var behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.DELVIS_TILBAKEBETALING);
    }

    private Behandling lagBehandling(BehandlingStegType behandlingStegType, BehandlingStatus behandlingStatus) {
        var fagsakId = fagsakRepository.lagre(Fagsak.opprettNy(saksnummer, NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.nb)));
        var fagsak = fagsakRepository.finnEksaktFagsak(fagsakId);
        var behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandling.setAnsvarligSaksbehandler("Z991136");
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, behandlingStegType, behandlingStatus);
        var lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        lagGrunnlag(behandling.getId());
        return behandling;
    }

    private BehandlingDto assertBehandlingDto(Behandling behandling,
                                              List<BehandlingDto> behandlingDtoListe,
                                              BehandlingResultatType behandlingResultatType) {
        assertFalse(behandlingDtoListe.isEmpty());
        assertEquals(1, behandlingDtoListe.size());

        var behandlingDto = behandlingDtoListe.get(0);
        assertEquals(behandlingDto.getFagsakId(), behandling.getFagsakId());
        assertEquals(BehandlingType.TILBAKEKREVING, behandlingDto.getType());
        assertFalse(behandlingDto.isKanHenleggeBehandling());
        assertEquals(behandlingDto.getBehandlingsresultat().getType(), behandlingResultatType);
        return behandlingDto;
    }

    private void assertUtvidetBehandlingDto(UtvidetBehandlingDto utvidetBehandlingDto) {
        assertNotNull(utvidetBehandlingDto);
        assertEquals("Z991136", utvidetBehandlingDto.getAnsvarligSaksbehandler());
        assertFalse(utvidetBehandlingDto.isBehandlingPåVent());
    }

    private void lagGrunnlag(long behandlingId) {
        var mockMedFeilPostering = new KravgrunnlagMock(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        var mockMedYtelPostering = new KravgrunnlagMock(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));
        var kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedYtelPostering));
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

    private void lagFaktaFeilutbetaling(long behandlingId) {
        var faktaFeilutbetaling = new FaktaFeilutbetaling();
        var faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder()
            .medPeriode(FOM, TOM)
            .medHendelseType(HendelseType.FP_UTTAK_UTSETTELSE_TYPE)
            .medHendelseUndertype(HendelseUnderType.ARBEID_HELTID)
            .medFeilutbetalinger(faktaFeilutbetaling)
            .build();
        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);
        faktaFeilutbetaling.setBegrunnelse("begrunnelse");
        faktaFeilutbetalingRepository.lagre(behandlingId, faktaFeilutbetaling);
    }

    private void lagVilkårsVurdering(long behandlingId) {
        var vurdering = new VilkårVurderingEntitet();
        var p = VilkårVurderingPeriodeEntitet.builder()
            .medPeriode(FOM, TOM)
            .medBegrunnelse("foo")
            .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
            .medVurderinger(vurdering)
            .build();
        p.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder().medAktsomhet(Aktsomhet.FORSETT).medBegrunnelse("foo").medPeriode(p).build());
        vurdering.leggTilPeriode(p);
        vilkårsvurderingRepository.lagre(behandlingId, vurdering);
    }
}
