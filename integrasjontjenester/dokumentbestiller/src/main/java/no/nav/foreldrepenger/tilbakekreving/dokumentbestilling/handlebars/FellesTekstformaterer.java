package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevSpråkUtil.finnRiktigSpråk;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public abstract class FellesTekstformaterer {

    private static final DateTimeFormatter FORMATTER_LANGT_DATOFORMAT = DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("no"));

    protected static Handlebars opprettHandlebarsKonfigurasjon() {
        ClassPathTemplateLoader loader = new ClassPathTemplateLoader();
        loader.setCharset(StandardCharsets.UTF_8);
        loader.setPrefix("/templates/");
        loader.setSuffix(".hbs");
        
        Handlebars handlebars = new Handlebars(loader);
        handlebars.setCharset(StandardCharsets.UTF_8);
        handlebars.setInfiniteLoops(false);
        handlebars.setPrettyPrint(true);
        handlebars.registerHelpers(ConditionalHelpers.class);

        return handlebars;
    }

    protected static OverskriftBrevData lagOverskriftBrevData(BrevMetadata brevMetadata) {
        OverskriftBrevData overskriftBrevData = new OverskriftBrevData();
        overskriftBrevData.setFagsakType(brevMetadata.getFagsaktypenavnPåSpråk());
        overskriftBrevData.setLokale(finnRiktigSpråk(brevMetadata.getSpråkkode()));
        return overskriftBrevData;
    }

    protected static Helper<Object> datoformatHelper() {
        return (value, options) -> konverterFraLocaldateTilTekst((LocalDate) value);
    }

    private static String konverterFraLocaldateTilTekst(LocalDate dato) {
        return FORMATTER_LANGT_DATOFORMAT.format(dato);
    }
}
