package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TpsAdapterWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class ForvaltningBehandlingRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private ØkonomiMottattXmlRepository mottattXmlRepository = new ØkonomiMottattXmlRepository(repositoryRule.getEntityManager());
    private TpsAdapterWrapper mockTpsAdapterWrapper = mock(TpsAdapterWrapper.class);
    private KravgrunnlagMapper kravgrunnlagMapper = new KravgrunnlagMapper(mockTpsAdapterWrapper);
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, mock(BehandlingModellRepository.class), mock(BehandlingskontrollEventPubliserer.class));
    private ForvaltningBehandlingRestTjeneste forvaltningBehandlingRestTjeneste = new ForvaltningBehandlingRestTjeneste(repositoryProvider, prosessTaskRepository, mottattXmlRepository, kravgrunnlagMapper);

    @Test
    public void skal_ikke_tvinge_henlegg_behandling_når_behandling_er_allerede_avsluttet() {
        Behandling behandling = lagBehandling();
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_tvinge_henlegg_behandling() {
        Behandling behandling = lagBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingHenleggelseBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(TvingHenlegglBehandlingTask.TASKTYPE);
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_er_avsluttet() {
        Behandling behandling = lagBehandling();
        behandling.avsluttBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling_når_behandling_ikke_er_på_vent() {
        Behandling behandling = lagBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_gjenoppta_behandling() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        Response response = forvaltningBehandlingRestTjeneste.tvingGjenopptaBehandling(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG, ProsessTaskStatus.KLAR);
        assertThat(prosessTasker).isNotEmpty();
        assertThat(prosessTasker.size()).isEqualTo(1);
        assertThat(prosessTasker.get(0).getTaskType()).isEqualTo(GjenopptaBehandlingTask.TASKTYPE);
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_behandling_er_ikke_på_vent() {
        Behandling behandling = lagBehandling();

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(behandling.getId(), 1l));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_behandling_er_ikke_på_vent_på_tilbakekrevingsgrunnlag() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(behandling.getId(), 1l));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_mottattXml_er_status_melding() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml("<?xml version=\"1.0\" encoding=\"utf-8\"?><urn:endringKravOgVedtakstatus xmlns:urn=\"urn:no:nav:tilbakekreving:status:v1\"/>");

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_mottattXml_er_allerede_koblet() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(true));
        mottattXmlRepository.opprettTilkobling(mottattXmlId);

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void skal_tvinge_koble_grunnlag_når_mottattXml_er_grunnlag() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(true));

        when(mockTpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(anyString(), any(GjelderType.class))).thenReturn("123");

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(repositoryProvider.getGrunnlagRepository().harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isTrue();
    }

    @Test
    public void skal_ikke_tvinge_koble_grunnlag_når_kravgrunnlaget_er_ugyldig() {
        Behandling behandling = lagBehandling();
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(3), Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getKravgrunnlagXml(false));

        when(mockTpsAdapterWrapper.hentAktørIdEllerOrganisajonNummer(anyString(), any(GjelderType.class))).thenReturn("123");

        Response response = forvaltningBehandlingRestTjeneste.tvingkobleBehandlingTilGrunnlag(new KobleBehandlingTilGrunnlagDto(behandling.getId(), mottattXmlId));
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(repositoryProvider.getGrunnlagRepository().harGrunnlagForBehandlingId(behandling.getId())).isFalse();
        assertThat(mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)).isFalse();
    }


    private Behandling lagBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }

    private String getKravgrunnlagXml(boolean gyldig) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<urn:detaljertKravgrunnlagMelding xmlns:urn=\"urn:no:nav:tilbakekreving:kravgrunnlag:detalj:v1\"\n" +
            "                                  xmlns:mmel=\"urn:no:nav:tilbakekreving:typer:v1\">\n" +
            "    <urn:detaljertKravgrunnlag>\n" +
            "        <urn:kravgrunnlagId>123456789</urn:kravgrunnlagId>\n" +
            "        <urn:vedtakId>100</urn:vedtakId>\n" +
            "        <urn:kodeStatusKrav>NY</urn:kodeStatusKrav>\n" +
            "        <urn:kodeFagomraade>FP</urn:kodeFagomraade>\n" +
            "        <urn:fagsystemId>100001</urn:fagsystemId>\n" +
            "        <!--Optional:-->\n" +
            "        <urn:datoVedtakFagsystem>2007-10-26</urn:datoVedtakFagsystem>\n" +
            "        <urn:vedtakGjelderId>12345678901</urn:vedtakGjelderId>\n" +
            "        <urn:typeGjelderId>PERSON</urn:typeGjelderId>\n" +
            "        <urn:utbetalesTilId>12345678901</urn:utbetalesTilId>\n" +
            "        <urn:typeUtbetId>PERSON</urn:typeUtbetId>\n" +
            "        <urn:enhetAnsvarlig>8020</urn:enhetAnsvarlig>\n" +
            "        <urn:enhetBosted>8020</urn:enhetBosted>\n" +
            "        <urn:enhetBehandl>8020</urn:enhetBehandl>\n" +
            "        <urn:kontrollfelt>kontrolll-123</urn:kontrollfelt>\n" +
            "        <urn:saksbehId>Z111111</urn:saksbehId>\n" +
            "        <!--Optional:-->\n" +
            "        <urn:referanse>100000001</urn:referanse>\n" +
            "        <!--1 or more repetitions:-->\n" +
            "        <urn:tilbakekrevingsPeriode>\n" +
            "            <urn:periode>\n" +
            "                <mmel:fom>2018-11-01+01:00</mmel:fom>\n" +
            "                <mmel:tom>2018-11-22+02:00</mmel:tom>\n" +
            "            </urn:periode>\n" +
            "            <urn:belopSkattMnd>0.00</urn:belopSkattMnd>\n" +
            "            <!--1 or more repetitions:-->\n" +
            "            <urn:tilbakekrevingsBelop>\n" +
            "                <urn:kodeKlasse>FPATORD</urn:kodeKlasse>\n" +
            "                <urn:typeKlasse>YTEL</urn:typeKlasse>\n" +
            "                <urn:belopOpprUtbet>9000.00</urn:belopOpprUtbet>\n" +
            "                <urn:belopNy>0.00</urn:belopNy>\n" +
            "                <urn:belopTilbakekreves>9000.00</urn:belopTilbakekreves>\n" +
            String.format("  <urn:skattProsent>%d.0000</urn:skattProsent>\n", gyldig ? 0 : 100) +
            "            </urn:tilbakekrevingsBelop>\n" +
            "            <urn:tilbakekrevingsBelop>\n" +
            "                <urn:kodeKlasse>KL_KODE_FEIL_KORTTID</urn:kodeKlasse>\n" +
            "                <urn:typeKlasse>FEIL</urn:typeKlasse>\n" +
            "                <urn:belopNy>9000.00</urn:belopNy>\n" +
            "                <urn:belopTilbakekreves>0.00</urn:belopTilbakekreves>\n" +
            "                <urn:skattProsent>0.0000</urn:skattProsent>\n" +
            "            </urn:tilbakekrevingsBelop>\n" +
            "        </urn:tilbakekrevingsPeriode>\n" +
            "    </urn:detaljertKravgrunnlag>\n" +
            "</urn:detaljertKravgrunnlagMelding>\n";
    }
}
