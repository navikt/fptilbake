package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.personopplysning;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonopplysningDto {

    private List<PersonopplysningDto> barnSoktFor = new ArrayList<>();
    private boolean harVerge;

    public boolean harVerge() {
        return harVerge;
    }

    public int getAntallBarnSøktFor() {
        return barnSoktFor.size();
    }
}
