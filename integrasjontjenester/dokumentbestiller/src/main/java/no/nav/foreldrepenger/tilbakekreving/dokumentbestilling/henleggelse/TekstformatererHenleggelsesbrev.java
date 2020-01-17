package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Locale;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.ConditionalHelpers;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto.HenleggelsesbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrevFeil;

public class TekstformatererHenleggelsesbrev {

    private TekstformatererHenleggelsesbrev() {
        // for static access
    }

    public static String lagHenleggelsebrevFritekst(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("/templates/henleggelse");
            HenleggelsesbrevDokument henleggelsesbrevDokument = mapTilHenleggelsebrevDokument(
                henleggelsesbrevSamletInfo);

            return template.apply(henleggelsesbrevDokument);
        } catch (IOException e) {
            throw TekstformatererVarselbrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
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
        return (value, options) -> konverterFraLocaldateTilTekst((LocalDate) value);
    }

     private static String konverterFraLocaldateTilTekst(LocalDate dato) {
        DateFormat dateInstance = DateFormat.getDateInstance(1, new Locale("no"));
        return dateInstance.format(Date.valueOf(dato));
    }


    private static BaseDokument.Lokale finnRiktigSpråk(Språkkode språkkode) {
        if (Språkkode.nn.equals(språkkode)) {
            return BaseDokument.Lokale.NYNORSK;
        } else if (Språkkode.nb.equals(språkkode) || Språkkode.en.equals(språkkode)
            || "NO".equals(språkkode.getKode()) || Språkkode.UDEFINERT.equals(språkkode)) {
            return BaseDokument.Lokale.BOKMÅL;
        } else {
            throw new IllegalArgumentException("Utviklerfeil - ugyldig språkkode: " + språkkode.getKode());
        }
    }

    private static HenleggelsesbrevDokument mapTilHenleggelsebrevDokument(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo){
        HenleggelsesbrevDokument henleggelsesbrevDokument = new HenleggelsesbrevDokument();
        henleggelsesbrevDokument.setFagsaktypeNavn(henleggelsesbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        henleggelsesbrevDokument.setAvsenderEnhetNavn(henleggelsesbrevSamletInfo.getBrevMetadata().getBehandlendeEnhetNavn());
        henleggelsesbrevDokument.setVarsletDato(henleggelsesbrevSamletInfo.getVarsletDato());
        henleggelsesbrevDokument.setLocale(finnRiktigSpråk(henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode()));

        henleggelsesbrevDokument.valider();
        return henleggelsesbrevDokument;
    }



}
