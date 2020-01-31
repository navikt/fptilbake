package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjenesteTest.GYLDIG_AKTØR_ID;
import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjenesteTest.GYLDIG_SAKSNR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegMockUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
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
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository = mock(FaktaFeilutbetalingRepository.class);
    private VilkårsvurderingRepository vilkårsvurderingRepository = mock(VilkårsvurderingRepository.class);
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repositoryRule.getEntityManager());
    private BehandlingDtoTjeneste behandlingDtoTjeneste = new BehandlingDtoTjeneste(behandlingTjeneste, foreldelseTjeneste, faktaFeilutbetalingRepository, vilkårsvurderingRepository, behandlingModellRepository);

    private Saksnummer saksnummer = new Saksnummer(GYLDIG_SAKSNR);

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
            "feilutbetalingAarsak");
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medForeldelseSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORELDELSEVURDERINGSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(anyLong())).thenReturn(true);

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
            "vilkarvurderingsperioder"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVilkårSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(anyLong())).thenReturn(true);
        when(vilkårsvurderingRepository.harDataForVilkårsvurdering(anyLong())).thenReturn(false);

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
            "vilkarvurderingsperioder"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVilkårSteg_når_vilkårsvurdering_finnes_allrede() {
        Behandling behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(anyLong())).thenReturn(true);
        when(vilkårsvurderingRepository.harDataForVilkårsvurdering(anyLong())).thenReturn(true);

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
            "vilkarvurdering"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVedtakSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(anyLong())).thenReturn(true);
        when(vilkårsvurderingRepository.harDataForVilkårsvurdering(anyLong())).thenReturn(true);

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
            "vedtaksbrev"
        );
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVedtakSteg_når_henlagt() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.AVSLUTTET);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);
        when(behandlingTjeneste.erBehandlingHenlagt(any(Behandling.class))).thenReturn(true);
        when(faktaFeilutbetalingRepository.harDataForFaktaFeilutbetaling(anyLong())).thenReturn(false);
        when(vilkårsvurderingRepository.harDataForVilkårsvurdering(anyLong())).thenReturn(false);

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
    }

    @Test
    public void skal_hentAlleBehandlinger_medFaktaSteg() {
        Behandling behandling = mockBehandling();
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        BehandlingDto behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe);

        assertThat(behandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "totrinnskontroll-arsaker-readOnly",
            "brev-maler",
            "brev-bestill",
            "brev-forhandvis",
            "handling-rettigheter");
    }

    @Test
    public void skal_hentAlleBehandlinger_medFatteVedtakSteg() {
        Behandling behandling = mockBehandling();
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, BehandlingStegType.FATTE_VEDTAK, BehandlingStatus.FATTER_VEDTAK);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        BehandlingDto behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe);

        assertThat(behandlingDto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).containsOnly(
            "totrinnskontroll-arsaker",
            "bekreft-totrinnsaksjonspunkt",
            "brev-maler",
            "brev-bestill",
            "brev-forhandvis",
            "handling-rettigheter");
    }

    private Behandling mockBehandling() {
        return Behandling.nyBehandlingFor(
            Fagsak.opprettNy(saksnummer, NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.nb)),
            BehandlingType.TILBAKEKREVING).build();
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
        return behandling;
    }

    private BehandlingDto assertBehandlingDto(Behandling behandling, List<BehandlingDto> behandlingDtoListe) {
        assertThat(behandlingDtoListe).isNotEmpty();
        assertThat(behandlingDtoListe.size()).isEqualTo(1);

        BehandlingDto behandlingDto = behandlingDtoListe.get(0);
        assertThat(behandlingDto.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(behandlingDto.getType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);
        return behandlingDto;
    }

    private void assertUtvidetBehandlingDto(UtvidetBehandlingDto utvidetBehandlingDto) {
        assertThat(utvidetBehandlingDto).isNotNull();
        assertThat(utvidetBehandlingDto.getAnsvarligSaksbehandler()).isEqualTo("Z991136");
        assertThat(utvidetBehandlingDto.isBehandlingPåVent()).isFalse();
    }
}
