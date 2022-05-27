package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;

public class SamletEksternBehandlingInfo {

    private static final Logger logger = LoggerFactory.getLogger(SamletEksternBehandlingInfo.class);

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
        check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.PERSONOPPLYSNINGER), "Utvikler-feil: har ikke hentet personopplysninger");
        return personopplysninger;
    }

    public String getVarseltekst() {
        check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.VARSELTEKST), "Utvikler-feil: har ikke hentet varseltekst");
        return varseltekst;
    }

    public TilbakekrevingValgDto getTilbakekrevingsvalg() {
        check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.TILBAKEKREVINGSVALG), "Utvikler-feil: har ikke hentet tilbakekrevingsvalg");
        return tilbakekrevingsvalg;
    }

    public SoknadDto getSøknad() {
        check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.SØKNAD), "Utvikler-feil: har ikke hentet søknad");
        return søknad;
    }

    public FagsakDto getFagsak() {
        check(tilleggsinformasjonHentet.contains(Tillegsinformasjon.FAGSAK), "Utvikler-feil: har ikke hentet fagsak");
        return fagsak;
    }

    public VergeDto getVerge() {
        return verge;
    }

    public AktørId getAktørId() {
        PersonopplysningDto po = getPersonopplysninger();
        String aktoerId = po.getAktoerId();
        return new AktørId(aktoerId);
    }

    public SøknadType getSøknadType() {
        return getSøknad().getSøknadType();
    }

    public Saksnummer getSaksnummer() {
        return getFagsak().getSaksnummer();
    }

    public int getAntallBarnSøktFor() {
        return getPersonopplysninger().getAntallBarn() != null ? personopplysninger.getAntallBarn() :
            Optional.ofNullable(getPersonopplysninger().getBarnSoktFor()).map(Collection::size).orElseThrow();
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
            valider(Tillegsinformasjon.PERSONOPPLYSNINGER, SamletEksternBehandlingInfo::getPersonopplysninger);
            valider(Tillegsinformasjon.TILBAKEKREVINGSVALG, SamletEksternBehandlingInfo::getTilbakekrevingsvalg);
            valider(Tillegsinformasjon.FAGSAK, SamletEksternBehandlingInfo::getFagsak);
            valider(Tillegsinformasjon.SØKNAD, SamletEksternBehandlingInfo::getSøknad);
            valider(Tillegsinformasjon.VERGE, SamletEksternBehandlingInfo::getVerge);
            valider(Tillegsinformasjon.VARSELTEKST, SamletEksternBehandlingInfo::getVarseltekst);
            return kladd;
        }

        private void valider(Tillegsinformasjon tillegsinformasjon, Function<SamletEksternBehandlingInfo, Object> opplysningsSupplier) {
            if (kladd.tilleggsinformasjonHentet.contains(tillegsinformasjon) && opplysningsSupplier.apply(kladd) == null) {
                //TODO når verifisert i prod, gjør om logging til å kaste exceptions
                logger.info("Etterspurte {}, men fikk ikke dette fra fagsystemet", tillegsinformasjon);
            }
        }
    }

    private static void check(boolean check, String message, Object... params) {
        if (!check) {
            throw new IllegalArgumentException(String.format(message, params));
        }
    }

}
