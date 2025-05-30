package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.LogiskPeriodeMedFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;

class ManueltVarselBrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String VARSEL_TEKST = "Sender manuelt varselbrev";
    private final String KORRIGERT_VARSEL_TEKST = "Sender korrigert varselbrev";

    private final UUID BESTILLING_UUID = UUID.randomUUID();

    @Inject
    private VarselRepository varselRepository;

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FaktaFeilutbetalingTjeneste mockFeilutbetalingTjeneste = mock(FaktaFeilutbetalingTjeneste.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;

    private Long behandlingId;

    @BeforeEach
    void setup() {
        manueltVarselBrevTjeneste = new ManueltVarselBrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, mockFeilutbetalingTjeneste, mockPdfBrevTjeneste);

        behandlingId = behandling.getId();
        when(mockFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId)).thenReturn(lagFeilutbetalingFakta());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.NB))
                .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.NN);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(any(), eq(aktørId))).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(any(), any(Personinfo.class), any(BrevMottaker.class), any(Optional.class))).thenReturn(lagStandardNorskAdresse());

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.NB);
        when(mockEksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(FPSAK_BEHANDLING_UUID))
                .thenReturn(SamletEksternBehandlingInfo.builder()
                        .setGrunninformasjon(eksternBehandlingsinfoDto)
                        .build());
    }

    @Test
    void skal_sende_manuelt_varselbrev() {
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.BRUKER, BESTILLING_UUID);

        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.VARSEL), eq(Long.valueOf(9000L)), anyString(), any(BrevData.class), eq(BESTILLING_UUID));
    }

    @Test
    void skal_sende_korrigert_varselbrev() {
        //arrange
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.BRUKER, BESTILLING_UUID);
        varselRepository.lagre(behandlingId, VARSEL_TEKST, 100L);
        Mockito.clearInvocations(mockPdfBrevTjeneste);

        //act
        manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, KORRIGERT_VARSEL_TEKST, BrevMottaker.BRUKER, BESTILLING_UUID);

        //assert
        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.KORRIGERT_VARSEL), eq(Long.valueOf(9000L)), anyString(), any(BrevData.class), eq(BESTILLING_UUID));
    }

    @Test
    void skal_sende_korrigert_varselbrev_med_verge() {
        //arrange
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.BRUKER, BESTILLING_UUID);
        varselRepository.lagre(behandlingId, VARSEL_TEKST, 100L);
        Mockito.clearInvocations(mockPdfBrevTjeneste);
        vergeRepository.lagreVergeInformasjon(behandlingId, lagVerge());

        //act
        manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.VERGE, BESTILLING_UUID);

        //assert
        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.KORRIGERT_VARSEL), eq(Long.valueOf(9000L)), anyString(), any(BrevData.class), eq(BESTILLING_UUID));
    }

    @Test
    void skal_forhåndsvise_manuelt_varselbrev() {
        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    @Test
    void skal_forhåndsvise_korrigert_varselbrev() {
        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        varselRepository.lagre(behandlingId, KORRIGERT_VARSEL_TEKST, 32000l);
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.KORRIGERT_VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    private BehandlingFeilutbetalingFakta lagFeilutbetalingFakta() {
        LogiskPeriodeMedFaktaDto logiskPeriodeMedFaktaDto = LogiskPeriodeMedFaktaDto.lagPeriode(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                BigDecimal.valueOf(9000));
        return BehandlingFeilutbetalingFakta.builder().medAktuellFeilUtbetaltBeløp(BigDecimal.valueOf(9000))
                .medPerioder(List.of(logiskPeriodeMedFaktaDto))
                .medDatoForRevurderingsvedtak(LocalDate.now()).build();
    }
}
