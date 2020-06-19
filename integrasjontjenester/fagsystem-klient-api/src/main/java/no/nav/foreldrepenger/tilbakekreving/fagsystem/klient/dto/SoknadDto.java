package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SoknadDto {

    private SøknadType soknadType;

    public SøknadType getSøknadType() {
        return soknadType;
    }

    public void setSoknadType(SøknadType soknadType) {
        this.soknadType = soknadType;
    }
}
