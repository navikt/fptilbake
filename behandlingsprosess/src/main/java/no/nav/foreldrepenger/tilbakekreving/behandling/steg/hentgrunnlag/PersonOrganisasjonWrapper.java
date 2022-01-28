package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import static no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.ORGANISASJON;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class PersonOrganisasjonWrapper {

    private PersoninfoAdapter pdl;

    PersonOrganisasjonWrapper() {
        // for CDI proxy
    }

    @Inject
    public PersonOrganisasjonWrapper(PersoninfoAdapter pdl) {
        this.pdl = pdl;
    }

    public String hentAktørIdEllerOrganisajonNummer(String fnrEllerOrgNo, GjelderType gjelderType) {
        if (gjelderType.equals(ORGANISASJON)) {
            return fnrEllerOrgNo;
        } else {
            Optional<AktørId> aktørId = pdl.hentAktørForFnr(PersonIdent.fra(fnrEllerOrgNo));
            return aktørId.map(AktørId::getId)
                    .orElseThrow(() -> new TekniskException("FPT-107926", "Klarte ikke mappe - Fant ikke person med fnr"));
        }
    }

}
