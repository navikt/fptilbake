package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import javax.persistence.MappedSuperclass;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;


@MappedSuperclass
public abstract class Person extends Aktør {

    public Person(AktørId aktørId) {
        super(aktørId);
    }

}
