package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumer;

@ApplicationScoped
public class DokumentArkivTjeneste {
    private JournalConsumer journalConsumer;

    private FagsakRepository fagsakRepository;

    DokumentArkivTjeneste() {
        // for CDI proxy
    }

    @Inject
    public DokumentArkivTjeneste(JournalConsumer journalConsumer,
                                 FagsakRepository fagsakRepository) {
        this.journalConsumer = journalConsumer;
        this.fagsakRepository = fagsakRepository;
    }

    public List<ArkivJournalPost> hentAlleJournalposterForSak(Saksnummer saksnummer) {
        List<ArkivJournalPost> journalPosts = new ArrayList<>();
        doHentKjerneJournalpostListe(saksnummer)
            .map(HentKjerneJournalpostListeResponse::getJournalpostListe).orElse(new ArrayList<>())
            .stream()
            .filter(journalpost -> !Journaltilstand.UTGAAR.equals(journalpost.getJournaltilstand()))
            .forEach(journalpost -> journalPosts.add(opprettArkivJournalPost(saksnummer, journalpost)));

        return journalPosts;
    }

    private Optional<HentKjerneJournalpostListeResponse> doHentKjerneJournalpostListe(Saksnummer saksnummer) {
        final Optional<Fagsak> fagsak = fagsakRepository.hentSakGittSaksnummer(saksnummer);
        if (fagsak.isEmpty()) {
            return Optional.empty();
        }
        HentKjerneJournalpostListeRequest hentKjerneJournalpostListeRequest = new HentKjerneJournalpostListeRequest();

        hentKjerneJournalpostListeRequest.getArkivSakListe().add(lageJournalSak(saksnummer, Fagsystem.GOSYS.getOffisiellKode()));

        try {
            HentKjerneJournalpostListeResponse hentKjerneJournalpostListeResponse = journalConsumer.hentKjerneJournalpostListe(hentKjerneJournalpostListeRequest);
            return Optional.ofNullable(hentKjerneJournalpostListeResponse);
        } catch (HentKjerneJournalpostListeSikkerhetsbegrensning e) {
            throw DokumentArkivTjenesteFeil.FACTORY.journalUtilgjengeligSikkerhetsbegrensning("hent journalpostliste", e).toException();
        } catch (HentKjerneJournalpostListeUgyldigInput e) {
            throw DokumentArkivTjenesteFeil.FACTORY.journalpostUgyldigInput(e).toException();
        }
    }

    private ArkivSak lageJournalSak(Saksnummer saksnummer, String fagsystem) {
        ArkivSak journalSak = new ArkivSak();
        journalSak.setArkivSakSystem(fagsystem);
        journalSak.setArkivSakId(saksnummer.getVerdi());
        journalSak.setErFeilregistrert(false);
        return journalSak;
    }

    private ArkivJournalPost opprettArkivJournalPost(Saksnummer saksnummer, Journalpost journalpost) {
        return new ArkivJournalPost(saksnummer, new JournalpostId(journalpost.getJournalpostId()));
    }
}
