package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.UtbetaltPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
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
    private JournalTjeneste mockJournalTjeneste = mock(JournalTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);

    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;

    private Long behandlingId;

    @Before
    public void setup() {
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository,
            mockJournalTjeneste,
            mockPersoninfoAdapter);
        manueltVarselBrevTjeneste = new ManueltVarselBrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste,
            mockFeilutbetalingTjeneste, mockFritekstbrevTjeneste, historikkinnslagTjeneste);

        behandlingId = behandling.getId();
        when(mockFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId)).thenReturn(lagFeilutbetalingFakta());
        when(mockFritekstbrevTjeneste.sendFritekstbrev(any(FritekstbrevData.class))).thenReturn(lagJournalOgDokument());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId)).thenReturn(lagStandardNorskAdresse());
        when(mockEksternDataForBrevTjeneste.getBrukersSvarfrist()).thenReturn(Period.ofWeeks(3));

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nb);
        when(mockEksternDataForBrevTjeneste.hentBehandlingFpsak(FPSAK_BEHANDLING_UUID))
            .thenReturn(SamletEksternBehandlingInfo.builder()
                .setGrunninformasjon(eksternBehandlingsinfoDto)
                .build());
    }

    @Test
    public void skal_sende_manuelt_varselbrev() {
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST);

        Optional<VarselInfo> varselInfo = varselRepository.finnVarsel(behandlingId);
        assertThat(varselInfo).isPresent();
        VarselInfo varsel = varselInfo.get();
        assertThat(varsel.getVarselTekst()).isEqualTo(VARSEL_TEKST);
        assertThat(varsel.getVarselBeløp()).isEqualTo(9000l);
        assertThat(varsel.isAktiv()).isTrue();

        assertThat(brevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId)).isTrue();

        List<Historikkinnslag> historikkInnslager = historikkRepository.hentHistorikk(behandlingId);
        assertThat(historikkInnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkInnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker().get(0).getLinkTekst()).isEqualTo(ManueltVarselBrevTjeneste.TITTEL_VARSELBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_sende_korrigert_varselbrev() {
        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, VARSEL_TEKST);
        manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, KORRIGERT_VARSEL_TEKST);

        Optional<VarselInfo> varselInfo = varselRepository.finnVarsel(behandlingId);
        assertThat(varselInfo).isPresent();
        VarselInfo varsel = varselInfo.get();
        assertThat(varsel.getVarselTekst()).isEqualTo(KORRIGERT_VARSEL_TEKST);
        assertThat(varsel.getVarselBeløp()).isEqualTo(9000l);
        assertThat(varsel.isAktiv()).isTrue();

        assertThat(brevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId)).isTrue();

        List<Historikkinnslag> historikkInnslager = historikkRepository.hentHistorikk(behandlingId);
        assertThat(historikkInnslager.size()).isEqualTo(2);
        historikkInnslager.sort(Comparator.comparing(Historikkinnslag::getId));
        Historikkinnslag historikkinnslag = historikkInnslager.get(1);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker().get(0).getLinkTekst()).isEqualTo(ManueltVarselBrevTjeneste.TITTEL_KORRIGERT_VARSELBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_forhåndsvise_manuelt_varselbrev() {
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        byte[] data = manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, DokumentMalType.VARSEL_DOK, VARSEL_TEKST);

        assertThat(data).isNotEmpty();
    }

    @Test
    public void skal_forhåndsvise_korrigert_varselbrev() {
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(VARSEL_TEKST.getBytes());
        varselRepository.lagre(behandlingId, KORRIGERT_VARSEL_TEKST,32000l);
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
