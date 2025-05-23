package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
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
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.vedtak.exception.FunksjonellException;

class HenleggelsesbrevTjenesteTest extends DokumentBestillerTestOppsett {

    private static final String REVURDERING_HENLEGGELSESBREV_FRITEKST = "Revurderingen ble henlagt";
    private static final UUID BESTILLING_UUID = UUID.randomUUID();

    private EksternDataForBrevTjeneste mockEksternDataForBrevTjeneste = mock(EksternDataForBrevTjeneste.class);
    private PdfBrevTjeneste mockPdfBrevTjeneste = mock(PdfBrevTjeneste.class);

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;
    private BehandlingRevurderingTjeneste behandlingRevurderingTjeneste;

    private Long behandlingId;

    @BeforeEach
    void setup() {
        henleggelsesbrevTjeneste = new HenleggelsesbrevTjeneste(repositoryProvider, mockEksternDataForBrevTjeneste, mockPdfBrevTjeneste);
        var kontroll = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        behandlingRevurderingTjeneste = new BehandlingRevurderingTjeneste(repositoryProvider, kontroll);

        behandlingId = behandling.getId();
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID), FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        String varselTekst = "hello";

        when(mockPdfBrevTjeneste.genererForhåndsvisning(any(BrevData.class))).thenReturn(varselTekst.getBytes());

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

        OrganisasjonsEnhet organisasjonsEnhet = new OrganisasjonsEnhet("1234", "Nav Testenhet");
        behandling.setBehandlendeOrganisasjonsEnhet(organisasjonsEnhet);
    }

    @Test
    void skal_sende_henleggelsesbrev() {
        lagreVarselBrevSporing();
        henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null, BrevMottaker.BRUKER, BESTILLING_UUID);

        Mockito.verify(mockPdfBrevTjeneste).sendBrev(eq(behandlingId), eq(DetaljertBrevType.HENLEGGELSE), any(BrevData.class), eq(BESTILLING_UUID));
    }

    @Test
    void skal_forhåndsvise_henleggelsebrev() {
        lagreVarselBrevSporing();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingId, null)).isNotEmpty();
    }

    @Test
    void skal_forhåndsvise_henleggelsebrev_for_tilbakekreving_revurdering() {
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();
        assertThat(henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(revurderingBehandlingId, REVURDERING_HENLEGGELSESBREV_FRITEKST)).isNotEmpty();
    }

    @Test
    void skal_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        var e = assertThrows(FunksjonellException.class, () ->
                henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, null, BrevMottaker.BRUKER, BESTILLING_UUID));
        assertThat(e.getMessage()).contains("FPT-110801");
    }

    @Test
    void skal_ikke_sende_henleggelsesbrev_for_tilbakekreving_revurdering_uten_fritekst() {
        Long revurderingBehandlingId = opprettOgForberedTilbakekrevingRevurdering();
        var e = assertThrows(FunksjonellException.class, () ->
                henleggelsesbrevTjeneste.sendHenleggelsebrev(revurderingBehandlingId, null, BrevMottaker.BRUKER, BESTILLING_UUID));
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
