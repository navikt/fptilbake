package no.nav.foreldrepenger.tilbakekreving.domene.person;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
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

    public NavBrukerKjønn hentKjønnForAktør(AktørId aktørId) {
        try {
            return aktørTjeneste.hentKjønnForAktør(aktørId);
        } catch (Exception e) {
            return NavBrukerKjønn.UDEFINERT;
        }
    }

    public Optional<Personinfo> hentBrukerForAktør(AktørId aktørId) {
        return aktørTjeneste.hentPersonIdentForAktørId(aktørId).map(pi -> aktørTjeneste.hentPersoninfo(aktørId, pi));
    }

    public Optional<PersonIdent> hentFnrForAktør(AktørId aktørId) {
        return aktørTjeneste.hentPersonIdentForAktørId(aktørId);
    }

    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        return aktørTjeneste.hentAktørIdForPersonIdent(fnr);
    }


}
