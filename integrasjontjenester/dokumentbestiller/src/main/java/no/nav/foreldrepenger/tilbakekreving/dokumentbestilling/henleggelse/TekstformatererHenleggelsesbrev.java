package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.OverskriftBrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto.HenleggelsesbrevDokument;

class TekstformatererHenleggelsesbrev extends FellesTekstformaterer {

    private TekstformatererHenleggelsesbrev() {
        // for static access
    }

    static String lagHenleggelsebrevFritekst(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate(lagSpråkstøttetFilsti("henleggelse/henleggelse",
                henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode()));
            HenleggelsesbrevDokument henleggelsesbrevDokument = mapTilHenleggelsebrevDokument(
                henleggelsesbrevSamletInfo);

            return template.apply(henleggelsesbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    static String lagHenleggelsebrevOverskrift(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate(lagSpråkstøttetFilsti("henleggelse/henleggelse_overskrift",
                henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode()));
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(henleggelsesbrevSamletInfo.getBrevMetadata());
            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        handlebars.registerHelper("datoformat", datoformatHelper());
        return handlebars.compile(filsti);
    }

    private static HenleggelsesbrevDokument mapTilHenleggelsebrevDokument(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        HenleggelsesbrevDokument henleggelsesbrevDokument = new HenleggelsesbrevDokument();
        henleggelsesbrevDokument.setFagsaktypeNavn(henleggelsesbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        henleggelsesbrevDokument.setVarsletDato(henleggelsesbrevSamletInfo.getVarsletDato());

        henleggelsesbrevDokument.valider();
        return henleggelsesbrevDokument;
    }
}
