package no.nav.foreldrepenger.tilbakekreving.domene.person.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.SpråkKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoenn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personstatus;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Spraak;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.log.util.LoggerUtils;

@ApplicationScoped
public class TpsOversetter {

    public static final Logger logger = LoggerFactory.getLogger(TpsOversetter.class);

    private NavBrukerKodeverkRepository navBrukerKodeverkRepository;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;
    private SpråkKodeverkRepository språkKodeverkRepository;
    private TpsAdresseOversetter tpsAdresseOversetter;

    public TpsOversetter() {
        // CDI Proxy
    }

    @Inject
    public TpsOversetter(NavBrukerKodeverkRepository navBrukerKodeverkRepository,
                         BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository,
                         SpråkKodeverkRepository språkKodeverkRepository,
                         TpsAdresseOversetter tpsAdresseOversetter) {
        this.navBrukerKodeverkRepository = navBrukerKodeverkRepository;
        this.behandlingsgrunnlagKodeverkRepository = behandlingsgrunnlagKodeverkRepository;
        this.språkKodeverkRepository = språkKodeverkRepository;
        this.tpsAdresseOversetter = tpsAdresseOversetter;
    }

    public Personinfo tilBrukerInfo(AktørId aktørId, Bruker bruker) {

        String navn = bruker.getPersonnavn().getSammensattNavn();
        String adresse = tpsAdresseOversetter.finnAdresseFor(bruker);
        String adresseLandkode = tpsAdresseOversetter.finnAdresseLandkodeFor(bruker);
        String utlandsadresse = tpsAdresseOversetter.finnUtlandsadresseFor(bruker);

        LocalDate fødselsdato = finnFødselsdato(bruker);
        LocalDate dødsdato = finnDødsdato(bruker);

        Aktoer aktoer = bruker.getAktoer();
        PersonIdent pi = (PersonIdent) aktoer;
        String ident = pi.getIdent().getIdent();
        NavBrukerKjønn kjønn = tilBrukerKjønn(bruker.getKjoenn());
        PersonstatusType personstatus = tilPersonstatusType(bruker.getPersonstatus());

        Landkoder landkoder = utledLandkode(bruker.getStatsborgerskap());
        Region region = behandlingsgrunnlagKodeverkRepository.finnHøyestRangertRegion(Collections.singletonList(landkoder.getKode()));

        String diskresjonskode = bruker.getDiskresjonskode() == null ? null : bruker.getDiskresjonskode().getValue();
        String geografiskTilknytning = bruker.getGeografiskTilknytning() != null ? bruker.getGeografiskTilknytning().getGeografiskTilknytning() : null;

        List<Adresseinfo> adresseinfoList = tpsAdresseOversetter.lagListeMedAdresseInfo(bruker);
        SivilstandType sivilstandType = bruker.getSivilstand() == null ? null : behandlingsgrunnlagKodeverkRepository.finnSivilstandType(bruker.getSivilstand().getSivilstand().getValue());

        return Personinfo.builder()
                .medAktørId(aktørId)
                .medPersonIdent(no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent.fra(ident))
                .medNavn(navn)
                .medAdresse(adresse)
                .medAdresseLandkode(adresseLandkode)
                .medFødselsdato(fødselsdato)
                .medDødsdato(dødsdato)
                .medNavBrukerKjønn(kjønn)
                .medPersonstatusType(personstatus)
                .medStatsborgerskap(new no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Statsborgerskap(landkoder.getKode()))
                .medRegion(region)
                .medUtlandsadresse(utlandsadresse)
                .medForetrukketSpråk(bestemForetrukketSpråk(bruker))
                .medGegrafiskTilknytning(geografiskTilknytning)
                .medDiskresjonsKode(diskresjonskode)
                .medAdresseInfoList(adresseinfoList)
                .medSivilstandType(sivilstandType)
                .medLandkode(landkoder)
                .build();
    }

    public Adresseinfo tilAdresseinfo(Person person) {
        return tpsAdresseOversetter.tilAdresseInfoFor(person);
    }

    private LocalDate finnDødsdato(Bruker person) {
        LocalDate dødsdato = null;
        Doedsdato dødsdatoJaxb = person.getDoedsdato();
        if (dødsdatoJaxb != null) {
            dødsdato = DateUtil.convertToLocalDate(dødsdatoJaxb.getDoedsdato());
        }
        return dødsdato;
    }

    private LocalDate finnFødselsdato(Bruker person) {
        LocalDate fødselsdato = null;
        Foedselsdato fødselsdatoJaxb = person.getFoedselsdato();
        if (fødselsdatoJaxb != null) {
            fødselsdato = DateUtil.convertToLocalDate(fødselsdatoJaxb.getFoedselsdato());
        }
        return fødselsdato;
    }

    private NavBrukerKjønn tilBrukerKjønn(Kjoenn kjoenn) {
        return Optional.ofNullable(kjoenn)
                .map(Kjoenn::getKjoenn)
                .map(kj -> navBrukerKodeverkRepository.finnBrukerKjønn(kj.getValue()))
                .orElse(NavBrukerKjønn.UDEFINERT);
    }

    private PersonstatusType tilPersonstatusType(Personstatus personstatus) {
        return navBrukerKodeverkRepository.finnPersonstatus(personstatus.getPersonstatus().getValue());
    }

    private Landkoder utledLandkode(Statsborgerskap statsborgerskap) {
        Landkoder landkode = Landkoder.UDEFINERT;
        if (Optional.ofNullable(statsborgerskap).isPresent()) {
            landkode = behandlingsgrunnlagKodeverkRepository.finnLandkode(statsborgerskap.getLand().getValue());
        }
        return landkode;
    }

    private Språkkode bestemForetrukketSpråk(Bruker person) {
        Språkkode defaultSpråk = Språkkode.nb;
        Spraak språk = person.getMaalform();
        // For å slippe å håndtere foreldet forkortelse "NO" andre steder i løsningen
        if (språk == null || "NO".equals(språk.getValue())) {
            return defaultSpråk;
        }
        Optional<Språkkode> kode = språkKodeverkRepository.finnSpråkMedKodeverkEiersKode(språk.getValue());
        if (kode.isPresent()) {
            return kode.get();
        }
        if (logger.isInfoEnabled()) {
            logger.info("Mottok ukjent språkkode: '{}'. Defaulter til '{}'", LoggerUtils.removeLineBreaks(språk.getValue()), defaultSpråk.getKode()); //NOSONAR
        }
        return defaultSpråk;
    }
}
