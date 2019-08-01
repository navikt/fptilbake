package no.nav.foreldrepenger.tilbakekreving.domene.person;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public interface PersoninfoAdapter {

    Personinfo innhentSaksopplysningerForSøker(AktørId aktørId);

}
