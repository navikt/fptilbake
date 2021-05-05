package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;

public class TekstformatererHeader extends FellesTekstformaterer {

    private TekstformatererHeader() {
        // for static access
    }

    public static String lagHeader(BrevMetadata brevMetadata, String overskrift) {
        return lagHeader(new HeaderData(brevMetadata, overskrift));
    }

    public static String lagHeader(HeaderData data) {
        try {
            Template template = opprettHandlebarsTemplate("header", data.getSpråkkode());
            return applyTemplate(template, data);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti, Språkkode språkkode) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        return handlebars.compile(lagSpråkstøttetFilsti(filsti, språkkode));
    }

}
