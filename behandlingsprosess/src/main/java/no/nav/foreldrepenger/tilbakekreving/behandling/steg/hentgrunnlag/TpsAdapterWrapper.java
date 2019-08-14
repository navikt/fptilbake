package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import static no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType.ORGANISASJON;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.GjelderType;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class TpsAdapterWrapper {

    private TpsAdapter tps;

    TpsAdapterWrapper(){
        // for CDI proxy
    }

    @Inject
    public TpsAdapterWrapper(TpsAdapter tps){
        this.tps = tps;
    }

    public String hentAktørIdEllerOrganisajonNummer(String fnrEllerOrgNo, GjelderType gjelderType) {
        if (gjelderType.equals(ORGANISASJON)) {
            return fnrEllerOrgNo;
        } else {
            Optional<AktørId> aktørId = tps.hentAktørIdForPersonIdent(PersonIdent.fra(fnrEllerOrgNo));
            return aktørId
                .map(AktørId::getId)
                .orElseThrow(() -> TpsAdapterWrapper.TpsAdapterWrapperFeil.FACTORY.fantIkkePersonIdentMedFnr().toException());
        }
    }

    public interface TpsAdapterWrapperFeil extends DeklarerteFeil {

        TpsAdapterWrapper.TpsAdapterWrapperFeil FACTORY = FeilFactory.create(TpsAdapterWrapper.TpsAdapterWrapperFeil.class);

        @TekniskFeil(feilkode = "FPT-107926", feilmelding = "Klarte ikke mappe - Fant ikke person med fnr", logLevel = LogLevel.WARN)
        Feil fantIkkePersonIdentMedFnr();
    }
}
