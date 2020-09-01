package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.OverskriftBrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon.handlebars.dto.InnhentDokumentasjonbrevDokument;

class TekstformatererInnhentDokumentasjonbrev extends FellesTekstformaterer {

    private TekstformatererInnhentDokumentasjonbrev() {
    }

    public static String lagInnhentDokumentasjonBrevFritekst(InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("innhentdokumentasjon/innhent_dokumentasjon",
                innhentDokumentasjonBrevSamletInfo.getBrevMetadata().getSpråkkode());
            InnhentDokumentasjonbrevDokument innhentDokumentasjonBrevDokument = mapTilInnhentDokumentasjonBrevDokument(
                innhentDokumentasjonBrevSamletInfo);

            return applyTemplate(template, innhentDokumentasjonBrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    public static String lagInnhentDokumentasjonBrevOverskrift(InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("innhentdokumentasjon/innhent_dokumentasjon_overskrift",
                innhentDokumentasjonBrevSamletInfo.getBrevMetadata().getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(innhentDokumentasjonBrevSamletInfo.getBrevMetadata());

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti, Språkkode språkkode) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        return handlebars.compile(lagSpråkstøttetFilsti(filsti, språkkode));
    }

    private static InnhentDokumentasjonbrevDokument mapTilInnhentDokumentasjonBrevDokument(InnhentDokumentasjonbrevSamletInfo innhentDokumentasjonBrevSamletInfo) {
        InnhentDokumentasjonbrevDokument innhentDokumentasjonBrevDokument = new InnhentDokumentasjonbrevDokument();
        innhentDokumentasjonBrevDokument.setFagsaktypeNavn(innhentDokumentasjonBrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        innhentDokumentasjonBrevDokument.setYtelsetype(innhentDokumentasjonBrevSamletInfo.getBrevMetadata().getFagsaktype());
        innhentDokumentasjonBrevDokument.setFritekstFraSaksbehandler(innhentDokumentasjonBrevSamletInfo.getFritekstFraSaksbehandler());
        innhentDokumentasjonBrevDokument.setFristDato(innhentDokumentasjonBrevSamletInfo.getFristDato());
        innhentDokumentasjonBrevDokument.setFinnesVerge(innhentDokumentasjonBrevSamletInfo.getBrevMetadata().isFinnesVerge());
        innhentDokumentasjonBrevDokument.setAnnenMottakerNavn(BrevMottakerUtil.getAnnenMottakerNavn(innhentDokumentasjonBrevSamletInfo.getBrevMetadata()));

        innhentDokumentasjonBrevDokument.valider();
        return innhentDokumentasjonBrevDokument;
    }

}
