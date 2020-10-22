package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonopplysningDto {

    private String aktoerId;
    private boolean harVerge;
    private String navn;
    @JsonProperty("fnr")
    private String fødselsnummer;
    private List<PersonopplysningDto> barnSoktFor = new ArrayList<>();
    private List<PersonadresseDto> adresser = new ArrayList<>();
    private Integer antallBarn;

    public String getFødselsnummer() {
        return fødselsnummer;
    }

    public void setFødselsnummer(String fødselsnummer) {
        this.fødselsnummer = fødselsnummer;
    }

    public List<PersonadresseDto> getAdresser() {
        return adresser;
    }

    public void setAdresser(List<PersonadresseDto> adresser) {
        this.adresser = adresser;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public boolean isHarVerge() {
        return harVerge;
    }

    public void setHarVerge(boolean harVerge) {
        this.harVerge = harVerge;
    }

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
