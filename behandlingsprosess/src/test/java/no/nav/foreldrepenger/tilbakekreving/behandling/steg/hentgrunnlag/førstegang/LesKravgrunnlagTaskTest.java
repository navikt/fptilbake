package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class LesKravgrunnlagTaskTest extends FellesTestOppsett {

    private Long kravgrunnlagId;

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
        Optional<EksternBehandling> eksternBehandling = eksternBehandlingRepository.hentFraHenvisning(HENVISNING);
        assertThat(eksternBehandling).isNotEmpty();
        assertThat(eksternBehandling.get().getInternId()).isEqualTo(behandling.getId());
        assertTilkobling();
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_nårBehandlingFinnesIkkeIFpsak() {
        when(fpsakKlientMock.finnesBehandlingIFpsak(fagsak.getSaksnummer().getVerdi(), HENVISNING)).thenReturn(false);

        expectedException.expectMessage("FPT-587195");
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_forUgyldigBehandling() {
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ugyldig_referanse.xml"));

        expectedException.expectMessage("Mottok et tilbakekrevingsgrunnlag fra Økonomi med henvisning som ikke er i støttet format. henvisning=ABC. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!");
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
    }

    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(LesKravgrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, String.valueOf(kravgrunnlagId));
        return prosessTaskData;
    }

    private void lagEksternBehandling() {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, HENVISNING, FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

    private void assertTilkobling() {
        Optional<ØkonomiXmlMottatt> økonomiXmlMottatt = mottattXmlRepository.finnForHenvisning(HENVISNING);
        assertThat(økonomiXmlMottatt).isPresent();
        assertThat(økonomiXmlMottatt.get().isTilkoblet()).isTrue();
    }
}
