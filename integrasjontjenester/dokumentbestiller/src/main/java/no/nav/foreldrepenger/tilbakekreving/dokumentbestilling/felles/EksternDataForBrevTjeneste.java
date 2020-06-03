package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.Virksomhet;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.VirksomhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.klient.FpOppdragRestKlient;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Transactional
//FIXME k9-tilbake
// .. splitt eksternDataForBrevTjeneste i 2 (hvorav 1 del er for å hente fra fagsystemet)
// .. lag 2 implementasjoner av fagsystemdelen
// .. hentFeilutbetaltePerioder bør ta inn henvisning (eller intern behandlingId) og konvertere til eksernid/uuid
public class EksternDataForBrevTjeneste {

    private FpOppdragRestKlient fpOppdragKlient;
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private KodeverkRepository kodeverkRepository;
    private FpsakKlient fpsakKlient;
    private Period brukersSvarfrist;

    @Inject
    public EksternDataForBrevTjeneste(FpOppdragRestKlient fpOppdragKlient,
                                      TpsTjeneste tpsTjeneste,
                                      VirksomhetTjeneste virksomhetTjeneste,
                                      FpsakKlient fpsakKlient,
                                      KodeverkRepository kodeverkRepository,
                                      @KonfigVerdi(value = "brukertilbakemelding.venter.frist.lengde") Period brukersSvarfrist) {
        this.fpOppdragKlient = fpOppdragKlient;
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.fpsakKlient = fpsakKlient;
        this.brukersSvarfrist = brukersSvarfrist;
    }

    public EksternDataForBrevTjeneste() {
        //NOSONAR
    }

    public Period getBrukersSvarfrist() {
        return brukersSvarfrist;
    }

    public String finnFagsaktypeNavnPåRiktigSpråk(FagsakYtelseType fagsaktype, Språkkode sprakkode) {
        FagsakYtelseType fagsakYtelseType = kodeverkRepository.finn(FagsakYtelseType.class, fagsaktype.getKode());
        List<KodelisteNavnI18N> kodelisteNavnI18NList = fagsakYtelseType.getKodelisteNavnI18NList();
        return BrevSpråkUtil.finnFagsaktypenavnPåAngittSpråk(kodelisteNavnI18NList, sprakkode);
    }

    public SamletEksternBehandlingInfo hentYtelsesbehandlingFraFagsystemet(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        SamletEksternBehandlingInfo behandlingsinfo = fpsakKlient.hentBehandlingsinfo(eksternUuid, tillegsinformasjon);
        if (behandlingsinfo.getGrunninformasjon() == null) {
            throw EksternDataForBrevFeil.FACTORY.fantIkkeYtelesbehandlingIFagsystemet(eksternUuid.toString()).toException();
        }
        return behandlingsinfo;
    }

    public Personinfo hentPerson(String aktørId) {
        Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(new AktørId(aktørId));
        if (!personinfo.isPresent()) {
            throw EksternDataForBrevFeil.FACTORY.fantIkkeAdresseForAktørId(aktørId).toException();
        }
        return personinfo.get();
    }

    public Adresseinfo hentAdresse(Personinfo personinfo, String aktørId) {
        Optional<Adresseinfo> adresseinfo = Optional.of(personinfo).map(s -> tpsTjeneste.hentAdresseinformasjon(s.getPersonIdent()));
        if (!adresseinfo.isPresent()) {
            throw EksternDataForBrevFeil.FACTORY.fantIkkeAdresseForAktørId(aktørId).toException();
        }
        return adresseinfo.get();
    }

    public Adresseinfo hentOrganisasjonAdresse(String organisasjonNummer, String vergeNavn, Personinfo personinfo) {
        try {
            Virksomhet virksomhet = virksomhetTjeneste.hentOrganisasjon(organisasjonNummer);
            return fra(virksomhet, vergeNavn, personinfo);
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet e) {
            throw EksternDataForBrevFeil.FACTORY.organisasjonIkkeFunnet(organisasjonNummer, e).toException();
        } catch (HentOrganisasjonUgyldigInput e) {
            throw EksternDataForBrevFeil.FACTORY.ugyldigInput(organisasjonNummer, e).toException();
        }
    }

    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Long eksternBehandlingId) {
        Optional<FeilutbetaltePerioderDto> feilutbetaltePerioderDto = fpOppdragKlient.hentFeilutbetaltePerioder(eksternBehandlingId); //tilpasse feilmelding til eksternid
        if (!feilutbetaltePerioderDto.isPresent()) {
            throw EksternDataForBrevFeil.FACTORY.fantIkkeBehandlingIFpoppdrag(eksternBehandlingId).toException();
        }
        return feilutbetaltePerioderDto.get();
    }

    public YtelseNavn hentYtelsenavn(FagsakYtelseType ytelsetype, Språkkode språkkode) {
        YtelseNavn ytelseNavn = new YtelseNavn();
        String ytelsePåBokmål = finnFagsaktypeNavnPåRiktigSpråk(ytelsetype, Språkkode.nb);
        ytelseNavn.setNavnPåBokmål(ytelsePåBokmål);

        if (språkkode != null && !språkkode.equals(Språkkode.nb)) {
            ytelseNavn.setNavnPåBrukersSpråk(finnFagsaktypeNavnPåRiktigSpråk(ytelsetype, språkkode));
        } else {
            ytelseNavn.setNavnPåBrukersSpråk(ytelsePåBokmål);
        }
        return ytelseNavn;
    }

    private Adresseinfo fra(Virksomhet virksomhet, String vergeNavn, Personinfo personinfo) {
        String navn = virksomhet.getNavn() + " c/o " + vergeNavn;
        Adresseinfo.Builder adresseinfo = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, personinfo.getPersonIdent(), navn, personinfo.getPersonstatus());
        return adresseinfo.medAdresselinje1(virksomhet.getAdresselinje1())
            .medAdresselinje2(virksomhet.getAdresselinje2())
            .medAdresselinje3(virksomhet.getAdresselinje3())
            .medAdresselinje4(virksomhet.getAdresselinje4())
            .medLand(virksomhet.getLandkode())
            .medPostNr(virksomhet.getPostNr())
            .medPoststed(virksomhet.getPoststed()).build();
    }

}
