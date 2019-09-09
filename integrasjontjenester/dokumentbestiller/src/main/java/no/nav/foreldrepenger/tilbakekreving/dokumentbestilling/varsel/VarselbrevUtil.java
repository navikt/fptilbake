package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.YtelseNavn;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfoDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.PeriodeDto;
import no.nav.vedtak.util.FPDateUtil;

public class VarselbrevUtil {

    private VarselbrevUtil() {
        // for static access
    }

    public static VarselbrevSamletInfo sammenstillInfoFraFagsystemerForSending(
        SamletEksternBehandlingInfoDto eksternBehandlingsinfoDto,
        Saksnummer saksnummer,
        Adresseinfo adresseinfo,
        Personinfo personinfo,
        FeilutbetaltePerioderDto feilutbetaltePerioderDto,
        Period ventetid,
        YtelseNavn ytelseNavn) {

        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfoDto.getGrunninformasjon();
        BrevMetadata metadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(grunninformasjon.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(grunninformasjon.getBehandlendeEnhetNavn())
            .medSakspartId(personinfo.getPersonIdent().getIdent())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(saksnummer.getVerdi())
            .medSakspartNavn(personinfo.getNavn())
            .medFagsaktypenavnPåSpråk(ytelseNavn.getNavnPåBrukersSpråk())
            .medFagsaktype(grunninformasjon.getFagsaktype())
            .medSprakkode(personinfo.getForetrukketSpråk())
            .medAnsvarligSaksbehandler(StringUtils.isNotEmpty(grunninformasjon.getAnsvarligSaksbehandler()) ? grunninformasjon.getAnsvarligSaksbehandler() : "VL")
            .medTittel(VarselbrevOverskrift.finnTittelVarselbrev(ytelseNavn.getNavnPåBokmål()))
            .build();

        return new VarselbrevSamletInfo.Builder()
            .medMetadata(metadata)
            .medFritekstFraSaksbehandler(eksternBehandlingsinfoDto.getVarseltekst())
            .medSumFeilutbetaling(feilutbetaltePerioderDto.getSumFeilutbetaling())
            .medFeilutbetaltePerioder(mapFeilutbetaltePerioder(feilutbetaltePerioderDto))
            .medFristdato(finnFristForTilbakemeldingFraBruker(FPDateUtil.nå(), ventetid))
            .build();
    }

    public static VarselbrevSamletInfo sammenstillInfoFraFagsystemerForhåndvisningVarselbrev(
        Saksnummer saksnummer,
        String varseltekst,
        Adresseinfo adresseinfo,
        SamletEksternBehandlingInfoDto eksternBehandlingsinfo,
        FeilutbetaltePerioderDto feilutbetaltePerioderDto,
        Period ventetid,
        YtelseNavn ytelseNavn) {

        EksternBehandlingsinfoDto grunninformasjon = eksternBehandlingsinfo.getGrunninformasjon();
        BrevMetadata brevMetadata = new BrevMetadata.Builder()
            .medBehandlendeEnhetId(grunninformasjon.getBehandlendeEnhetId())
            .medBehandlendeEnhetNavn(grunninformasjon.getBehandlendeEnhetNavn())
            .medSakspartId(eksternBehandlingsinfo.getPersonopplysninger().getFødselsnummer())
            .medMottakerAdresse(adresseinfo)
            .medSaksnummer(saksnummer.getVerdi())
            .medSakspartNavn(eksternBehandlingsinfo.getPersonopplysninger().getNavn())
            .medFagsaktype(grunninformasjon.getFagsaktype())
            .medSprakkode(grunninformasjon.getSprakkode())
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
            .build();
    }

    private static List<Periode> mapFeilutbetaltePerioder(FeilutbetaltePerioderDto feilutbetaltePerioderDto) {
        ArrayList<Periode> feilutbetaltPerioder = new ArrayList<>();
        for (PeriodeDto periodeDto : feilutbetaltePerioderDto.getPerioder()) {
            feilutbetaltPerioder.add(new Periode(periodeDto.getFom(), periodeDto.getTom()));
        }
        return feilutbetaltPerioder;
    }

    static LocalDate finnFristForTilbakemeldingFraBruker(LocalDateTime dagensDato, Period ventetid) {
        return dagensDato.plus(ventetid).toLocalDate();
    }
}
