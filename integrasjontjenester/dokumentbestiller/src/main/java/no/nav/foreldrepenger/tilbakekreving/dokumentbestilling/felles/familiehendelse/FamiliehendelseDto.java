package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.familiehendelse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FamiliehendelseDto {
    private String søknadType;

    public SøknadType getSøknadType() {
        return SøknadType.fra(søknadType);
    }
}
