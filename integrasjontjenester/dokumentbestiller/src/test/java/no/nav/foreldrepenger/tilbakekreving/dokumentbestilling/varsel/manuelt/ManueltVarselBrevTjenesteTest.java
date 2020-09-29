package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
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
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class ManueltVarselBrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String VARSEL_TEKST = "Sender manuelt varselbrev";
    private final String KORRIGERT_VARSEL_TEKST = "Sender korrigert varselbrev";

    @Inject
    private VarselRepository varselRepository;

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FaktaFeilutbetalingTjeneste mockFeilutbetalingTjeneste = mock(FaktaFeilutbetalingTjeneste.class);
    private FritekstbrevTjeneste mockFritekstbrevTjeneste = mock(FritekstbrevTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;

    private Long behandlingId;

    @Before
    public void setup() {
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, mockPersoninfoAdapter);
        manueltVarselBrevTjeneste = new ManueltVarselBrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, mockFeilutbetalingTjeneste, mockFritekstbrevTjeneste, historikkinnslagTjeneste, mockPdfBrevTjeneste);

        behandlingId = behandling.getId();
        when(mockFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId)).thenReturn(lagFeilutbetalingFakta());
        when(mockFritekstbrevTjeneste.sendFritekstbrev(any(FritekstbrevData.class))).thenReturn(lagJournalOgDokument());
        when(mockPdfBrevTjeneste.sendBrev(anyLong(), any(DetaljertBrevType.class), anyLong(), any(BrevData.class))).thenReturn(lagJournalOgDokument());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(any(Personinfo.class), any(BrevMottaker.class), any(Optional.class))).thenReturn(lagStandardNorskAdresse());
        when(mockEksternDataForBrevTjeneste.getBrukersSvarfrist()).thenReturn(Period.ofWeeks(3));

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nb);
        when(mockEksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(FPSAK_BEHANDLING_UUID))
            .thenReturn(SamletEksternBehandlingInfo.builder()
                .setGrunninformasjon(eksternBehandlingsinfoDto)
                .build());
    }

    @Test
    public void skal_sende_manuelt_varselbrev() {
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.BRUKER);

        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.VARSEL), eq(Long.valueOf(9000L)), any(BrevData.class));
    }

    @Test
    public void skal_sende_korrigert_varselbrev() {
        //arrange
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.BRUKER);
        varselRepository.lagre(behandlingId, VARSEL_TEKST, 100L);
        Mockito.clearInvocations(mockPdfBrevTjeneste);

        //act
        manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, KORRIGERT_VARSEL_TEKST, BrevMottaker.BRUKER);

        //assert
        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.KORRIGERT_VARSEL), eq(Long.valueOf(9000L)), any(BrevData.class));
    }

    @Test
    public void skal_sende_korrigert_varselbrev_med_verge() {
        //arrange
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.BRUKER);
        varselRepository.lagre(behandlingId, VARSEL_TEKST, 100L);
        Mockito.clearInvocations(mockPdfBrevTjeneste);
        vergeRepository.lagreVergeInformasjon(behandlingId, lagVerge());

        //act
        manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, VARSEL_TEKST, BrevMottaker.VERGE);

        //assert
        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.KORRIGERT_VARSEL), eq(Long.valueOf(9000L)), any(BrevData.class));
    }

    @Test
    public void skal_forhåndsvise_manuelt_varselbrev() {
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    @Test
    public void skal_forhåndsvise_korrigert_varselbrev() {
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        varselRepository.lagre(behandlingId, KORRIGERT_VARSEL_TEKST, 32000l);
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.KORRIGERT_VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    private BehandlingFeilutbetalingFakta lagFeilutbetalingFakta() {
        UtbetaltPeriode utbetaltPeriode = UtbetaltPeriode.lagPeriode(LocalDate.of(2019, 10, 1),
            LocalDate.of(2019, 10, 30),
            BigDecimal.valueOf(9000));
        return BehandlingFeilutbetalingFakta.builder().medAktuellFeilUtbetaltBeløp(BigDecimal.valueOf(9000))
            .medPerioder(Lists.newArrayList(utbetaltPeriode))
            .medDatoForRevurderingsvedtak(LocalDate.now()).build();
    }
}
