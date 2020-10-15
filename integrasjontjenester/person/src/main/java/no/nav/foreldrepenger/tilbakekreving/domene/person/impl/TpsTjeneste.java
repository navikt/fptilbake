package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

@ApplicationScoped
public class TpsTjeneste {

    private TpsAdapter tpsAdapter;

    public TpsTjeneste() {
        // CDI proxy
    }

    @Inject
    public TpsTjeneste(TpsAdapter tpsAdapter) {
        this.tpsAdapter = tpsAdapter;
    }

    public Optional<PersonIdent> hentFnr(AktørId aktørId) {
        return tpsAdapter.hentIdentForAktørId(aktørId);
    }

    public Optional<Personinfo> hentBrukerForAktør(AktørId aktørId) {
        return hentFnr(aktørId).map(fnr -> tpsAdapter.hentKjerneinformasjon(fnr, aktørId));
    }

    public Optional<AktørId> hentAktørForFnr(PersonIdent fnr) {
        return tpsAdapter.hentAktørIdForPersonIdent(fnr);
    }

    public Adresseinfo hentAdresseinformasjon(PersonIdent personIdent) {
        return tpsAdapter.hentAdresseinformasjon(personIdent);
    }
}
