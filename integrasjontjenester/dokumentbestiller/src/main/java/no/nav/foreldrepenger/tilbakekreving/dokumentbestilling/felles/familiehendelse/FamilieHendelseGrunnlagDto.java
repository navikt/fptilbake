package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.familiehendelse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FamilieHendelseGrunnlagDto {

    private FamiliehendelseDto gjeldende;

    public FamiliehendelseDto getGjeldende() {
        return gjeldende;
    }

    public void setGjeldende(FamiliehendelseDto gjeldende) {
        this.gjeldende = gjeldende;
    }

}
