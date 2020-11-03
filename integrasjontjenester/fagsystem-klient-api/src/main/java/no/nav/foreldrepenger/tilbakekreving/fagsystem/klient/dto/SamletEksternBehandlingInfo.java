package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.vedtak.util.Objects;

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
        return getPersonopplysninger().getAntallBarn() != null ? personopplysninger.getAntallBarn() : getPersonopplysninger().getBarnSoktFor().size();
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
            //TODO når verifisert i prod, gjør om logging til å kaste exceptions
            if (kladd.tilleggsinformasjonHentet.contains(Tillegsinformasjon.PERSONOPPLYSNINGER) && kladd.personopplysninger == null) {
                logger.warn("Etterspurte PERSONOPPLYSNINGER, men fikk ikke dette fra fagsystemet");
            }
            if (kladd.tilleggsinformasjonHentet.contains(Tillegsinformasjon.TILBAKEKREVINGSVALG) && kladd.tilbakekrevingsvalg == null) {
                logger.warn("Etterspurte TILBAKEKREVINGSVALG, men fikk ikke dette fra fagsystemet");
            }
            if (kladd.tilleggsinformasjonHentet.contains(Tillegsinformasjon.FAGSAK) && kladd.fagsak == null) {
                logger.warn("Etterspurte FAGSAK, men fikk ikke dette fra fagsystemet");
            }
            if (kladd.tilleggsinformasjonHentet.contains(Tillegsinformasjon.SØKNAD) && kladd.søknad == null) {
                logger.warn("Etterspurte SØKNAD, men fikk ikke dette fra fagsystemet");
            }
            if (kladd.tilleggsinformasjonHentet.contains(Tillegsinformasjon.VERGE) && kladd.verge == null) {
                logger.warn("Etterspurte VERGE, men fikk ikke dette fra fagsystemet");
            }
            if (kladd.tilleggsinformasjonHentet.contains(Tillegsinformasjon.VARSELTEKST) && kladd.varseltekst == null) {
                logger.warn("Etterspurte VARSELTEKST, men fikk ikke dette fra fagsystemet");
            }
            return kladd;
        }
    }

}
