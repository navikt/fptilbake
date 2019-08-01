package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.TittelOverskriftUtil;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.klient.FpOppdragRestKlient;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.konfig.KonfigVerdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transaction
public class FellesInfoTilBrevTjeneste {

    private FpOppdragRestKlient fpOppdragKlient;
    private TpsTjeneste tpsTjeneste;
    private KodeverkRepository kodeverkRepository;
    private FpsakKlient fpsakKlient;
    private Period brukersSvarfrist;

    @Inject
    public FellesInfoTilBrevTjeneste(FpOppdragRestKlient fpOppdragKlient,
                                     TpsTjeneste tpsTjeneste,
                                     FpsakKlient fpsakKlient,
                                     KodeverkRepository kodeverkRepository,
                                     @KonfigVerdi(value = "behandling.venter.frist.lengde") Period brukersSvarfrist) {
        this.fpOppdragKlient = fpOppdragKlient;
        this.tpsTjeneste = tpsTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.fpsakKlient = fpsakKlient;
        this.brukersSvarfrist = brukersSvarfrist;
    }

    public FellesInfoTilBrevTjeneste() {
        //NOSONAR
    }

    public Period getBrukersSvarfrist() {
        return brukersSvarfrist;
    }

    public String finnFagsaktypeNavnPåRiktigSpråk(KodeDto fagsaktype, Språkkode sprakkode) {
        FagsakYtelseType fagsakYtelseType = kodeverkRepository.finn(FagsakYtelseType.class, fagsaktype.getKode());
        List<KodelisteNavnI18N> kodelisteNavnI18NList = fagsakYtelseType.getKodelisteNavnI18NList();
        return BrevUtil.finnFagsaktypenavnPåAngittSpråk(kodelisteNavnI18NList, sprakkode);
    }

    public int antallUkerKlagefrist() {
        return brukersSvarfrist.getDays() / 7;
    }

    public EksternBehandlingsinfoDto hentBehandlingFpsak(Long behandlingId, String saksnummer) {
        Optional<EksternBehandlingsinfoDto> dokumentinfoDto = fpsakKlient.hentBehandlingsinfo(behandlingId, saksnummer);
        if (!dokumentinfoDto.isPresent()) {
            throw DokumentbestillingFeil.FACTORY.fantIkkeBehandlingIFpsak(behandlingId).toException();
        }
        return dokumentinfoDto.get();
    }

    public Personinfo hentPerson(String aktørId) {
        Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(new AktørId(aktørId));
        if (!personinfo.isPresent()) {
            throw DokumentbestillingFeil.FACTORY.fantIkkeAdresseForAktørId(aktørId).toException();
        }
        return personinfo.get();
    }

    public Adresseinfo hentAdresse(Personinfo personinfo, String aktørId) {
        Optional<Adresseinfo> adresseinfo = Optional.of(personinfo).map(s -> tpsTjeneste.hentAdresseinformasjon(s.getPersonIdent()));
        if (!adresseinfo.isPresent()) {
            throw DokumentbestillingFeil.FACTORY.fantIkkeAdresseForAktørId(aktørId).toException();
        }
        return adresseinfo.get();
    }

    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Long eksternBehandlingId) {
        Optional<FeilutbetaltePerioderDto> feilutbetaltePerioderDto = fpOppdragKlient.hentFeilutbetaltePerioder(eksternBehandlingId); //tilpasse feilmelding til eksternid
        if (!feilutbetaltePerioderDto.isPresent()) {
            throw DokumentbestillingFeil.FACTORY.fantIkkeBehandlingIFpoppdrag(eksternBehandlingId).toException();
        }
        return feilutbetaltePerioderDto.get();
    }

    public YtelseNavn hentYtelsenavn(KodeDto ytelsetype, Språkkode språkkode) {
        YtelseNavn ytelseNavn = new YtelseNavn();
        String ytelsePåBokmål = finnFagsaktypeNavnPåRiktigSpråk(ytelsetype, Språkkode.nb);
        ytelseNavn.setNavnPåBokmål(ytelsePåBokmål);

        if (!språkkode.equals(Språkkode.nb)) {
            ytelseNavn.setNavnPåBrukersSpråk(finnFagsaktypeNavnPåRiktigSpråk(ytelsetype, språkkode));
        } else {
            ytelseNavn.setNavnPåBrukersSpråk(ytelsePåBokmål);
        }
        return ytelseNavn;
    }

    BrevMetadata lagMetadataForVedtaksbrev(Behandling behandling, Long totalTilbakekrevingBeløp, Long behandlingIdIFpsak) {
        String saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        EksternBehandlingsinfoDto eksternBehandlingsinfo = hentBehandlingFpsak(behandlingIdIFpsak, saksnummer);
        String aktørId = eksternBehandlingsinfo.getPersonopplysningDto().getAktoerId();
        Personinfo personinfo = hentPerson(aktørId);
        Adresseinfo adresseinfo = hentAdresse(personinfo, aktørId);
        YtelseNavn ytelseNavn = hentYtelsenavn(eksternBehandlingsinfo.getFagsaktype(), eksternBehandlingsinfo.getSprakkode());

        return new BrevMetadata.Builder()
            .medAnsvarligSaksbehandler(behandling.getAnsvarligBeslutter())
            .medBehandlendeEnhetId("4833") //FIXME fjern hardkoding
            .medBehandlendeEnhetNavn("NAV Familie- og pensjonsytelser Oslo 1") //FIXME fjern hardkoding
            .medMottakerAdresse(adresseinfo)
            .medFagsaktype(eksternBehandlingsinfo.getFagsaktype())
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medSakspartNavn(personinfo.getNavn())
            .medSprakkode(personinfo.getForetrukketSpråk())
            .medTittel(TittelOverskriftUtil.finnTittelVedtaksbrev(ytelseNavn.getNavnPåBokmål(), totalTilbakekrevingBeløp > 0))
            .build();
    }
}
