package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class InnhentDokumentasjonbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String FLERE_OPPLYSNINGER = "Vi trenger flere opplysninger";
    private final UUID BESTILLING_UUID = UUID.randomUUID();

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste;
    private Long behandlingId;

    @BeforeEach
    void setup() {
        innhentDokumentasjonBrevTjeneste = new InnhentDokumentasjonbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, mockPdfBrevTjeneste);

        behandlingId = behandling.getId();

        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(FLERE_OPPLYSNINGER.getBytes());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.NB))
                .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.NN);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(any(), eq(aktørId))).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(any(), any(Personinfo.class), any(BrevMottaker.class), any(Optional.class)))
                .thenReturn(lagStandardNorskAdresse());

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.NB);
        when(mockEksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(FPSAK_BEHANDLING_UUID))
                .thenReturn(SamletEksternBehandlingInfo.builder()
                        .setGrunninformasjon(eksternBehandlingsinfoDto)
                        .build());
    }

    @Test
    void skal_sende_innhent_dokumentasjonbrev() {
        innhentDokumentasjonBrevTjeneste.sendInnhentDokumentasjonBrev(behandlingId, FLERE_OPPLYSNINGER, BrevMottaker.BRUKER, BESTILLING_UUID);

        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.INNHENT_DOKUMETASJON), any(BrevData.class), eq(BESTILLING_UUID));
    }

    @Test
    void skal_forhåndsvise_innhent_dokumentasjonbrev() {
        byte[] data = innhentDokumentasjonBrevTjeneste.hentForhåndsvisningInnhentDokumentasjonBrev(behandlingId, FLERE_OPPLYSNINGER);
        assertThat(data).isNotEmpty();
    }
}
