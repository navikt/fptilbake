package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class HenleggelsesbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FritekstbrevTjeneste mockFritekstbrevTjeneste = mock(FritekstbrevTjeneste.class);
    private JournalTjeneste mockJournalTjeneste = mock(JournalTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;

    private Long behandlingId;

    @Before
    public void setup() {
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository,
            mockJournalTjeneste,
            mockPersoninfoAdapter);
        henleggelsesbrevTjeneste = new HenleggelsesbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste,
            mockFritekstbrevTjeneste, historikkinnslagTjeneste);

        behandlingId = behandling.getId();
        String varselTekst = "hello";
        when(mockFritekstbrevTjeneste.sendFritekstbrev(any(FritekstbrevData.class))).thenReturn(lagJournalOgDokument());
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(varselTekst.getBytes());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(personinfo, aktørId)).thenReturn(lagStandardNorskAdresse());

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nb);
        when(mockEksternDataForBrevTjeneste.hentBehandlingFpsak(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.PERSONOPPLYSNINGER, Tillegsinformasjon.SØKNAD))
            .thenReturn(SamletEksternBehandlingInfo.builder()
                .setGrunninformasjon(eksternBehandlingsinfoDto)
                .build());

        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet("1234", "NAV-TESTENHET");
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);
    }

    @Test
    public void skal_sende_henleggelsesbrev() {
        lagreVarselBrevSporing();
        Optional<JournalpostIdOgDokumentId> dokumentReferanse = henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId);
        assertThat(dokumentReferanse).isPresent();

        List<BrevSporing> brevSporing = brevSporingRepository.hentBrevData(behandlingId, BrevType.HENLEGGELSE_BREV);
        assertThat(brevSporing).isNotEmpty();
        assertThat(brevSporing.get(0).getDokumentId()).isEqualTo(dokumentReferanse.get().getDokumentId());

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(behandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
    }

    @Test
    public void skal_forhåndsvise_henleggelsebrev() {
        lagreVarselBrevSporing();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingId)).isNotEmpty();
    }

    @Test
    public void skal_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        expectedException.expectMessage("FPT-110801");
        expectedException.expect(FunksjonellException.class);

        henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId);
    }

    private void lagreVarselBrevSporing() {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medJournalpostId(new JournalpostId("1213214234"))
            .medBrevType(BrevType.VARSEL_BREV)
            .medDokumentId("12312423432423")
            .medBehandlingId(behandlingId).build();
        brevSporingRepository.lagre(brevSporing);
    }
}
