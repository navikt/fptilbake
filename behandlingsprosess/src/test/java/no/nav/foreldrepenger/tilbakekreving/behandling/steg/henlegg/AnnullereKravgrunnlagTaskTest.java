package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henlegg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlSendt;
import no.nav.tilbakekreving.kravgrunnlag.annuller.v1.AnnullerKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class AnnullereKravgrunnlagTaskTest extends FellesTestOppsett {

    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository = new ØkonomiSendtXmlRepository(repoRule.getEntityManager());
    private ØkonomiConsumer økonomiConsumerMock = mock(ØkonomiConsumer.class);
    private AnnullereKravgrunnlagTask annullereKravgrunnlagTask = new AnnullereKravgrunnlagTask(grunnlagRepository, prosessTaskRepository, økonomiSendtXmlRepository, økonomiConsumerMock);

    @Before
    public void setup() {
        String xml = getInputXML("xml/kravgrunnlag_periode_FEIL.xml");
        DetaljertKravgrunnlag input = KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);

        when(tpsAdapterMock.hentAktørIdForPersonIdent(PersonIdent.fra("12345678901"))).thenReturn(Optional.of(new AktørId(999999L)));
        Kravgrunnlag431 kravgrunnlag431 = mapper.mapTilDomene(input);
        grunnlagRepository.lagre(behandling.getId(), kravgrunnlag431);

        when(økonomiConsumerMock.anullereKravgrunnlag(anyLong(), any(AnnullerKravgrunnlagDto.class))).thenReturn(lagMockRespons());
    }


    @Test
    public void skal_annuleregrunnlag() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AnnullereKravgrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(fagsak.getId(), behandling.getId(), fagsak.getAktørId().getId());

        annullereKravgrunnlagTask.doTask(prosessTaskData);

        Optional<ØkonomiXmlSendt> økonomiXmlSendt = økonomiSendtXmlRepository.finn(behandling.getId(), MeldingType.ANNULERE_GRUNNLAG);
        assertThat(økonomiXmlSendt).isPresent();
        ØkonomiXmlSendt xmlSendt = økonomiXmlSendt.get();
        assertThat(xmlSendt.getMeldingType()).isEqualByComparingTo(MeldingType.ANNULERE_GRUNNLAG);
        assertThat(xmlSendt.getKvittering()).isNotEmpty();
        assertThat(xmlSendt.getMelding()).isNotEmpty();
    }

    private MmelDto lagMockRespons() {
        MmelDto mmelDto = new MmelDto();
        mmelDto.setSystemId("460-BIDR");
        mmelDto.setAlvorlighetsgrad("00");
        mmelDto.setBeskrMelding("OK");
        return mmelDto;
    }
}
