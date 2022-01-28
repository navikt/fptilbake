package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class LesKravgrunnlagTaskTest extends FellesTestOppsett {

    private Long kravgrunnlagId;
    private static final long REFERANSE = 100000001l;
    private Behandling behandling;
    private String saksnummer;

    @BeforeEach
    public void setup() {
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        when(personinfoAdapterMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(fagsak.getAktørId()));
        behandling = lagBehandling();
        saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
    }

    @Test
    public void skal_utføre_leskravgrunnlag_task_forGyldigBehandling() {
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(saksnummer)).thenReturn(lagResponsFraFagsystemKlient());
        lesKravgrunnlagTask.doTask(lagProsessTaskData());

        assertTrue(grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId()));
        assertTilkobling();
        assertFalse(behandling.isBehandlingPåVent());
        assertTrue(eksternBehandlingRepository.finnesEksternBehandling(behandling.getId(), Henvisning.fraEksternBehandlingId(REFERANSE)));
    }

    @Test
    public void skal_ikke_utføre_leskravgrunnlag_task_når_grunnlag_referanse_ikke_finnes_i_fagsystem() {
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(saksnummer)).thenReturn(Collections.emptyList());

        var e = assertThrows(TekniskException.class, () -> lesKravgrunnlagTask.doTask(lagProsessTaskData()));
        assertThat(e.getMessage()).contains("FPT-587195");
    }

    @Test
    public void skal_ikke_utføre_leskravgrunnlag_task_nårBehandlingFinnesIkkeIFpsak() {
        when(fagsystemKlientMock.finnesBehandlingIFagsystem(fagsak.getSaksnummer().getVerdi(), HENVISNING)).thenReturn(false);

        assertThatThrownBy(() -> lesKravgrunnlagTask.doTask(lagProsessTaskData()))
                .hasMessageContaining("FPT-587195");
    }

    @Test
    public void skal_ikke_utføre_leskravgrunnlag_task_forUgyldigBehandling() {
        kravgrunnlagId = mottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL_ugyldig_referanse.xml"));

        assertThatThrownBy(() -> lesKravgrunnlagTask.doTask(lagProsessTaskData()))
                .hasMessageContaining("Mottok et tilbakekrevingsgrunnlag fra Økonomi med henvisning som ikke er i støttet format."
                        + " henvisning=ABC. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!");

    }

    @Test
    public void skal_utføre_les_kravgrunnlag_task_for_ugyldig_kravgrunnlag() {
        when(fagsystemKlientMock.hentBehandlingForSaksnummer(saksnummer)).thenReturn(lagResponsFraFagsystemKlient());
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

    private List<EksternBehandlingsinfoDto> lagResponsFraFagsystemKlient() {
        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setUuid(UUID.randomUUID());
        eksternBehandlingsinfoDto.setHenvisning(Henvisning.fraEksternBehandlingId(REFERANSE));
        return List.of(eksternBehandlingsinfoDto);
    }

    private ProsessTaskData lagProsessTaskData() {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(LesKravgrunnlagTask.class);
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
