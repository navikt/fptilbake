package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
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
        Behandling behandling = lagBehandling();
        lesKravgrunnlagTask.doTask(lagProsessTaskData());

        assertTrue(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId()));
        assertTilkobling();
        assertFalse(behandling.isBehandlingPåVent());
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_nårBehandlingFinnesIkkeIFpsak() {
        when(fagsystemKlientMock.finnesBehandlingIFagsystem(fagsak.getSaksnummer().getVerdi(), HENVISNING)).thenReturn(false);

        expectedException.expectMessage("FPT-587195");
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_forUgyldigBehandling() {
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ugyldig_referanse.xml"));

        expectedException.expectMessage("Mottok et tilbakekrevingsgrunnlag fra Økonomi med henvisning som ikke er i støttet format. henvisning=ABC. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!");
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
    }

    @Test
    public void skal_utføre_les_kravgrunnlag_task_for_ugyldig_kravgrunnlag(){
        Behandling behandling = lagBehandling();
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_ugyldig_ENDR_negativ_beløp.xml"));
        lesKravgrunnlagTask.doTask(lagProsessTaskData());
        assertTilkobling();
        assertThat(behandling.isBehandlingPåVent()).isTrue();
    }

    private Behandling lagBehandling() {
        NavBruker navBruker = NavBruker.opprettNy(TestFagsakUtil.genererBruker().getAktørId(), Språkkode.nb);
        Fagsak fagsak = Fagsak.opprettNy(new Saksnummer("139015144"), navBruker);
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }

    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(LesKravgrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, String.valueOf(kravgrunnlagId));
        return prosessTaskData;
    }


    private void assertTilkobling() {
        Optional<ØkonomiXmlMottatt> økonomiXmlMottatt = mottattXmlRepository.finnForHenvisning(HENVISNING);
        assertThat(økonomiXmlMottatt).isPresent();
        assertTrue(økonomiXmlMottatt.get().isTilkoblet());
    }
}
