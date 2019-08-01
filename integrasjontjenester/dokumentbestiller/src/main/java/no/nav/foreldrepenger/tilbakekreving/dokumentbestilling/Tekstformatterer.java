package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.PeriodeMedBrevtekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.ReturadresseKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VedtaksbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.FeilutbetalingsperiodeMedTekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.VedtaksbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevUtil;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.vedtak.util.FPDateUtil;

class Tekstformatterer {

    private Tekstformatterer(){
        // for static access
    }

    static String lagVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("/templates/varsel");
            VarselbrevDokument varselbrevDokument = mapTilVarselbrevDokument(
                varselbrevSamletInfo,
                FPDateUtil.nå());

            return template.apply(varselbrevDokument);
        } catch (IOException e) {
            throw DokumentbestillingFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    static String lagVedtaksbrevFritekst(VedtaksbrevSamletInfo vedtaksbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("/templates/vedtak");
            VedtaksbrevDokument vedtaksbrevDokument = mapTilVedtaksbrevDokument(
                vedtaksbrevSamletInfo);

            return template.apply(vedtaksbrevDokument);
        } catch (IOException e) {
            throw DokumentbestillingFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti) throws IOException {
        Handlebars handlebars = new Handlebars();
        handlebars.setCharset(Charset.forName("latin1"));
        handlebars.setInfiniteLoops(false);
        handlebars.setPrettyPrint(true);
        handlebars.registerHelper("datoformat", datoformatHelper());
        return handlebars.compile(filsti);
    }

    private static Helper<Object> datoformatHelper() {
        return (value, options) -> BrevUtil.konverterFraLocaldateTilTekst((LocalDate) value);
    }

    private static void settFagsaktype(BaseDokument baseDokument, KodeDto fagsaktype) {
        if (FagsakYtelseType.ENGANGSTØNAD.getKode().equals(fagsaktype.getKode())) {
            baseDokument.setEngangsstonad(true);
        } else if (FagsakYtelseType.FORELDREPENGER.getKode().equals(fagsaktype.getKode())) {
            baseDokument.setForeldrepenger(true);
        } else if (FagsakYtelseType.SVANGERSKAPSPENGER.getKode().equals(fagsaktype.getKode())) {
            baseDokument.setSvangerskapspenger(true);
        } else {
            throw new IllegalArgumentException("Utviklerfeil - Kunne ikke finne fagsaktype: " + fagsaktype.getKode());
        }
    }

    private static void settSenesteOgTidligsteDatoer(VarselbrevDokument varselbrevDokument, List<Periode> feilutbetaltPerioder) {
        if (feilutbetaltPerioder != null && feilutbetaltPerioder.size() == 1) {
            LocalDate fom = feilutbetaltPerioder.get(0).getFom();
            LocalDate tom = feilutbetaltPerioder.get(0).getTom();
            varselbrevDokument.setDatoerHvisSammenhengendePeriode(new Periode(fom, tom));
        }
    }

    static BaseDokument.Lokale finnRiktigSpråk(Språkkode språkkode) {
        if (Språkkode.nn.equals(språkkode)) {
            return BaseDokument.Lokale.NYNORSK;
        } else if (Språkkode.nb.equals(språkkode) || Språkkode.en.equals(språkkode)
            || "NO".equals(språkkode.getKode()) || Språkkode.UDEFINERT.equals(språkkode)) {
            return BaseDokument.Lokale.BOKMÅL;
        } else {
            throw new IllegalArgumentException("Utviklerfeil - ugyldig språkkode: " + språkkode.getKode());
        }
    }

    static VarselbrevDokument mapTilVarselbrevDokument(VarselbrevSamletInfo varselbrevSamletInfo, LocalDateTime dagensDato) {
        VarselbrevDokument varselbrevDokument = new VarselbrevDokument();
        varselbrevDokument.setBelop(varselbrevSamletInfo.getSumFeilutbetaling());
        varselbrevDokument.setKontakttelefonnummer(ReturadresseKonfigurasjon.getBrevTelefonnummerKlageEnhet());
        varselbrevDokument.setEndringsdato(dagensDato.toLocalDate());
        varselbrevDokument.setFristdatoForTilbakemelding(varselbrevSamletInfo.getFristdato());
        varselbrevDokument.setVarseltekstFraSaksbehandler(varselbrevSamletInfo.getFritekstFraSaksbehandler());
        varselbrevDokument.setFeilutbetaltePerioder(varselbrevSamletInfo.getFeilutbetaltePerioder());
        varselbrevDokument.setFagsaktypeNavn(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        varselbrevDokument.setLocale(finnRiktigSpråk(varselbrevSamletInfo.getBrevMetadata().getSpråkkode()));
        settFagsaktype(varselbrevDokument, varselbrevSamletInfo.getBrevMetadata().getFagsaktype());
        settSenesteOgTidligsteDatoer(varselbrevDokument, varselbrevSamletInfo.getFeilutbetaltePerioder());

        varselbrevDokument.valider();
        return varselbrevDokument;
    }

    private static VedtaksbrevDokument mapTilVedtaksbrevDokument(VedtaksbrevSamletInfo vedtaksbrevSamletInfo) {
        VedtaksbrevDokument vedtaksbrevDokument = new VedtaksbrevDokument();
        vedtaksbrevDokument.setAntallUkerKlagefrist(vedtaksbrevSamletInfo.getAntallUkerKlagefrist());
        vedtaksbrevDokument.setFeilutbetaltBeloep(vedtaksbrevSamletInfo.getSumFeilutbetaling());
        vedtaksbrevDokument.setBeloepSomSkalTilbakekreves(vedtaksbrevSamletInfo.getSumBeløpSomSkalTilbakekreves());
        vedtaksbrevDokument.setKontakttelefonnummer(ReturadresseKonfigurasjon.getBrevTelefonnummerKlageEnhet());
        vedtaksbrevDokument.setFagsaktypeNavn(vedtaksbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        settFagsaktype(vedtaksbrevDokument, vedtaksbrevSamletInfo.getBrevMetadata().getFagsaktype());
        vedtaksbrevDokument.setLocale(finnRiktigSpråk(vedtaksbrevSamletInfo.getBrevMetadata().getSpråkkode()));
        vedtaksbrevDokument.setFeilutbetalingsperioderMedTekst(mapPerioderMedTekst(vedtaksbrevSamletInfo.getPerioderMedBrevtekst()));
        vedtaksbrevDokument.setVarselbrevSendtDato(vedtaksbrevSamletInfo.getVarselbrevSendtUt());
        vedtaksbrevDokument.setOppsummeringFritekst(vedtaksbrevSamletInfo.getOppsummeringFritekst());
        vedtaksbrevDokument.valider();
        return vedtaksbrevDokument;
    }

    private static List<FeilutbetalingsperiodeMedTekst> mapPerioderMedTekst(List<PeriodeMedBrevtekst> perioderMedBrevtekst) {
        return perioderMedBrevtekst.stream()
            .sorted(Comparator.comparing(PeriodeMedBrevtekst::getFom))
            .map(vilkårsperiode -> {
                FeilutbetalingsperiodeMedTekst feilutbetalingsperiodeMedTekst = new FeilutbetalingsperiodeMedTekst();
                Periode periode = new Periode(vilkårsperiode.getFom(), vilkårsperiode.getTom());
                feilutbetalingsperiodeMedTekst.setPeriode(periode);
                feilutbetalingsperiodeMedTekst.setGenerertFaktaAvsnitt(vilkårsperiode.getGenerertFaktaAvsnitt());
                feilutbetalingsperiodeMedTekst.setGenerertVilkaarAvsnitt(vilkårsperiode.getGenerertVilkårAvsnitt());
                feilutbetalingsperiodeMedTekst.setGenerertSaerligeGrunnerAvsnitt(vilkårsperiode.getGenerertSærligeGrunnerAvsnitt());
                feilutbetalingsperiodeMedTekst.setFritekstFaktaAvsnitt(vilkårsperiode.getFritekstFakta());
                feilutbetalingsperiodeMedTekst.setFritekstVilkaarAvsnitt(vilkårsperiode.getFritekstVilkår());
                feilutbetalingsperiodeMedTekst.setFritekstSaerligeGrunnerAvsnitt(vilkårsperiode.getFritekstSærligeGrunner());
                return feilutbetalingsperiodeMedTekst;
            }).collect(Collectors.toList());
    }
}
