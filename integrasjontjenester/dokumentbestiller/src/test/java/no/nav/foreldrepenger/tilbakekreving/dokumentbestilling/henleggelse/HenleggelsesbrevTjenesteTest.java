package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class HenleggelsesbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private FritekstbrevTjeneste mockFritekstbrevTjeneste = mock(FritekstbrevTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingRevurderingTjeneste behandlingRevurderingTjeneste;

    private Long behandlingId;
    private static final String REVURDERING_HENLEGGELSESBREV_FRITEKST = "Revurderingen ble henlagt";

    @Before
    public void setup() {
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository,
                mockPersoninfoAdapter);

        henleggelsesbrevTjeneste = new HenleggelsesbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste,
            mockFritekstbrevTjeneste, historikkinnslagTjeneste, mockPdfBrevTjeneste);
        ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(),null,null);
        henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider,prosessTaskRepository,mock(BehandlingskontrollTjeneste.class),historikkinnslagTjeneste);
        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT,
            "manuell henlagt",null);
        behandlingRevurderingTjeneste = new BehandlingRevurderingTjeneste(repositoryProvider);

        behandlingId = behandling.getId();
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID),FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        String varselTekst = "hello";
        when(mockFritekstbrevTjeneste.sendFritekstbrev(any(FritekstbrevData.class))).thenReturn(lagJournalOgDokument());
        when(mockFritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(any(FritekstbrevData.class))).thenReturn(varselTekst.getBytes());

        when(mockPdfBrevTjeneste.sendBrevSomIkkeErVedtaksbrev(anyLong(), any(BrevData.class))).thenReturn(lagJournalOgDokument());
        when(mockPdfBrevTjeneste.genererForhåndsvisning( any(BrevData.class))).thenReturn(varselTekst.getBytes());

        when(mockEksternDataForBrevTjeneste.hentYtelsenavn(FagsakYtelseType.FORELDREPENGER, Språkkode.nb))
            .thenReturn(lagYtelseNavn("foreldrepenger", "foreldrepenger"));
        Personinfo personinfo = byggStandardPerson("Fiona", DUMMY_FØDSELSNUMMER, Språkkode.nn);
        String aktørId = behandling.getAktørId().getId();
        when(mockEksternDataForBrevTjeneste.hentPerson(aktørId)).thenReturn(personinfo);
        when(mockEksternDataForBrevTjeneste.hentAdresse(any(Personinfo.class), any(BrevMottaker.class), any(Optional.class))).thenReturn(lagStandardNorskAdresse());

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfoDto.setSprakkode(Språkkode.nb);
        when(mockEksternDataForBrevTjeneste.hentYtelsesbehandlingFraFagsystemet(FPSAK_BEHANDLING_UUID))
            .thenReturn(SamletEksternBehandlingInfo.builder()
                .setGrunninformasjon(eksternBehandlingsinfoDto)
                .build());

        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet("1234", "NAV-TESTENHET");
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);
    }

    @Test
    public void skal_sende_henleggelsesbrev() {
        lagreVarselBrevSporing();
        Optional<JournalpostIdOgDokumentId> dokumentReferanse = henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null,BrevMottaker.BRUKER);
        assertThat(dokumentReferanse).isPresent();

        List<BrevSporing> brevSporing = brevSporingRepository.hentBrevData(behandlingId, BrevType.HENLEGGELSE_BREV);
        assertThat(brevSporing).isNotEmpty();
        assertThat(brevSporing.get(0).getDokumentId()).isEqualTo(dokumentReferanse.get().getDokumentId());

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(behandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(2);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
        assertThat(historikkinnslager.get(1).getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslager.get(1).getDokumentLinker().get(0).getLinkTekst())
            .isEqualTo(HenleggelsesbrevTjeneste.TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_sende_henleggelsesbrev_for_tilbakekreving_revurdering() {
        behandling.avsluttBehandling();
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();

        Optional<JournalpostIdOgDokumentId> dokumentReferanse = henleggelsesbrevTjeneste.sendHenleggelsebrev(revurderingBehandlingId,
            REVURDERING_HENLEGGELSESBREV_FRITEKST,BrevMottaker.BRUKER);
        assertThat(dokumentReferanse).isPresent();

        List<BrevSporing> brevSporing = brevSporingRepository.hentBrevData(revurderingBehandlingId, BrevType.HENLEGGELSE_BREV);
        assertThat(brevSporing).isNotEmpty();
        assertThat(brevSporing.get(0).getDokumentId()).isEqualTo(dokumentReferanse.get().getDokumentId());

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(revurderingBehandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(3);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.REVURD_OPPR);
        assertThat(historikkinnslager.get(1).getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
        assertThat(historikkinnslager.get(2).getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslager.get(2).getDokumentLinker().get(0).getLinkTekst())
            .isEqualTo(HenleggelsesbrevTjeneste.TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG);
    }

    @Test
    public void skal_sende_henleggelsesbrev_med_verge() {
        vergeRepository.lagreVergeInformasjon(behandlingId,lagVerge());
        lagreVarselBrevSporing();
        Optional<JournalpostIdOgDokumentId> dokumentReferanse = henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null, BrevMottaker.VERGE);
        assertThat(dokumentReferanse).isPresent();

        List<BrevSporing> brevSporing = brevSporingRepository.hentBrevData(behandlingId, BrevType.HENLEGGELSE_BREV);
        assertThat(brevSporing).isNotEmpty();
        assertThat(brevSporing.get(0).getDokumentId()).isEqualTo(dokumentReferanse.get().getDokumentId());

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(behandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(2);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
        assertThat(historikkinnslager.get(1).getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslager.get(1).getDokumentLinker().get(0).getLinkTekst())
            .isEqualTo(HenleggelsesbrevTjeneste.TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG_TIL_VERGE);
    }

    @Test
    public void skal_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_verge() {
        vergeRepository.lagreVergeInformasjon(behandlingId,lagVerge());
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();

        Optional<JournalpostIdOgDokumentId> dokumentReferanse = henleggelsesbrevTjeneste.sendHenleggelsebrev(revurderingBehandlingId,
            REVURDERING_HENLEGGELSESBREV_FRITEKST, BrevMottaker.VERGE);
        assertThat(dokumentReferanse).isPresent();

        List<BrevSporing> brevSporing = brevSporingRepository.hentBrevData(revurderingBehandlingId, BrevType.HENLEGGELSE_BREV);
        assertThat(brevSporing).isNotEmpty();
        assertThat(brevSporing.get(0).getDokumentId()).isEqualTo(dokumentReferanse.get().getDokumentId());

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(revurderingBehandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(3);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.REVURD_OPPR);
        assertThat(historikkinnslager.get(1).getType()).isEqualByComparingTo(HistorikkinnslagType.AVBRUTT_BEH);
        assertThat(historikkinnslager.get(2).getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslager.get(2).getDokumentLinker().get(0).getLinkTekst())
            .isEqualTo(HenleggelsesbrevTjeneste.TITTEL_HENLEGGELSESBREV_HISTORIKKINNSLAG_TIL_VERGE);
    }

    @Test
    public void skal_forhåndsvise_henleggelsebrev() {
        lagreVarselBrevSporing();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingId,null)).isNotEmpty();
    }

    @Test
    public void skal_forhåndsvise_henleggelsebrev_for_tilbakekreving_revurdering() {
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(revurderingBehandlingId,REVURDERING_HENLEGGELSESBREV_FRITEKST)).isNotEmpty();
    }

    @Test
    public void skal_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        assertThrows("FPT-110801",FunksjonellException.class, () ->
            henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null, BrevMottaker.BRUKER));
    }

    @Test
    public void skal_ikke_sende_henleggelsesbrev_for_tilbakekreving_revurdering_uten_fritekst() {
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();
        assertThrows("FPT-110802",FunksjonellException.class, () ->
            henleggelsesbrevTjeneste.sendHenleggelsebrev(revurderingBehandlingId, null, BrevMottaker.BRUKER));
    }

    private void lagreVarselBrevSporing() {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medJournalpostId(new JournalpostId("1213214234"))
            .medBrevType(BrevType.VARSEL_BREV)
            .medDokumentId("12312423432423")
            .medBehandlingId(behandlingId).build();
        brevSporingRepository.lagre(brevSporing);
    }

    private Long opprettOgForberedTilbakekrevingRevurdering(){
        behandling.avsluttBehandling();
        Behandling revurdering = behandlingRevurderingTjeneste.opprettRevurdering(behandlingId, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        Long revurderingBehandlingId = revurdering.getId();
        henleggBehandlingTjeneste.henleggBehandlingManuelt(revurderingBehandlingId, BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT,
            "manuell henlagt",REVURDERING_HENLEGGELSESBREV_FRITEKST);
        return revurderingBehandlingId;
    }

}
