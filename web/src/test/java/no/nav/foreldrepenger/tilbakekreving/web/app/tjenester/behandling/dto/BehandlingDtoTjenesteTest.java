package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjenesteTest.GYLDIG_AKTØR_ID;
import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjenesteTest.GYLDIG_SAKSNR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;

public class BehandlingDtoTjenesteTest {

    private BehandlingTjeneste behandlingTjeneste = mock(BehandlingTjeneste.class);

    private VurdertForeldelseTjeneste foreldelseTjeneste = mock(VurdertForeldelseTjeneste.class);

    private BehandlingDtoTjeneste behandlingDtoTjeneste = new BehandlingDtoTjeneste(behandlingTjeneste, foreldelseTjeneste);

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private Saksnummer saksnummer;

    private BehandlingRepository behandlingRepository;

    private FagsakRepository fagsakRepository;

    @Before
    public void setup() {
        saksnummer = new Saksnummer(GYLDIG_SAKSNR);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fagsakRepository = repositoryProvider.getFagsakRepository();
    }

    @Test
    public void skal_hentAlleBehandlinger_medFaktaSteg() {
        Behandling behandling = mockBehandling();
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        BehandlingDto behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe);

        assertThat(behandlingDto.getLinks()).isNotEmpty();
        assertThat(behandlingDto.getLinks().size()).isEqualTo(6);
        List<ResourceLink> lenker = behandlingDto.getLinks();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("totrinnskontroll-arsaker-readOnly")).findFirst()).isPresent();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("totrinnskontroll-arsaker")).findFirst()).isEmpty();
    }

    @Test
    public void skal_hentAlleBehandlinger_medFatteVedtakSteg() {
        Behandling behandling = mockBehandling();
        BehandlingStegMockUtil.nyBehandlingSteg(behandling, BehandlingStegType.FATTE_VEDTAK, BehandlingStatus.FATTER_VEDTAK);
        when(behandlingTjeneste.hentBehandlinger(saksnummer)).thenReturn(Lists.newArrayList(behandling));

        List<BehandlingDto> behandlingDtoListe = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        BehandlingDto behandlingDto = assertBehandlingDto(behandling, behandlingDtoListe);

        assertThat(behandlingDto.getLinks()).isNotEmpty();
        assertThat(behandlingDto.getLinks().size()).isEqualTo(7);
        List<ResourceLink> lenker = behandlingDto.getLinks();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("totrinnskontroll-arsaker-readOnly")).findFirst()).isEmpty();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("totrinnskontroll-arsaker")).findFirst()).isPresent();
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medFaktaSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L,null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks()).isNotEmpty();
        assertThat(utvidetBehandlingDto.getLinks().size()).isEqualTo(5);
        List<ResourceLink> lenker = utvidetBehandlingDto.getLinks();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("feilutbetalingFakta")).findFirst()).isPresent();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("feilutbetalingAarsak")).findFirst()).isPresent();
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medForeldelseSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORELDELSEVURDERINGSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L,null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks()).isNotEmpty();
        assertThat(utvidetBehandlingDto.getLinks().size()).isEqualTo(6);
        List<ResourceLink> lenker = utvidetBehandlingDto.getLinks();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("perioderForeldelse")).findFirst()).isPresent();
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVilkårSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.VTILBSTEG, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L,null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks()).isNotEmpty();
        assertThat(utvidetBehandlingDto.getLinks().size()).isEqualTo(8);
        List<ResourceLink> lenker = utvidetBehandlingDto.getLinks();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("vilkarvurderingsperioder")).findFirst()).isPresent();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("vilkarvurdering")).findFirst()).isPresent();
    }

    @Test
    public void skal_hentUtvidetBehandlingResultat_medVedtakSteg() {
        Behandling behandling = lagBehandling(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingStatus.UTREDES);
        when(behandlingTjeneste.hentBehandling(anyLong())).thenReturn(behandling);

        UtvidetBehandlingDto utvidetBehandlingDto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(1L,null);
        assertUtvidetBehandlingDto(utvidetBehandlingDto);

        assertThat(utvidetBehandlingDto.getLinks()).isNotEmpty();
        assertThat(utvidetBehandlingDto.getLinks().size()).isEqualTo(10);
        List<ResourceLink> lenker = utvidetBehandlingDto.getLinks();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("vedtaksbrev")).findFirst()).isPresent();
        assertThat(lenker.stream().filter(resourceLink -> resourceLink.getRel().equals("beregningsresultat")).findFirst()).isPresent();
    }

    private Behandling mockBehandling() {
        return Behandling.nyBehandlingFor(
            Fagsak.opprettNy(1l, saksnummer, NavBruker.opprettNy(new AktørId(GYLDIG_AKTØR_ID), Språkkode.nb)),
            BehandlingType.TILBAKEKREVING).build();
    }

    private Behandling lagBehandling(BehandlingStegType behandlingStegType, BehandlingStatus behandlingStatus) {
        Long fagsakId = fagsakRepository.lagre(Fagsak.opprettNy(1l, saksnummer,
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
