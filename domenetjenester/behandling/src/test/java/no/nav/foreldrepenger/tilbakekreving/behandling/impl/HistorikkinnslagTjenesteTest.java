package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.PersoninfoAdapterImpl;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.TpsAdapterImpl;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.TpsOversetter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class HistorikkinnslagTjenesteTest extends FellesTestOppsett {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("389426448");
    private static final String DOKUMENT_ID = "417743491";
    private AktørConsumerMedCache mockAktørConsumer = mock(AktørConsumerMedCache.class);
    private PersonConsumer mockPersonConsumer = mock(PersonConsumer.class);
    private TpsOversetter mockTpsOversetter = mock(TpsOversetter.class);
    private JournalTjeneste mockJournalTjeneste = mock(JournalTjeneste.class);

    private TpsAdapter tpsAdapter = new TpsAdapterImpl(mockAktørConsumer, mockPersonConsumer, mockTpsOversetter);
    private PersoninfoAdapter personinfoAdapter = new PersoninfoAdapterImpl(tpsAdapter);
    private HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, mockJournalTjeneste, personinfoAdapter);

    @Test
    public void skal_opprette_historikkinnslag_for_utsendt_brev() {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(
                JOURNALPOST_ID, DOKUMENT_ID, BEHANDLING.getFagsakId(), BEHANDLING.getId(), BEHANDLING.getAktørId(), "Vedtaksbrev");

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(BEHANDLING.getId());
        assertThat(historikkinnslager).isNotEmpty();

        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker()).isNotEmpty();
        HistorikkinnslagDokumentLink historikkinnslagDokumentLink = historikkinnslag.getDokumentLinker().get(0);
        assertThat(historikkinnslagDokumentLink.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    public void opprettHistorikkinnslagForOpprettetTilbakekreving() {
        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(BEHANDLING);

        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(BEHANDLING.getId());
        assertThat(historikkinnslager).isNotEmpty();

        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.TBK_OPPR);
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
        verify(mockJournalTjeneste, never()).hentMetadata(null);
    }

    private JournalMetadata<DokumentTypeId> byggJournalMetadata(JournalpostId journalpostId, String dokumentId, ArkivFilType arkivFiltype, boolean hoveddokument,
                                                                VariantFormat variantFormat) {
        JournalMetadata.Builder<DokumentTypeId> builderHoveddok = JournalMetadata.builder();
        builderHoveddok.medJournalpostId(journalpostId);
        builderHoveddok.medDokumentId(dokumentId);
        builderHoveddok.medVariantFormat(variantFormat);
        builderHoveddok.medMottakKanal(MottakKanal.EIA);
        builderHoveddok.medDokumentType(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        builderHoveddok.medDokumentKategori(DokumentKategori.SØKNAD);
        builderHoveddok.medArkivFilType(arkivFiltype);
        builderHoveddok.medErHoveddokument(hoveddokument);
        builderHoveddok.medForsendelseMottatt(LocalDate.now());
        builderHoveddok.medBrukerIdentListe(Collections.singletonList("01234567890"));
        JournalMetadata<DokumentTypeId> journalMetadataHoveddokument = builderHoveddok.build();
        return journalMetadataHoveddokument;
    }
}
