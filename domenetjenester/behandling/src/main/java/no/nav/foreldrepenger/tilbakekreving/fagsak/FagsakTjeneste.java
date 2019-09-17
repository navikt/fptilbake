package no.nav.foreldrepenger.tilbakekreving.fagsak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class FagsakTjeneste {

    private TpsTjeneste tpsTjeneste;
    private FagsakRepository fagsakRepository;
    private NavBrukerRepository navBrukerRepository;

    FagsakTjeneste() {
        // For CDI
    }

    @Inject
    public FagsakTjeneste(TpsTjeneste tpsTjeneste, FagsakRepository fagsakRepository, NavBrukerRepository navBrukerRepository) {
        this.tpsTjeneste = tpsTjeneste;
        this.fagsakRepository = fagsakRepository;
        this.navBrukerRepository = navBrukerRepository;
    }

    public Fagsak opprettFagsak(Saksnummer saksnummer, AktørId aktørId, FagsakYtelseType fagsakYtelseType) {
        NavBruker bruker = hentNavBruker(aktørId);
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

    private NavBruker hentNavBruker(AktørId aktørId) {
        NavBruker navBruker;
        Optional<NavBruker> navBrukerOptional = navBrukerRepository.hent(aktørId);
        if (!navBrukerOptional.isPresent()) {
            Personinfo personinfo = tpsTjeneste.hentBrukerForAktør(aktørId).orElseThrow(() -> BehandlingFeil.FACTORY.fantIkkePersonMedAktørId().toException());
            navBruker = NavBruker.opprettNy(personinfo.getAktørId(), personinfo.getForetrukketSpråk());
        } else {
            navBruker = navBrukerOptional.get();
        }
        return navBruker;
    }

    private Fagsak lagreFagsak(Fagsak fagsak, Saksnummer saksnummer) {
        try {
            fagsakRepository.lagre(fagsak);
        } catch (PersistenceException e) { // NOSONAR
            if (e.getCause() instanceof ConstraintViolationException) {
                throw BehandlingFeil.FACTORY.saksnummerKnyttetTilAnnenBruker(saksnummer).toException();
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
