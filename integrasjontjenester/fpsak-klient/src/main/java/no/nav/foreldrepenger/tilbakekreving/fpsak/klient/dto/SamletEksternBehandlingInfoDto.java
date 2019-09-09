package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public class SamletEksternBehandlingInfoDto {

    private EksternBehandlingsinfoDto grunninformasjon;
    private PersonopplysningDto personopplysninger;
    private String varseltekst;
    private TilbakekrevingValgDto tilbakekrevingsvalg;

    public EksternBehandlingsinfoDto getGrunninformasjon() {
        return grunninformasjon;
    }

    public PersonopplysningDto getPersonopplysninger() {
        return personopplysninger;
    }

    public String getVarseltekst() {
        return varseltekst;
    }

    public TilbakekrevingValgDto getTilbakekrevingsvalg() {
        return tilbakekrevingsvalg;
    }

    public AktørId getAktørId() {
        return new AktørId(personopplysninger.getAktoerId());
    }

    SamletEksternBehandlingInfoDto() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        SamletEksternBehandlingInfoDto kladd = new SamletEksternBehandlingInfoDto();

        public Builder setGrunninformasjon(EksternBehandlingsinfoDto grunninformasjon) {
            kladd.grunninformasjon = grunninformasjon;
            return this;
        }

        public Builder setPersonopplysninger(PersonopplysningDto personopplysninger) {
            kladd.personopplysninger = personopplysninger;
            return this;
        }

        public Builder setVarseltekst(String varseltekst) {
            kladd.varseltekst = varseltekst;
            return this;
        }

        public Builder setVarseltekst(VarseltekstDto varseltekst) {
            kladd.varseltekst = varseltekst.getVarseltekst();
            return this;
        }

        public Builder setTilbakekrevingvalg(TilbakekrevingValgDto tilbakekrevingvalg) {
            kladd.tilbakekrevingsvalg = tilbakekrevingvalg;
            return this;
        }

        public SamletEksternBehandlingInfoDto build() {
            return kladd;
        }
    }

}
