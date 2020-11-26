package no.nav.foreldrepenger.tilbakekreving.domene.person.pdl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.pdl.Doedsfall;
import no.nav.pdl.DoedsfallResponseProjection;
import no.nav.pdl.Foedsel;
import no.nav.pdl.FoedselResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Kjoenn;
import no.nav.pdl.KjoennResponseProjection;
import no.nav.pdl.KjoennType;
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.pdl.Sivilstand;
import no.nav.pdl.SivilstandResponseProjection;
import no.nav.pdl.Sivilstandstype;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class AktørTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AktørTjeneste.class);

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private static final Map<Sivilstandstype, SivilstandType> SIVSTAND_FRA_FREG = Map.ofEntries(
        Map.entry(Sivilstandstype.UOPPGITT, SivilstandType.UOPPGITT),
        Map.entry(Sivilstandstype.UGIFT, SivilstandType.UGIFT),
        Map.entry(Sivilstandstype.GIFT, SivilstandType.GIFT),
        Map.entry(Sivilstandstype.ENKE_ELLER_ENKEMANN, SivilstandType.ETTERLATT),
        Map.entry(Sivilstandstype.SKILT, SivilstandType.SKILT),
        Map.entry(Sivilstandstype.SEPARERT, SivilstandType.SEPARERT),
        Map.entry(Sivilstandstype.REGISTRERT_PARTNER, SivilstandType.REGISTRERT_PARTNER),
        Map.entry(Sivilstandstype.SEPARERT_PARTNER, SivilstandType.SEPARERT_PARTNER),
        Map.entry(Sivilstandstype.SKILT_PARTNER, SivilstandType.SKILT_PARTNER),
        Map.entry(Sivilstandstype.GJENLEVENDE_PARTNER, SivilstandType.GJENLEVENDE_PARTNER)
    );

    private LRUCache<AktørId, PersonIdent> cacheAktørIdTilIdent;
    private LRUCache<PersonIdent, AktørId> cacheIdentTilAktørId;

    private PdlKlient pdlKlient;
    private Tema tema;

    AktørTjeneste() {
        // CDI
    }

    @Inject
    public AktørTjeneste(PdlKlient pdlKlient, @KonfigVerdi(value = "app.name") String applikasjon) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.tema = "fptilbake".equals(applikasjon) ? Tema.FOR : Tema.OMS;
    }

    public Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent, Optional<AktørId> aktørFraTps) {
        if (personIdent.erFdatNummer()) {
            // har ikke tildelt personnr
            return Optional.empty();
        }
        var fraCache = cacheIdentTilAktørId.get(personIdent);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        var request = new HentIdenterQueryRequest();
        request.setIdent(personIdent.getIdent());
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection()
            .identer(new IdentInformasjonResponseProjection().ident());

        final Identliste identliste;

        try {
            identliste = pdlKlient.hentIdenter(request, projection, tema);
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            LOG.info("TILBAKE PDL AKTØRID hentaktørid error", v);
            return Optional.empty();
        } catch (Exception e) {
            LOG.info("TILBAKE PDL AKTØRID hentaktørid error", e);
            return Optional.empty();
        }

        var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(AktørId::new);
        var antall = identliste.getIdenter().size();
        aktørId.ifPresent(a -> cacheIdentTilAktørId.put(personIdent, a)); // Kan ikke legge til i cache aktørId -> ident ettersom ident kan være ikke-current
        if (antall == 1 && Objects.equals(aktørFraTps, aktørId)) {
            LOG.info("FPOPPDRAG PDL AKTØRID: like aktørid");
        } else if (antall != 1 && Objects.equals(aktørFraTps, aktørId)) {
            LOG.info("FPOPPDRAG PDL AKTØRID: ulikt antall aktørid {}", antall);
        } else {
            LOG.info("FPOPPDRAG PDL AKTØRID: ulike aktørid TPS og PDL, antall {}", antall);
        }
        return aktørId;
    }

    public Optional<PersonIdent> hentPersonIdentForAktørId(AktørId aktørId, Optional<PersonIdent> fraTps) {
        var fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId.getId());
        request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection()
            .identer(new IdentInformasjonResponseProjection().ident());

        final Identliste identliste;

        try {
            identliste = pdlKlient.hentIdenter(request, projection, tema);
        } catch (VLException v) {
            if (PdlKlient.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            LOG.info("TILBAKE PDL AKTØRID hentident error", v);
            return Optional.empty();
        } catch (Exception e) {
            LOG.info("TILBAKE PDL AKTØRID hentident error", e);
            return Optional.empty();
        }

        var ident = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(PersonIdent::new);
        var antall = identliste.getIdenter().size();
        ident.ifPresent(i -> {
            cacheAktørIdTilIdent.put(aktørId, i);
            cacheIdentTilAktørId.put(i, aktørId); // OK her, men ikke over ettersom dette er gjeldende mapping
        });
        if (antall == 1 && Objects.equals(ident, fraTps)) {
            LOG.info("FPOPPDRAG PDL AKTØRID: like identer");
        } else if (antall != 1 && Objects.equals(ident, fraTps)) {
            LOG.info("FPOPPDRAG PDL AKTØRID: ulikt antall identer {}", antall);
        } else {
            LOG.info("FPOPPDRAG PDL AKTØRID: ulike identer TPS og PDL antall {}", antall);
        }
        return ident;
    }

    public void hentPersoninfo(AktørId aktørId, PersonIdent personIdent, Personinfo fraTPS) {
        try {
            var query = new HentPersonQueryRequest();
            query.setIdent(aktørId.getId());
            var projection = new PersonResponseProjection()
                .navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn())
                .foedsel(new FoedselResponseProjection().foedselsdato())
                .doedsfall(new DoedsfallResponseProjection().doedsdato())
                .kjoenn(new KjoennResponseProjection().kjoenn())
                .sivilstand(new SivilstandResponseProjection().type());


            var person = pdlKlient.hentPerson(query, projection, Tema.FOR);

            var fødselsdato = person.getFoedsel().stream()
                .map(Foedsel::getFoedselsdato)
                .filter(Objects::nonNull)
                .findFirst().map(d -> LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
            var dødssdato = person.getDoedsfall().stream()
                .map(Doedsfall::getDoedsdato)
                .filter(Objects::nonNull)
                .findFirst().map(d -> LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
            var sivilstand = person.getSivilstand().stream()
                .map(Sivilstand::getType)
                .findFirst()
                .map(st -> SIVSTAND_FRA_FREG.getOrDefault(st, SivilstandType.UOPPGITT)).orElse(SivilstandType.UOPPGITT);
            var fraPDL = new Personinfo.Builder().medAktørId(aktørId).medPersonIdent(personIdent)
                .medNavn(person.getNavn().stream().map(AktørTjeneste::mapNavn).filter(Objects::nonNull).findFirst().orElse("MANGLER NAVN"))
                .medFødselsdato(fødselsdato)
                .medDødsdato(dødssdato)
                .medNavBrukerKjønn(mapKjønn(person))
                .medSivilstandType(sivilstand)
                .build();

            if (!erLike(fraPDL, fraTPS)) {
                var avvik = finnAvvik(fraTPS, fraPDL);
                LOG.info("TILBAKE PDL FULL: avvik {}", avvik);
            }
        } catch (Exception e) {
            LOG.info("TILBAKE PDL FULL: error", e);
        }
    }

    private static String mapNavn(Navn navn) {
        if (navn.getForkortetNavn() != null)
            return navn.getForkortetNavn();
        return navn.getEtternavn() + " " + navn.getFornavn() + (navn.getMellomnavn() == null ? "" : " " + navn.getMellomnavn());
    }

    private static NavBrukerKjønn mapKjønn(Person person) {
        var kode = person.getKjoenn().stream()
            .map(Kjoenn::getKjoenn)
            .filter(Objects::nonNull)
            .findFirst().orElse(KjoennType.UKJENT);
        if (KjoennType.MANN.equals(kode))
            return NavBrukerKjønn.MANN;
        return KjoennType.KVINNE.equals(kode) ? NavBrukerKjønn.KVINNE : NavBrukerKjønn.UDEFINERT;
    }

    private static boolean erLike(Personinfo pdl, Personinfo tps) {
        if (tps == null && pdl == null) return true;
        if (pdl == null || tps == null || tps.getClass() != pdl.getClass()) return false;
        return // Objects.equals(pdl.getNavn(), tps.getNavn()) && - avvik skyldes tegnsett
            Objects.equals(pdl.getFødselsdato(), tps.getFødselsdato()) &&
                Objects.equals(pdl.getDødsdato(), tps.getDødsdato()) &&
                pdl.getKjønn() == tps.getKjønn() &&
                pdl.getSivilstandType() == tps.getSivilstandType();
    }

    private static String finnAvvik(Personinfo tps, Personinfo pdl) {
        //String navn = Objects.equals(tps.getNavn(), pdl.getNavn()) ? "" : " navn ";
        String kjonn = Objects.equals(tps.getKjønn(), pdl.getKjønn()) ? "" : " kjønn ";
        String fdato = Objects.equals(tps.getFødselsdato(), pdl.getFødselsdato()) ? "" : " fødsel ";
        String ddato = Objects.equals(tps.getDødsdato(), pdl.getDødsdato()) ? "" : " død ";
        String sivstand = Objects.equals(tps.getSivilstandType(), pdl.getSivilstandType()) ? "" : " sivilst " + tps.getSivilstandType().getKode() + " PDL " + pdl.getSivilstandType().getKode();

        return "Avvik" + kjonn + fdato + ddato + sivstand ;
    }
}
