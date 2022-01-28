package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.OverskriftBrevData;

public abstract class FellesTekstformaterer {

    private static final DateTimeFormatter FORMATTER_LANGT_DATOFORMAT = DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("no"));

    protected static final ObjectMapper OM = ObjectMapperForUtvekslingAvDataMedHandlebars.INSTANCE;

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
        handlebars.registerHelper("switch", new CustomHelpers.SwitchHelper());
        handlebars.registerHelper("case", new CustomHelpers.CaseHelper());
        handlebars.registerHelper("var", new CustomHelpers.VariableHelper());
        handlebars.registerHelper("lookup-map", new CustomHelpers.MapLookupHelper());
        handlebars.registerHelper("kroner", new CustomHelpers.KroneFormattererMedTusenskille());
        handlebars.registerHelper("formater-periode", new CustomHelpers.PeriodeFormatterer());
        handlebars.registerHelper("formater-perioder", new CustomHelpers.PerioderFormatterer());
        return handlebars;
    }

    protected static String applyTemplate(Template template, BaseDokument data) {
        try {
            //Går via JSON for å
            //1. tilrettelegger for å flytte generering til PDF etc til ekstern applikasjon
            //2. ha egen navngiving på variablene i template for enklere å lese template
            //3. unngår at template feiler når variable endrer navn
            JsonNode jsonNode = OM.valueToTree(data);
            Context context = Context.newBuilder(jsonNode)
                    .resolver(JsonNodeValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE, MapValueResolver.INSTANCE)
                    .build();
            return template.apply(context).stripLeading().stripTrailing();
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    protected static OverskriftBrevData lagOverskriftBrevData(BrevMetadata brevMetadata) {
        OverskriftBrevData overskriftBrevData = new OverskriftBrevData();
        overskriftBrevData.setFagsakType(brevMetadata.getFagsaktypenavnPåSpråk());
        return overskriftBrevData;
    }

    protected static Helper<Object> datoformatHelper() {
        return (value, options) -> konverterFraLocaldateTilTekst((LocalDate) value);
    }

    private static String konverterFraLocaldateTilTekst(LocalDate dato) {
        return FORMATTER_LANGT_DATOFORMAT.format(dato);
    }

    protected static String lagSpråkstøttetFilsti(String filsti, Språkkode språkkode) {
        String språk = mapTilSpråk(språkkode);
        return String.format("%s/%s", språk, filsti);
    }

    private static String mapTilSpråk(Språkkode språkkode) {
        if (Språkkode.nn.equals(språkkode)) {
            return "nn";
        } else {
            return "nb";
        }
    }
}
