package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

@ApplicationScoped
public class PersoninfoAdapterImpl implements PersoninfoAdapter {

    private TpsAdapter tpsAdapter;

    public PersoninfoAdapterImpl() {
        // for CDI proxy
    }

    @Inject
    public PersoninfoAdapterImpl(TpsAdapter tpsAdapter) {
        this.tpsAdapter = tpsAdapter;
    }

    @Override
    public Personinfo innhentSaksopplysningerForSøker(AktørId aktørId) {
        return hentKjerneinformasjon(aktørId);
    }

    private Personinfo hentKjerneinformasjon(AktørId aktørId) {
        Optional<PersonIdent> personIdent = tpsAdapter.hentIdentForAktørId(aktørId);
        if (personIdent.isPresent()) {
            return hentKjerneinformasjon(aktørId, personIdent.get());
        }
        return null;
    }

    private Personinfo hentKjerneinformasjon(AktørId aktørId, PersonIdent personIdent) {
        return tpsAdapter.hentKjerneinformasjon(personIdent, aktørId);
    }

}
