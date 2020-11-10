package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.DokumentBestillerTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.EksternDataForBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.BrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf.PdfBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class HenleggelsesbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String REVURDERING_HENLEGGELSESBREV_FRITEKST = "Revurderingen ble henlagt";

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private PersoninfoAdapter mockPersoninfoAdapter = mock(PersoninfoAdapter.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;
    private BehandlingRevurderingTjeneste behandlingRevurderingTjeneste;

    private Long behandlingId;

    @Before
    public void setup() {
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository,
            mockPersoninfoAdapter);

        henleggelsesbrevTjeneste = new HenleggelsesbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, historikkinnslagTjeneste, mockPdfBrevTjeneste);
        behandlingRevurderingTjeneste = new BehandlingRevurderingTjeneste(repositoryProvider);

        behandlingId = behandling.getId();
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID), FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        String varselTekst = "hello";

        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(varselTekst.getBytes());

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
        henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null, BrevMottaker.BRUKER);

        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.HENLEGGELSE), any(BrevData.class));
    }

    @Test
    public void skal_forhåndsvise_henleggelsebrev() {
        lagreVarselBrevSporing();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingId, null)).isNotEmpty();
    }

    @Test
    public void skal_forhåndsvise_henleggelsebrev_for_tilbakekreving_revurdering() {
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(revurderingBehandlingId, REVURDERING_HENLEGGELSESBREV_FRITEKST)).isNotEmpty();
    }

    @Test
    public void skal_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        var e = assertThrows(FunksjonellException.class, () ->
            henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null, BrevMottaker.BRUKER));
        assertThat(e.getMessage()).contains("FPT-110801");
    }

    @Test
    public void skal_ikke_sende_henleggelsesbrev_for_tilbakekreving_revurdering_uten_fritekst() {
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();
        var e = assertThrows(FunksjonellException.class, () ->
            henleggelsesbrevTjeneste.sendHenleggelsebrev(revurderingBehandlingId, null, BrevMottaker.BRUKER));
        assertThat(e.getMessage()).contains("FPT-110802");
    }

    private void lagreVarselBrevSporing() {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medJournalpostId(new JournalpostId("1213214234"))
            .medBrevType(BrevType.VARSEL_BREV)
            .medDokumentId("12312423432423")
            .medBehandlingId(behandlingId).build();
        brevSporingRepository.lagre(brevSporing);
    }

    private Long opprettOgForberedTilbakekrevingRevurdering() {
        behandling.avsluttBehandling();
        Behandling revurdering = behandlingRevurderingTjeneste.opprettRevurdering(behandlingId, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        Long revurderingBehandlingId = revurdering.getId();
        return revurderingBehandlingId;
    }

}
