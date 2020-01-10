package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import javax.xml.bind.JAXBException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ØkonomiResponsFeil extends DeklarerteFeil {

    ØkonomiResponsFeil FACTORY = FeilFactory.create(ØkonomiResponsFeil.class);

    @TekniskFeil(feilkode = "FPT-113618", feilmelding = "Kunne ikke marshalle respons fra økonomi for behandlingId=%s", logLevel = LogLevel.WARN)
    Feil kunneIkkeMarshalleØkonomiResponsXml(Long behandlingId, JAXBException e);

    @TekniskFeil(feilkode = "FPT-176103", feilmelding = "Kunne ikke unmarshalle respons fra økonomi for behandlingId=%s xmlId=%s", logLevel = LogLevel.WARN)
    Feil kunneIkkeUnmarshalleØkonomiResponsXml(Long behandlingId, Long xmlId, JAXBException e);

}
