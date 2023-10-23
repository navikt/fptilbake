package no.nav.foreldrepenger.tilbakekreving.domene.person;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.pdl.AktørTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

@ApplicationScoped
public class PersoninfoAdapter {

    private AktørTjeneste aktørTjeneste;

    public PersoninfoAdapter() {
        // for CDI proxy
    }

    @Inject
    public PersoninfoAdapter(AktørTjeneste aktørTjeneste) {
        this.aktørTjeneste = aktørTjeneste;
    }

    public Optional<Personinfo> hentBrukerForAktør(FagsakYtelseType ytelseType, AktørId aktørId) {
        return aktørTjeneste.hentPersonIdentForAktørId(aktørId).map(pi -> aktørTjeneste.hentPersoninfo(ytelseType, aktørId, pi));
    }

    public Optional<PersonIdent> hentFnrForAktør(AktørId aktørId) {
        return aktørTjeneste.hentPersonIdentForAktørId(aktørId);
    }

    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        return aktørTjeneste.hentAktørIdForPersonIdent(fnr);
    }


}
