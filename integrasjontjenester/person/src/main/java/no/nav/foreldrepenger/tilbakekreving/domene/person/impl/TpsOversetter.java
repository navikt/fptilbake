package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoenn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@ApplicationScoped
public class TpsOversetter {

    public static final Logger logger = LoggerFactory.getLogger(TpsOversetter.class);

    private TpsAdresseOversetter tpsAdresseOversetter;

    TpsOversetter() {
        // CDI Proxy
    }

    @Inject
    public TpsOversetter(TpsAdresseOversetter tpsAdresseOversetter) {
        this.tpsAdresseOversetter = tpsAdresseOversetter;
    }

    public Personinfo tilBrukerInfo(AktørId aktørId, Bruker bruker) {

        String navn = bruker.getPersonnavn().getSammensattNavn();

        LocalDate fødselsdato = finnFødselsdato(bruker);
        LocalDate dødsdato = finnDødsdato(bruker);

        Aktoer aktoer = bruker.getAktoer();
        PersonIdent pi = (PersonIdent) aktoer;
        String ident = pi.getIdent().getIdent();
        NavBrukerKjønn kjønn = tilBrukerKjønn(bruker.getKjoenn());

        SivilstandType sivilstandType = bruker.getSivilstand() == null ? null : SivilstandType.fraKode(bruker.getSivilstand().getSivilstand().getValue());

        return Personinfo.builder()
            .medAktørId(aktørId)
            .medPersonIdent(no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent.fra(ident))
            .medNavn(navn)
            .medFødselsdato(fødselsdato)
            .medDødsdato(dødsdato)
            .medNavBrukerKjønn(kjønn)
            .medSivilstandType(sivilstandType)
            .build();
    }

    public Adresseinfo tilAdresseinfo(Person person) {
        return tpsAdresseOversetter.tilAdresseInfoFor(person);
    }

    private LocalDate finnDødsdato(Bruker person) {
        LocalDate dødsdato = null;
        Doedsdato dødsdatoJaxb = person.getDoedsdato();
        if (dødsdatoJaxb != null) {
            dødsdato = DateUtil.convertToLocalDate(dødsdatoJaxb.getDoedsdato());
        }
        return dødsdato;
    }

    private LocalDate finnFødselsdato(Bruker person) {
        LocalDate fødselsdato = null;
        Foedselsdato fødselsdatoJaxb = person.getFoedselsdato();
        if (fødselsdatoJaxb != null) {
            fødselsdato = DateUtil.convertToLocalDate(fødselsdatoJaxb.getFoedselsdato());
        }
        return fødselsdato;
    }

    private NavBrukerKjønn tilBrukerKjønn(Kjoenn kjoenn) {
        return Optional.ofNullable(kjoenn)
            .map(Kjoenn::getKjoenn)
            .map(kj -> NavBrukerKjønn.fraKode(kj.getValue()))
            .orElse(NavBrukerKjønn.UDEFINERT);
    }

}
