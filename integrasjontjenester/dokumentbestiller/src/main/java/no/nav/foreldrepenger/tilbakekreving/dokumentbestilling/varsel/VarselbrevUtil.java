package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.PeriodeDto;
import no.nav.vedtak.util.FPDateUtil;

public class VarselbrevUtil {

    private VarselbrevUtil() {
        // for static access
    }

    public static VarselbrevSamletInfo sammenstillInfoFraFagsystemerForSending(//NOSONAR
                                                                               SamletEksternBehandlingInfo eksternBehandlingsinfoDto,
                                                                               Saksnummer saksnummer,
                                                                               Adresseinfo adresseinfo,
                                                                               Personinfo personinfo,
                                                                               FeilutbetaltePerioderDto feilutbetaltePerioderDto,
                                                                               Period ventetid,
                                                                               FagsakYtelseType fagsakYtelseType,
                                                                               YtelseNavn ytelseNavn,
                                                                               String varselTekst) {

        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfoDto.getGrunninformasjon();
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(grunninformasjon.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(grunninformasjon.getBehandlendeEnhetNavn())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(saksnummer.getVerdi())
            .medSakspartNavn(personinfo.getNavn())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medFagsaktype(fagsakYtelseType)
            .medSprakkode(personinfo.getForetrukketSpråk())
            .medAnsvarligSaksbehandler("VL")
            .medTittel(VarselbrevOverskrift.finnTittelVarselbrev(ytelseNavn.getNavnPåBokmål()))
            .build();

        return new VarselbrevSamletInfo.Builder()
            .medMetadata(metadata)
            .medFritekstFraSaksbehandler(varselTekst)
            .medSumFeilutbetaling(feilutbetaltePerioderDto.getSumFeilutbetaling())
            .medFeilutbetaltePerioder(mapFeilutbetaltePerioder(feilutbetaltePerioderDto))
            .medFristdato(finnFristForTilbakemeldingFraBruker(FPDateUtil.nå(), ventetid))
            .medRevurderingVedtakDato(grunninformasjon.getVedtakDato())
            .build();
    }

    public static VarselbrevSamletInfo sammenstillInfoFraFagsystemerForhåndvisningVarselbrev( //NOSONAR
                                                                                              Saksnummer saksnummer,
                                                                                              String varseltekst,
                                                                                              Adresseinfo adresseinfo,
                                                                                              SamletEksternBehandlingInfo eksternBehandlingsinfo,
                                                                                              FeilutbetaltePerioderDto feilutbetaltePerioderDto,
                                                                                              Period ventetid,
                                                                                              FagsakYtelseType fagsakYtelseType,
                                                                                              YtelseNavn ytelseNavn) {

        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfo.getGrunninformasjon();
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(grunninformasjon.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(grunninformasjon.getBehandlendeEnhetNavn())
            .medSakspartId(eksternBehandlingsinfo.getPersonopplysninger().getFødselsnummer())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(saksnummer.getVerdi())
            .medSakspartNavn(eksternBehandlingsinfo.getPersonopplysninger().getNavn())
            .medFagsaktype(fagsakYtelseType)
            .medSprakkode(grunninformasjon.getSpråkkodeEllerDefault())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medAnsvarligSaksbehandler(grunninformasjon.getAnsvarligSaksbehandler())
            .medTittel(VarselbrevOverskrift.finnTittelVarselbrev(ytelseNavn.getNavnPåBokmål()))
            .build();

        return new VarselbrevSamletInfo.Builder()
            .medMetadata(brevMetadata)
            .medFritekstFraSaksbehandler(varseltekst)
            .medSumFeilutbetaling(feilutbetaltePerioderDto.getSumFeilutbetaling())
            .medFeilutbetaltePerioder(mapFeilutbetaltePerioder(feilutbetaltePerioderDto))
            .medFristdato(finnFristForTilbakemeldingFraBruker(FPDateUtil.nå(), ventetid))
            .medRevurderingVedtakDato(grunninformasjon.getVedtakDato())
            .build();
    }

    public static VarselbrevSamletInfo sammenstillInfoFraFagsystemerForSendingManueltVarselBrev(//NOSONAR
        Behandling behandling,
        Personinfo personinfo,
        Adresseinfo adresseinfo,
        FagsakYtelseType fagsakYtelseType,
        Språkkode språkkode,
        YtelseNavn ytelseNavn,
        Period ventetid,
        String friTekst,
        BehandlingFeilutbetalingFakta feilutbetalingFakta,
        boolean erKorrigert) {

        BrevMetadata metadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(behandling.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(behandling.getBehandlendeEnhetNavn())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medSakspartNavn(personinfo.getNavn())
            .medFagsaktype(fagsakYtelseType)
            .medSprakkode(språkkode)
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medAnsvarligSaksbehandler("VL")
            .medTittel(erKorrigert ? VarselbrevOverskrift.finnTittelKorrigertVarselbrev(ytelseNavn.getNavnPåBokmål()) : VarselbrevOverskrift.finnTittelVarselbrev(ytelseNavn.getNavnPåBokmål()))
            .build();

        return new VarselbrevSamletInfo.Builder()
            .medMetadata(metadata)
            .medFritekstFraSaksbehandler(friTekst)
            .medSumFeilutbetaling(feilutbetalingFakta.getAktuellFeilUtbetaltBeløp().longValue())
            .medFeilutbetaltePerioder(mapFeilutbetaltePerioder(feilutbetalingFakta))
            .medFristdato(finnFristForTilbakemeldingFraBruker(FPDateUtil.nå(), ventetid))
            .medRevurderingVedtakDato(feilutbetalingFakta.getDatoForRevurderingsvedtak())
            .build();
    }

    private static List<Periode> mapFeilutbetaltePerioder(FeilutbetaltePerioderDto feilutbetaltePerioderDto) {
        ArrayList<Periode> feilutbetaltPerioder = new ArrayList<>();
        for (PeriodeDto periodeDto : feilutbetaltePerioderDto.getPerioder()) {
            feilutbetaltPerioder.add(new Periode(periodeDto.getFom(), periodeDto.getTom()));
        }
        return feilutbetaltPerioder;
    }

    private static List<Periode> mapFeilutbetaltePerioder(BehandlingFeilutbetalingFakta feilutbetalingFakta) {
        return feilutbetalingFakta.getPerioder().stream()
            .map(utbetaltPeriode -> new Periode(utbetaltPeriode.getFom(), utbetaltPeriode.getTom()))
            .collect(Collectors.toList());
    }

    static LocalDate finnFristForTilbakemeldingFraBruker(LocalDateTime dagensDato, Period ventetid) {
        return dagensDato.plus(ventetid).toLocalDate();
    }

}
