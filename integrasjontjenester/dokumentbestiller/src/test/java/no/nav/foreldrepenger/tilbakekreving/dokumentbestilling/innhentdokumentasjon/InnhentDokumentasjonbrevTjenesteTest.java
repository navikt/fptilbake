package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Period;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InnhentDokumentasjonbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String FLERE_OPPLYSNINGER = "Vi trenger flere opplysninger";

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste;
    private Long behandlingId;

    @Before
    public void setup() {
        innhentDokumentasjonBrevTjeneste = new InnhentDokumentasjonbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, mockPdfBrevTjeneste);

        behandlingId = behandling.getId();

        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(FLERE_OPPLYSNINGER.getBytes());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(any(Personinfo.class), any(BrevMottaker.class), any(Optional.class)))
            .thenReturn(lagStandardNorskAdresse());
        when(mockEksternDataForBrevTjeneste.getBrukersSvarfrist()).thenReturn(Period.ofWeeks(2));

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nb);
        when(mockEksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(FPSAK_BEHANDLING_UUID))
            .thenReturn(SamletEksternBehandlingInfo.builder()
                .setGrunninformasjon(eksternBehandlingsinfoDto)
                .build());
    }

    @Test
    public void skal_sende_innhent_dokumentasjonbrev() {
        innhentDokumentasjonBrevTjeneste.sendInnhentDokumentasjonBrev(behandlingId, FLERE_OPPLYSNINGER, BrevMottaker.BRUKER);

        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.INNHENT_DOKUMETASJON), any(BrevData.class));
    }

    @Test
    public void skal_forhåndsvise_innhent_dokumentasjonbrev() {
        byte[] data = innhentDokumentasjonBrevTjeneste.hentForhåndsvisningInnhentDokumentasjonBrev(behandlingId, FLERE_OPPLYSNINGER);
        assertThat(data).isNotEmpty();
    }
}
