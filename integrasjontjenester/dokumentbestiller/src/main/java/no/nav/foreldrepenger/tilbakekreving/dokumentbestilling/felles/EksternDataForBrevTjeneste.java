package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteNavnI18N;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.simulering.klient.FpOppdragRestKlient;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Transaction
public class EksternDataForBrevTjeneste {

    private FpOppdragRestKlient fpOppdragKlient;
    private TpsTjeneste tpsTjeneste;
    private KodeverkRepository kodeverkRepository;
    private FpsakKlient fpsakKlient;
    private Period brukersSvarfrist;

    @Inject
    public EksternDataForBrevTjeneste(FpOppdragRestKlient fpOppdragKlient,
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

    public int antallUkerKlagefrist() {
        return brukersSvarfrist.getDays() / 7;
    }

    public SamletEksternBehandlingInfo hentBehandlingFpsak(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        SamletEksternBehandlingInfo behandlingsinfo = fpsakKlient.hentBehandlingsinfo(eksternUuid, tillegsinformasjon);
        if (behandlingsinfo.getGrunninformasjon() == null) {
            throw EksternDataForBrevFeil.FACTORY.fantIkkeBehandlingIFpsak(eksternUuid.toString()).toException();
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

    public KodeDto henteFagsakYtelseType(Behandling behandling) {
        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        return new KodeDto(fagsakYtelseType.getKodeverk(), fagsakYtelseType.getKode(), fagsakYtelseType.getNavn());
    }

}
