package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class LesKravgrunnlagTaskTest extends FellesTestOppsett {


    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjenesteMock = mock(GjenopptaBehandlingTjeneste.class);

    private KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(grunnlagRepository, gjenopptaBehandlingTjenesteMock);
    private KravgrunnlagMapper kravgrunnlagMapper = new KravgrunnlagMapper(tpsAdapterWrapper);

    private LesKravgrunnlagTask lesKravgrunnlagTask = new LesKravgrunnlagTask(mottattXmlRepository, kravgrunnlagTjeneste, prosessTaskRepository,
        kravgrunnlagMapper,eksternBehandlingRepository, fpsakKlientMock);

    private Long kravgrunnlagId;
    private static final Long FPSAK_BEHANDLING_ID = 100000001L;

    @Before
    public void setup() {
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));

        when(tpsAdapterMock.hentAktørIdForPersonIdent(any(PersonIdent.class))).thenReturn(Optional.of(fagsak.getAktørId()));
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_forGyldigBehandling() {
        lagEksternBehandling();
        lesKravgrunnlagTask.doTask(lagProsessTaskData());

        boolean erGrunnlagFinnes = grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId());
        assertThat(erGrunnlagFinnes).isTrue();
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.hentFraEksternId(FPSAK_BEHANDLING_ID);
        assertThat(eksternBehandling).isNotEmpty();
        assertThat(eksternBehandling.get().getInternId()).isEqualTo(behandling.getId());
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_nårBehandlingFinnesIkkeIFpsak() {
        when(fpsakKlientMock.finnesBehandlingIFpsak(FPSAK_BEHANDLING_ID)).thenReturn(false);

        expectedException.expectMessage("FPT-587195");
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_forUgyldigBehandling() {
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ugyldig_referanse.xml"));

        expectedException.expectMessage("FPT-675363");
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
    }

    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(LesKravgrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, String.valueOf(kravgrunnlagId));
        return prosessTaskData;
    }

    private void lagEksternBehandling() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }
}
