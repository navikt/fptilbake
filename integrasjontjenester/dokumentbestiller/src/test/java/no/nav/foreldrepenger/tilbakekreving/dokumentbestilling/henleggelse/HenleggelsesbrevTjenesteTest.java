package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

public class HenleggelsesbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FritekstbrevTjeneste mockFritekstbrevTjeneste = mock(FritekstbrevTjeneste.class);
    private JournalTjeneste mockJournalTjeneste = mock(JournalTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);
    private HistorikkRepository historikkRepository = repositoryProvider.getHistorikkRepository();

    private HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(repositoryProvider.getHistorikkRepository(),
        mockJournalTjeneste,
        mockPersoninfoAdapter);
    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste = new HenleggelsesbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, mockFritekstbrevTjeneste, historikkinnslagTjeneste);

    private Long behandlingId;

    @Before
    public void setup() {
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

        repositoryProvider.getVarselRepository().lagre(behandlingId, varselTekst, 1000L);
        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet("1234", "NAV-TESTENHET");
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);
    }

    @Test
    public void skal_sende_henleggelsesbrev() {
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
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningVarselbrev(behandlingId)).isNotEmpty();
    }
}
