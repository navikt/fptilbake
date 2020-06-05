package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
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
            Template template = opprettHandlebarsTemplate("henleggelse/henleggelse",
                henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode());
            HenleggelsesbrevDokument henleggelsesbrevDokument = mapTilHenleggelsebrevDokument(
                henleggelsesbrevSamletInfo);

            return template.apply(henleggelsesbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    static String lagHenleggelsebrevOverskrift(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("henleggelse/henleggelse_overskrift",
                henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(henleggelsesbrevSamletInfo.getBrevMetadata());

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti, Språkkode språkkode) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        handlebars.registerHelper("datoformat", datoformatHelper());
        return handlebars.compile(lagSpråkstøttetFilsti(filsti, språkkode));
    }

    private static HenleggelsesbrevDokument mapTilHenleggelsebrevDokument(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        HenleggelsesbrevDokument henleggelsesbrevDokument = new HenleggelsesbrevDokument();
        henleggelsesbrevDokument.setFagsaktypeNavn(henleggelsesbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        henleggelsesbrevDokument.setVarsletDato(henleggelsesbrevSamletInfo.getVarsletDato());
        henleggelsesbrevDokument.setFinnesVerge(henleggelsesbrevSamletInfo.isFinnesVerge());
        henleggelsesbrevDokument.setAnnenMottakerNavn(BrevMottakerUtil.getAnnenMottakerNavn(henleggelsesbrevSamletInfo.getBrevMetadata()));

        henleggelsesbrevDokument.valider();
        return henleggelsesbrevDokument;
    }
}
