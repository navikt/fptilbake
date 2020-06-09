package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InnhentDokumentasjonbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String FLERE_OPPLYSNINGER = "Vi trenger flere opplysninger";

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FritekstbrevTjeneste mockFritekstbrevTjeneste = mock(FritekstbrevTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);

    private InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste;
    private Long behandlingId;

    @Before
    public void setup() {
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository,
                mockPersoninfoAdapter);

        innhentDokumentasjonBrevTjeneste = new InnhentDokumentasjonbrevTjeneste(repositoryProvider, mockFritekstbrevTjeneste, mockEksternDataForBrevTjeneste,
            historikkinnslagTjeneste);

        behandlingId = behandling.getId();
        when(mockFritekstbrevTjeneste.sendFritekstbrev(any(FritekstbrevData.class))).thenReturn(lagJournalOgDokument());
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(FLERE_OPPLYSNINGER.getBytes());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(any(Personinfo.class),any(BrevMottaker.class), any(Optional.class)))
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

        List<BrevSporing> brevSporingsData = brevSporingRepository.hentBrevData(behandlingId, BrevType.INNHENT_DOKUMENTASJONBREV);
        assertThat(brevSporingsData.size()).isEqualTo(1);

        List<Historikkinnslag> historikkInnslager = historikkRepository.hentHistorikk(behandlingId);
        assertThat(historikkInnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkInnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker().get(0).getLinkTekst()).isEqualTo(InnhentDokumentasjonbrevTjeneste.TITTEL_INNHENTDOKUMENTASJONBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_forhåndsvise_innhent_dokumentasjonbrev() {
        byte[] data = innhentDokumentasjonBrevTjeneste.hentForhåndsvisningInnhentDokumentasjonBrev(behandlingId, FLERE_OPPLYSNINGER);
        assertThat(data).isNotEmpty();
    }
}
