package no.nav.foreldrepenger.tilbakekreving.domene.person;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public interface TpsAdapter {

    Optional<PersonIdent> hentIdentForAktørId(AktørId aktørId);

    Personinfo hentKjerneinformasjon(PersonIdent personIdent, AktørId aktørId);

    Adresseinfo hentAdresseinformasjon(PersonIdent personIdent);

    Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent);

}
