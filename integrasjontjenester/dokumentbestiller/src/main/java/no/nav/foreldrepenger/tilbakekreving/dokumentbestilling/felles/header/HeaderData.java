package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;


public class HeaderData extends BaseDokument { //TODO Trenger ikke BaseDokument her, bør fjernes.. trengs nå pga signatur i FellesTekstformaterer

    @JsonIgnore
    private Språkkode språkkode;

    private Person person;
    private Brev brev;

    public HeaderData(BrevMetadata brevMetadata, String overskrift) {
        this(brevMetadata.getSpråkkode(), brevMetadata.getSakspartNavn(), new PersonIdent(brevMetadata.getSakspartId()), overskrift);
    }

    public HeaderData(Språkkode språkkode, String navn, PersonIdent fnr, String overskrift) {
        this.språkkode = språkkode;
        this.person = new Person(navn, fnr);
        this.brev = new Brev(overskrift);
    }

    public void setDato(LocalDate dato) {
        this.brev.setDato(dato);
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public Brev getBrev() {
        return brev;
    }

    public Person getPerson() {
        return person;
    }
}
