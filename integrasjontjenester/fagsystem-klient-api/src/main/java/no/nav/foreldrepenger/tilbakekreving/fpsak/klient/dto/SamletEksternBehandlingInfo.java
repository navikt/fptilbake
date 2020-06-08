package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import java.util.Arrays;
import java.util.Collection;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.vedtak.util.Objects;

public class SamletEksternBehandlingInfo {

    private Collection<Tillegsinformasjon> tilleggsinformasjonHentet;
    private EksternBehandlingsinfoDto grunninformasjon;
    private PersonopplysningDto personopplysninger;
    private String varseltekst;
    private TilbakekrevingValgDto tilbakekrevingsvalg;
    private SoknadDto søknad;
    private FagsakDto fagsak;
    private VergeDto verge;

    public EksternBehandlingsinfoDto getGrunninformasjon() {
        return grunninformasjon;
    }

    public PersonopplysningDto getPersonopplysninger() {
        Objects.check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.PERSONOPPLYSNINGER), "Utvikler-feil: har ikke hentet personopplysninger");
        return personopplysninger;
    }

    public String getVarseltekst() {
        Objects.check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.VARSELTEKST), "Utvikler-feil: har ikke hentet varseltekst");
        return varseltekst;
    }

    public TilbakekrevingValgDto getTilbakekrevingsvalg() {
        Objects.check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.TILBAKEKREVINGSVALG), "Utvikler-feil: har ikke hentet tilbakekrevingsvalg");
        return tilbakekrevingsvalg;
    }

    public SoknadDto getSøknad() {
        Objects.check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.SØKNAD), "Utvikler-feil: har ikke hentet søknad");
        return søknad;
    }

    public FagsakDto getFagsak() {
        Objects.check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.FAGSAK), "Utvikler-feil: har ikke hentet fagsak");
        return fagsak;
    }

    public VergeDto getVerge() {
        return verge;
    }

    public AktørId getAktørId() {
        return new AktørId(getPersonopplysninger().getAktoerId());
    }

    public SøknadType getSøknadType() {
        return getSøknad().getSøknadType();
    }

    public Saksnummer getSaksnummer() {
        return getFagsak().getSaksnummer();
    }

    public int getAntallBarnSøktFor() {
        return getPersonopplysninger().getBarnSoktFor().size();
    }

    SamletEksternBehandlingInfo() {
    }


    public static Builder builder(Tillegsinformasjon... tilleggsinformasjonHentet) {
        return builder(Arrays.asList(tilleggsinformasjonHentet));
    }

    public static Builder builder(Collection<Tillegsinformasjon> tilleggsinformasjonHentet) {
        return new Builder(tilleggsinformasjonHentet);
    }

    public static class Builder {
        SamletEksternBehandlingInfo kladd = new SamletEksternBehandlingInfo();

        public Builder(Collection<Tillegsinformasjon> tilleggsinformasjonHentet) {
            kladd.tilleggsinformasjonHentet = tilleggsinformasjonHentet;
        }

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

        public Builder setFagsak(FagsakDto fagsak) {
            kladd.fagsak = fagsak;
            return this;
        }

        public Builder setFamiliehendelse(SoknadDto søknad) {
            kladd.søknad = søknad;
            return this;
        }

        public Builder setVerge(VergeDto verge) {
            kladd.verge = verge;
            return this;
        }

        public SamletEksternBehandlingInfo build() {
            return kladd;
        }
    }

}
