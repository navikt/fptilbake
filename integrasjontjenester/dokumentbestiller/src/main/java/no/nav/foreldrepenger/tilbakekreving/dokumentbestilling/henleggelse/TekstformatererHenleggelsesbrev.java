package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevSpråkUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto.HenleggelsesbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrevFeil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Locale;

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

        handlebars.setCharset(StandardCharsets.UTF_8);
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


    private static HenleggelsesbrevDokument mapTilHenleggelsebrevDokument(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo){
        HenleggelsesbrevDokument henleggelsesbrevDokument = new HenleggelsesbrevDokument();
        henleggelsesbrevDokument.setFagsaktypeNavn(henleggelsesbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        henleggelsesbrevDokument.setVarsletDato(henleggelsesbrevSamletInfo.getVarsletDato());

        henleggelsesbrevDokument.valider();
        return henleggelsesbrevDokument;
    }



}
