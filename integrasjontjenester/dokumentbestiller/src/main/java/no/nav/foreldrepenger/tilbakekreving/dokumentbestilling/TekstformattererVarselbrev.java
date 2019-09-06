package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.ConditionalHelpers;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.ReturadresseKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.VarselbrevSamletInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util.BrevUtil;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;
import no.nav.vedtak.util.FPDateUtil;

class TekstformattererVarselbrev {

    private TekstformattererVarselbrev() {
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

    private static Template opprettHandlebarsTemplate(String filsti) throws IOException {
        Handlebars handlebars = new Handlebars();

        handlebars.setCharset(Charset.forName("latin1")); //TODO begge maler skal bruke UTF-8
        handlebars.setInfiniteLoops(false);
        handlebars.setPrettyPrint(true);
        handlebars.registerHelper("datoformat", datoformatHelper());
        handlebars.registerHelpers(ConditionalHelpers.class);
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

}
