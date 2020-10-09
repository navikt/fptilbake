package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.CustomHelpers;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.OverskriftBrevData;

class TekstFormatererFritekstVedtaksbrev extends FellesTekstformaterer {
    private static Map<String, Template> TEMPLATE_CACHE = new HashMap<>();

    TekstFormatererFritekstVedtaksbrev(){
        //for static access
    }

    public static String lagFritekstVedtaksbrevOverskrift(BrevMetadata brevMetadata) {
        try {
            Template template = opprettHandlebarsTemplate("vedatk/fritekst/fritekst_overskrift", brevMetadata.getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(brevMetadata);

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }


    private static Template opprettHandlebarsTemplate(String filsti, Språkkode språkkode) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        handlebars.registerHelper("switch", new CustomHelpers.SwitchHelper());
        handlebars.registerHelper("case", new CustomHelpers.CaseHelper());
        handlebars.registerHelper("lookup-map", new CustomHelpers.MapLookupHelper());
        handlebars.registerHelper("kroner", new CustomHelpers.KroneFormattererMedTusenskille());
        return handlebars.compile(lagSpråkstøttetFilsti(filsti, språkkode));
    }


}
