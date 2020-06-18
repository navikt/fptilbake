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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.Virksomhet;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.VirksomhetTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Transactional
public class EksternDataForBrevTjeneste {

    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private KodeverkRepository kodeverkRepository;
    private FagsystemKlient fagsystemKlient;
    private Period brukersSvarfrist;

    @Inject
    public EksternDataForBrevTjeneste(TpsTjeneste tpsTjeneste,
                                      VirksomhetTjeneste virksomhetTjeneste,
                                      FagsystemKlient fagsystemKlient,
                                      KodeverkRepository kodeverkRepository,
                                      @KonfigVerdi(value = "brukertilbakemelding.venter.frist.lengde") Period brukersSvarfrist) {
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.fagsystemKlient = fagsystemKlient;
        this.brukersSvarfrist = brukersSvarfrist;
    }

    public EksternDataForBrevTjeneste() {
        //NOSONAR
    }

    //TODO dette er ikke ekstern data, flytt til annen tjeneste
    public Period getBrukersSvarfrist() {
        return brukersSvarfrist;
    }

    public String finnFagsaktypeNavnPåRiktigSpråk(FagsakYtelseType fagsaktype, Språkkode sprakkode) {
        FagsakYtelseType fagsakYtelseType = kodeverkRepository.finn(FagsakYtelseType.class, fagsaktype.getKode());
        List<KodelisteNavnI18N> kodelisteNavnI18NList = fagsakYtelseType.getKodelisteNavnI18NList();
        return BrevSpråkUtil.finnFagsaktypenavnPåAngittSpråk(kodelisteNavnI18NList, sprakkode);
    }

    public SamletEksternBehandlingInfo hentYtelsesbehandlingFraFagsystemet(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        return fagsystemKlient.hentBehandlingsinfo(eksternUuid, tillegsinformasjon);
    }

    public Personinfo hentPerson(String aktørId) {
        return tpsTjeneste.hentBrukerForAktør(new AktørId(aktørId))
            .orElseThrow(() -> EksternDataForBrevFeil.FACTORY.fantIkkePersoniTPS(aktørId).toException());
    }

    //TODO Bør endre signatur til (PersonIdent) siden det kun er den som brukes
    public Adresseinfo hentAdresse(Personinfo personinfo) {
        return tpsTjeneste.hentAdresseinformasjon(personinfo.getPersonIdent());
    }

    public Adresseinfo hentAdresse(Personinfo personinfo, BrevMottaker brevMottaker, Optional<VergeEntitet> vergeEntitet) {
        if (vergeEntitet.isPresent()) {
            VergeEntitet verge = vergeEntitet.get();
            if (VergeType.ADVOKAT.equals(verge.getVergeType())) {
                return hentOrganisasjonAdresse(verge.getOrganisasjonsnummer(), verge.getNavn(), personinfo, brevMottaker);
            } else {
                String aktørId = verge.getVergeAktørId().getId();
                personinfo = hentPerson(aktørId);
            }
        }
        return hentAdresse(personinfo);
    }

    private Adresseinfo hentOrganisasjonAdresse(String organisasjonNummer, String vergeNavn, Personinfo personinfo, BrevMottaker brevMottaker) {
        Virksomhet virksomhet = virksomhetTjeneste.hentOrganisasjon(organisasjonNummer);
        return fra(virksomhet, vergeNavn, personinfo, brevMottaker);
    }

    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning) {
        return fagsystemKlient.hentFeilutbetaltePerioder(henvisning);
    }

    //TODO dette er ikke ekstern data, flytt til annen tjeneste
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

    private Adresseinfo fra(Virksomhet virksomhet, String vergeNavn, Personinfo personinfo, BrevMottaker brevMottaker) {
        String organisasjonNavn = virksomhet.getNavn();
        String vedVergeNavn = "v/ " + vergeNavn;
        String annenMottakerNavn = organisasjonNavn + " " + vedVergeNavn;
        Adresseinfo adresseinfo;
        if (BrevMottaker.VERGE.equals(brevMottaker)) {
            Adresseinfo.Builder adresseinfoBuilder = new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, personinfo.getPersonIdent(), organisasjonNavn, personinfo.getPersonstatus());
            adresseinfo = adresseinfoBuilder.medAdresselinje1(vedVergeNavn)
                .medAdresselinje2(virksomhet.getAdresselinje1())
                .medAdresselinje3(virksomhet.getAdresselinje2())
                .medAdresselinje4(virksomhet.getAdresselinje3())
                .medLand(virksomhet.getLandkode())
                .medPostNr(virksomhet.getPostNr())
                .medPoststed(virksomhet.getPoststed())
                .medAnnenMottakerNavn(personinfo.getNavn()).build();
        } else {
            adresseinfo = hentAdresse(personinfo);
            adresseinfo.setAnnenMottakerNavn(annenMottakerNavn);
        }
        return adresseinfo;
    }

}
