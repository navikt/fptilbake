package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class FinnGrunnlagTaskTest extends FellesTestOppsett {

    private KravVedtakStatusRepository kravVedtakStatusRepository = new KravVedtakStatusRepository(repoRule.getEntityManager());

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste = new KravVedtakStatusTjeneste(kravVedtakStatusRepository, repositoryProvider, henleggBehandlingTjeneste, behandlingskontrollTjeneste);
    private KravVedtakStatusMapper kravVedtakStatusMapper = new KravVedtakStatusMapper(tpsAdapterWrapper);

    private FinnGrunnlagTask finnGrunnlagTask = new FinnGrunnlagTask(repositoryProvider, mottattXmlRepository, kravVedtakStatusTjeneste, kravVedtakStatusMapper, kravgrunnlagMapper);

    @Before
    public void setup() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    @Test
    public void skal_finne_og_koble_ny_grunnlag_til_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID), mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
    }

    @Test
    public void skal_finne_og_koble_endre_grunnlag_til_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ENDR.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID), mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isTrue();
        Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandling.getId());
        assertThat(kravgrunnlag431.getKravStatusKode()).isEqualByComparingTo(KravStatusKode.NYTT);
    }

    @Test
    public void skal_finne_og_håndtere_avsl_melding_for_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_AVSL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID), mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.erAvsluttet()).isTrue();
        Optional<Behandlingsresultat> behandlingsresultat = repositoryProvider.getBehandlingresultatRepository().hent(behandling);
        assertThat(behandlingsresultat).isNotEmpty();
        assertThat(behandlingsresultat.get().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
    }

    @Test
    public void skal_finne_og_håndtere_sper_melding_for_behandling() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID), mottattXmlId);
        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
    }

    @Test
    public void skal_finne_og_håndtere_sper_melding_for_behandling_når_finnes_flere_meldinger() {
        Long mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID), mottattXmlId);

        mottattXmlId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        mottattXmlRepository.oppdaterMedEksternBehandlingId(String.valueOf(FPSAK_BEHANDLING_ID), mottattXmlId);

        ProsessTaskData prosessTaskData = opprettFinngrunnlagProsessTask();
        finnGrunnlagTask.doTask(prosessTaskData);

        assertThat(behandling.isBehandlingPåVent()).isTrue();
        assertThat(behandling.getVenteårsak()).isEqualByComparingTo(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        assertThat(kravVedtakStatusRepository.finnKravstatus(behandling.getId())).isNotEmpty();
        assertThat(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId())).isFalse();

    }

    private ProsessTaskData opprettFinngrunnlagProsessTask() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(FinnGrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        return prosessTaskData;
    }

}
