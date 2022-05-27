package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonopplysningDto {

    private String aktoerId;
    private List<PersonopplysningDto> barnSoktFor = new ArrayList<>();
    private Integer antallBarn;

    public String getAktoerId() {
        return aktoerId;
    }

    public void setAktoerId(String aktoerId) {
        this.aktoerId = aktoerId;
    }

    public List<PersonopplysningDto> getBarnSoktFor() {
        return barnSoktFor;
    }

    public void setBarnSoktFor(List<PersonopplysningDto> barnSoktFor) {
        this.barnSoktFor = barnSoktFor;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

}
