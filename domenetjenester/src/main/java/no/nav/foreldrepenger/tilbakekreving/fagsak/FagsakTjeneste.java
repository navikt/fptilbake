package no.nav.foreldrepenger.tilbakekreving.fagsak;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class FagsakTjeneste {

    private PersoninfoAdapter tpsTjeneste;
    private FagsakRepository fagsakRepository;
    private NavBrukerRepository navBrukerRepository;

    FagsakTjeneste() {
        // For CDI
    }

    @Inject
    public FagsakTjeneste(PersoninfoAdapter tpsTjeneste, FagsakRepository fagsakRepository, NavBrukerRepository navBrukerRepository) {
        this.tpsTjeneste = tpsTjeneste;
        this.fagsakRepository = fagsakRepository;
        this.navBrukerRepository = navBrukerRepository;
    }

    public Fagsak opprettFagsak(Saksnummer saksnummer, AktørId aktørId, FagsakYtelseType fagsakYtelseType, Språkkode språkkode) {
        NavBruker bruker = hentNavBruker(aktørId, språkkode);
        Fagsak fagsak = Fagsak.opprettNy(saksnummer, bruker);
        fagsak.setFagsakYtelseType(fagsakYtelseType);

        //Hent forrige fagsak hvis finnes
        Optional<Fagsak> forrigeFagsak = finnFagsak(saksnummer);
        if (forrigeFagsak.isPresent()) {
            fagsak = forrigeFagsak.get();
            fagsakRepository.oppdaterFagsakStatus(fagsak.getId(), FagsakStatus.OPPRETTET);
        }
        lagreFagsak(fagsak, saksnummer);
        return fagsak;
    }

    public AktørId hentAktørForFnr(String fnr) {
        PersonIdent personIdent = new PersonIdent(fnr);
        Optional<AktørId> aktørId = tpsTjeneste.hentAktørForFnr(personIdent);
        if (aktørId.isEmpty()) {
            throw BehandlingFeil.fantIkkePersonIdentMedFnr();
        }
        return aktørId.get();
    }

    public String hentNavnForAktør(AktørId aktørId) {
        return tpsTjeneste.hentBrukerForAktør(aktørId).map(Personinfo::getNavn).orElseThrow(() -> new TekniskException("FPT-7428492", "Fant ikke person med aktørId"));
    }

    private NavBruker hentNavBruker(AktørId aktørId, Språkkode språkkode) {
        return navBrukerRepository.hent(aktørId).orElseGet(() -> NavBruker.opprettNy(aktørId, språkkode));
    }

    private Fagsak lagreFagsak(Fagsak fagsak, Saksnummer saksnummer) {
        try {
            fagsakRepository.lagre(fagsak);
        } catch (PersistenceException e) { // NOSONAR
            if (e.getCause() instanceof ConstraintViolationException) {
                throw BehandlingFeil.saksnummerKnyttetTilAnnenBruker(saksnummer);
            } else {
                throw e;
            }
        }
        return fagsak;
    }

    private Optional<Fagsak> finnFagsak(Saksnummer saksnummer) {
        return fagsakRepository.hentSakGittSaksnummer(saksnummer);
    }
}
