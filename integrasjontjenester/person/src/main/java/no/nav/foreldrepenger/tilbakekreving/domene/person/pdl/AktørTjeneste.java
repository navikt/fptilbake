package no.nav.foreldrepenger.tilbakekreving.domene.person.pdl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
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
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.pdl.Sivilstand;
import no.nav.pdl.SivilstandResponseProjection;
import no.nav.pdl.Sivilstandstype;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.felles.integrasjon.person.Tema;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class AktørTjeneste {

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

    private final LRUCache<AktørId, PersonIdent> cacheAktørIdTilIdent;
    private final LRUCache<PersonIdent, AktørId> cacheIdentTilAktørId;

    private final Persondata pdlKlient;
    private final Tema tema;

    public AktørTjeneste() {
        this.tema = switch (ApplicationName.hvilkenTilbake()) {
            case FPTILBAKE -> Tema.FOR;
            case K9TILBAKE -> Tema.OMS;
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + ApplicationName.hvilkenTilbake() + " som ikke er en støttet verdi");
        };
        var behandling = switch (ApplicationName.hvilkenTilbake()) {
            case FPTILBAKE -> Persondata.Ytelse.FORELDREPENGER;
            case K9TILBAKE -> Persondata.Ytelse.PLEIEPENGER;
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + ApplicationName.hvilkenTilbake() + " som ikke er en støttet verdi");
        };
        this.pdlKlient = new PdlKlient(tema, behandling);
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public Optional<AktørId> hentAktørIdForPersonIdent(PersonIdent personIdent) {
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
            identliste = pdlKlient.hentIdenter(request, projection);
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }

        var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(AktørId::new);
        aktørId.ifPresent(a -> cacheIdentTilAktørId.put(personIdent, a)); // Kan ikke legge til i cache aktørId -> ident ettersom ident kan være ikke-current
        return aktørId;
    }

    public Optional<PersonIdent> hentPersonIdentForAktørId(AktørId aktørId) {
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
            identliste = pdlKlient.hentIdenter(request, projection);
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }

        var ident = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(PersonIdent::new);
        ident.ifPresent(i -> {
            cacheAktørIdTilIdent.put(aktørId, i);
            cacheIdentTilAktørId.put(i, aktørId); // OK her, men ikke over ettersom dette er gjeldende mapping
        });
        return ident;
    }

    public Personinfo hentPersoninfo(FagsakYtelseType ytelseType, AktørId aktørId, PersonIdent personIdent) {
        var query = new HentPersonQueryRequest();
        query.setIdent(aktørId.getId());
        var projection = new PersonResponseProjection()
                .navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn())
                .foedsel(new FoedselResponseProjection().foedselsdato())
                .doedsfall(new DoedsfallResponseProjection().doedsdato())
                .sivilstand(new SivilstandResponseProjection().type());

        var ytelse = utledYtelse(ytelseType);
        var person = pdlKlient.hentPerson(ytelse, query, projection);

        var fødselsdato = person.getFoedsel().stream()
                .map(Foedsel::getFoedselsdato)
                .filter(Objects::nonNull)
                .findFirst().map(d -> LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
        var dødsdato = person.getDoedsfall().stream()
                .map(Doedsfall::getDoedsdato)
                .filter(Objects::nonNull)
                .findFirst().map(d -> LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);
        var sivilstand = person.getSivilstand().stream()
                .map(Sivilstand::getType)
                .findFirst()
                .map(st -> SIVSTAND_FRA_FREG.getOrDefault(st, SivilstandType.UOPPGITT)).orElse(SivilstandType.UOPPGITT);

        return new Personinfo.Builder()
                .medAktørId(aktørId).medPersonIdent(personIdent)
                .medNavn(person.getNavn().stream().map(AktørTjeneste::mapNavn).filter(Objects::nonNull).findFirst().orElse("MANGLER NAVN"))
                .medFødselsdato(fødselsdato)
                .medDødsdato(dødsdato)
                .medSivilstandType(sivilstand)
                .build();
    }

    // Mulighet - skriv om til nullsafe fornavn + mellomnavn + etternavn. Forkortet = TPS
    private static String mapNavn(Navn navn) {
        if (navn.getForkortetNavn() != null)
            return navn.getForkortetNavn();
        return navn.getEtternavn() + " " + navn.getFornavn() + (navn.getMellomnavn() == null ? "" : " " + navn.getMellomnavn());
    }

    private Persondata.Ytelse utledYtelse(FagsakYtelseType ytelseType) {
        if (Tema.FOR.equals(this.tema)) {
            if (FagsakYtelseType.ENGANGSTØNAD.equals(ytelseType)) {
                return Persondata.Ytelse.ENGANGSSTØNAD;
            } else if (FagsakYtelseType.SVANGERSKAPSPENGER.equals(ytelseType)) {
                return Persondata.Ytelse.SVANGERSKAPSPENGER;
            } else {
                return Persondata.Ytelse.FORELDREPENGER;
            }
        } else {
            return Persondata.Ytelse.PLEIEPENGER;
        }
    }

}
