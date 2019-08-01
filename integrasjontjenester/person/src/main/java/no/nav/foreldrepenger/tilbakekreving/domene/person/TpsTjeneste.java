package no.nav.foreldrepenger.tilbakekreving.domene.person;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public interface TpsTjeneste {

    Optional<PersonIdent> hentFnr(AktørId aktørId);

    Optional<Personinfo> hentBrukerForAktør(AktørId aktørId);

    Adresseinfo hentAdresseinformasjon(PersonIdent personIdent);

    Optional<AktørId> hentAktørForFnr(PersonIdent fnr);

}
