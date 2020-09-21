package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjenesteTest.GYLDIG_AKTØR_ID;
import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjenesteTest.GYLDIG_SAKSNR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegMockUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;

public class BehandlingDtoTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repositoryRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);

    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
    private BehandlingTjeneste behandlingTjeneste = mock(BehandlingTjeneste.class);
    private VurdertForeldelseTjeneste foreldelseTjeneste = mock(VurdertForeldelseTjeneste.class);
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
    private VilkårsvurderingRepository vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
    private KravgrunnlagRepository grunnlagRepository = repositoryProvider.getGrunnlagRepository();
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repositoryRule.getEntityManager());
    private BehandlingDtoTjeneste behandlingDtoTjeneste = new BehandlingDtoTjeneste(behandlingTjeneste, foreldelseTjeneste, repositoryProvider, behandlingModellRepository, "fptilbake");

    private Saksnummer saksnummer = new Saksnummer(GYLDIG_SAKSNR);
    private static final LocalDate FOM = LocalDate.now().minusMonths(1);
    private static final LocalDate TOM = LocalDate.now();

    @Before
    public void init(){
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medFaktaSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter",
            "feilutbetalingFakta",
            "feilutbetalingAarsak",
            "opprett-verge",
            "fjern-verge");
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medForeldelseSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORELDELSEVURDERINGSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter",
            "feilutbetalingFakta",
            "feilutbetalingAarsak",
            "perioderForeldelse",
            "vilkarvurderingsperioder",
            "opprett-verge",
            "fjern-verge"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVilkårSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter",
            "feilutbetalingFakta",
            "feilutbetalingAarsak",
            "perioderForeldelse",
            "vilkarvurdering",
            "vilkarvurderingsperioder",
            "opprett-verge",
            "fjern-verge"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVilkårSteg_når_vilkårsvurdering_finnes_allrede() {
        Behandling behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());
        lagVilkårsVurdering(behandling.getId());

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter",
            "feilutbetalingFakta",
            "feilutbetalingAarsak",
            "perioderForeldelse",
            "vilkarvurderingsperioder",
            "vilkarvurdering",
            "opprett-verge",
            "fjern-verge"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVedtakSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        lagFaktaFeilutbetaling(behandling.getId());
        lagVilkårsVurdering(behandling.getId());

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter",
            "feilutbetalingFakta",
            "feilutbetalingAarsak",
            "perioderForeldelse",
            "vilkarvurdering",
            "vilkarvurderingsperioder",
            "beregningsresultat",
            "vedtaksbrev",
            "opprett-verge",
            "fjern-verge"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVedtakSteg_når_henlagt() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.AVSLUTTET);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(behandlingTjeneste.erBehandlingHenlagt(any(Behandling.class))).thenReturn(true);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter"
        );
        assertThat(utvidetBehandlingDto.isKanHenleggeBehandling()).isFalse();
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVergeAksjonspunkt(){
        Behandling behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L, null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);
        assertThat(utvidetBehandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "bytt-behandlende-enhet",
            "opne-for-endringer",
            "henlegg-behandling",
            "gjenoppta-behandling",
            "sett-behandling-pa-vent",
            "endre-pa-vent",
            "lagre-aksjonspunkter",
            "beregne-feilutbetalt-belop",
            "aksjonspunkter",
            "feilutbetalingFakta",
            "feilutbetalingAarsak",
            "opprett-verge",
            "fjern-verge",
            "soeker-verge");
    }

    @Test
    public void skal_hentAlleBehandlinger_medFaktaSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        BehandlingDto behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.IKKE_FASTSATT);


        assertThat(behandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "totrinnskontroll-arsaker-readOnly",
            "brev-maler",
            "brev-bestill",
            "brev-forhandvis",
            "handling-rettigheter",
            "finn-menyvalg-for-verge");
    }

    @Test
    public void skal_hentAlleBehandlinger_medFatteVedtakSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FATTE_VEDTAK, BehandlingStatus.FATTER_VEDTAK);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        BehandlingDto behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.IKKE_FASTSATT);

        assertThat(behandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "totrinnskontroll-arsaker",
            "bekreft-totrinnsaksjonspunkt",
            "brev-maler",
            "brev-bestill",
            "brev-forhandvis",
            "handling-rettigheter",
            "finn-menyvalg-for-verge");
    }

    @Test
    public void skal_hentAlleBehandlinger_medVenterPåGrunnlagSteg() {
        Behandling behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));
        when(behandlingTjeneste.erBehandlingHenlagt(any(Behandling.class))).thenReturn(false);

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertThat(behandlingDtoListe).isNotEmpty();
        assertThat(behandlingDtoListe.size()).isEqualTo(1);

        BehandlingDto behandlingDto = behandlingDtoListe.get(0);
        assertThat(behandlingDto.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(behandlingDto.getType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);

        assertThat(behandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsExactlyInAnyOrder(
            "totrinnskontroll-arsaker-readOnly",
            "brev-maler",
            "brev-bestill",
            "brev-forhandvis",
            "handling-rettigheter",
            "finn-menyvalg-for-verge");

        assertThat(behandlingDto.isKanHenleggeBehandling()).isTrue();
    }

    @Test
    public void skal_hentAlleBehandlinger_når_behandling_er_henlagt() {
        Behandling behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandling(behandling)
            .medBehandlingResultatType(BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD).build();
        repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
        behandling.avsluttBehandling();
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));
        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.HENLAGT);
    }

    @Test
    public void skal_hentAlleBehandlinger_når_behandling_er_avsluttet(){
        Behandling behandling = lagBehandling(BehandlingStegType.IVERKSETT_VEDTAK,BehandlingStatus.IVERKSETTER_VEDTAK);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandling(behandling)
            .medBehandlingResultatType(BehandlingResultatType.FASTSATT).build();
        repositoryProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);
        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtakResultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medAnsvarligSaksbehandler("VL")
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtaksdato(LocalDate.now())
            .medIverksettingStatus(IverksettingStatus.IVERKSATT).build();
        repositoryProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak);
        behandling.avsluttBehandling();
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        assertBehandlingDto(behandling, behandlingDtoListe, BehandlingResultatType.DELVIS_TILBAKEKREVING);
    }

    private Behandling lagBehandling(BehandlingStegType behandlingStegType, BehandlingStatus behandlingStatus) {
        Long fagsakId = fagsakRepository.lagre(Fagsak.opprettNy(saksnummer,
            NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.nb)));
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(fagsakId);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandling.setAnsvarligSaksbehandler("Z991136");
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, behandlingStegType, behandlingStatus);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        lagGrunnlag(behandling.getId());
        return behandling;
    }

    private BehandlingDto assertBehandlingDto(Behandling behandling, List<BehandlingDto> behandlingDtoListe, BehandlingResultatType behandlingResultatType) {
        assertThat(behandlingDtoListe).isNotEmpty();
        assertThat(behandlingDtoListe.size()).isEqualTo(1);

        BehandlingDto behandlingDto = behandlingDtoListe.get(0);
        assertThat(behandlingDto.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(behandlingDto.getType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);
        assertThat(behandlingDto.isKanHenleggeBehandling()).isFalse();
        assertThat(behandlingDto.getBehandlingsresultat().getType()).isEqualByComparingTo(behandlingResultatType);
        return behandlingDto;
    }

    private void assertUtvidetBehandlingDto(UtvidetBehandlingDto utvidetBehandlingDto) {
        assertThat(utvidetBehandlingDto).isNotNull();
        assertThat(utvidetBehandlingDto.getAnsvarligSaksbehandler()).isEqualTo("Z991136");
        assertThat(utvidetBehandlingDto.isBehandlingPåVent()).isFalse();
    }

    private void lagGrunnlag(long behandlingId) {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));
        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(com.google.common.collect.Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

    private void lagFaktaFeilutbetaling(long behandlingId){
        FaktaFeilutbetaling faktaFeilutbetaling = new FaktaFeilutbetaling();
        FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode = FaktaFeilutbetalingPeriode.builder()
            .medPeriode(FOM, TOM)
            .medHendelseType(HendelseType.FP_UTTAK_UTSETTELSE_TYPE)
            .medHendelseUndertype(FpHendelseUnderTyper.ARBEID_HELTID)
            .medFeilutbetalinger(faktaFeilutbetaling)
            .build();
        faktaFeilutbetaling.leggTilFeilutbetaltPeriode(faktaFeilutbetalingPeriode);
        faktaFeilutbetaling.setBegrunnelse("begrunnelse");
        faktaFeilutbetalingRepository.lagre(behandlingId, faktaFeilutbetaling);
    }

    private void lagVilkårsVurdering(long behandlingId) {
        VilkårVurderingEntitet vurdering = new VilkårVurderingEntitet();
        VilkårVurderingPeriodeEntitet p = VilkårVurderingPeriodeEntitet.builder()
            .medPeriode(FOM, TOM)
            .medBegrunnelse("foo")
            .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
            .medVurderinger(vurdering)
            .build();
        p.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.FORSETT)
            .medBegrunnelse("foo")
            .medPeriode(p)
            .build());
        vurdering.leggTilPeriode(p);
        vilkårsvurderingRepository.lagre(behandlingId, vurdering);
    }
}
