package no.nav.foreldrepenger.tilbakekreving.domene.person;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.pdl.AktørTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

@ApplicationScoped
public class PersoninfoAdapter {

    private TpsAdapter tpsAdapter;
    private AktørTjeneste aktørTjeneste;

    public PersoninfoAdapter() {
        // for CDI proxy
    }

    @Inject
    public PersoninfoAdapter(TpsAdapter tpsAdapter, AktørTjeneste aktørTjeneste) {
        this.tpsAdapter = tpsAdapter;
        this.aktørTjeneste = aktørTjeneste;
    }

    public Personinfo innhentSaksopplysningerForSøker(AktørId aktørId) {
        return hentPerson(aktørId).orElse(null);
    }

    public Optional<Personinfo> hentBrukerForAktør(AktørId aktørId) {
        return hentPerson(aktørId);
    }

    private Optional<Personinfo> hentPerson(AktørId aktørId) {
        var pi = tpsAdapter.hentIdentForAktørId(aktørId);
        aktørTjeneste.hentPersonIdentForAktørId(aktørId, pi);
        if (pi.isEmpty())
            return Optional.empty();
        var info = tpsAdapter.hentKjerneinformasjon(pi.get(), aktørId);
        aktørTjeneste.hentPersoninfo(aktørId, pi.get(), info);
        return Optional.ofNullable(info);
    }

    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        var fraTps = tpsAdapter.hentAktørIdForPersonIdent(fnr);
        aktørTjeneste.hentAktørIdForPersonIdent(fnr, fraTps);
        return fraTps;
    }

    public Adresseinfo hentAdresseinformasjon(PersonIdent personIdent) {
        return tpsAdapter.hentAdresseinformasjon(personIdent);
    }

}
