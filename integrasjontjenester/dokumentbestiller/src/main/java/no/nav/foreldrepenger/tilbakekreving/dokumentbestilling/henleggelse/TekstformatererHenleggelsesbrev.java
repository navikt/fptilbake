package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.OverskriftBrevData;
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

            return applyTemplate(template, henleggelsesbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    static String lagHenleggelsebrevOverskrift(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("henleggelse/henleggelse_overskrift",
                    henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(henleggelsesbrevSamletInfo.getBrevMetadata());

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    static String lagRevurderingHenleggelsebrevFritekst(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("henleggelse/henleggelse_revurdering",
                    henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode());
            HenleggelsesbrevDokument henleggelsesbrevDokument = mapTilHenleggelsebrevDokument(
                    henleggelsesbrevSamletInfo);

            return applyTemplate(template, henleggelsesbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    static String lagRevurderingHenleggelsebrevOverskrift(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("henleggelse/henleggelse_revurdering_overskrift",
                    henleggelsesbrevSamletInfo.getBrevMetadata().getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(henleggelsesbrevSamletInfo.getBrevMetadata());

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti, Språkkode språkkode) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        return handlebars.compile(lagSpråkstøttetFilsti(filsti, språkkode));
    }

    private static HenleggelsesbrevDokument mapTilHenleggelsebrevDokument(HenleggelsesbrevSamletInfo henleggelsesbrevSamletInfo) {
        HenleggelsesbrevDokument henleggelsesbrevDokument = new HenleggelsesbrevDokument();
        henleggelsesbrevDokument.setFagsaktypeNavn(henleggelsesbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        henleggelsesbrevDokument.setYtelsetype(henleggelsesbrevSamletInfo.getBrevMetadata().getFagsaktype());
        henleggelsesbrevDokument.setVarsletDato(henleggelsesbrevSamletInfo.getVarsletDato());
        henleggelsesbrevDokument.setFinnesVerge(henleggelsesbrevSamletInfo.getBrevMetadata().isFinnesVerge());
        henleggelsesbrevDokument.setAnnenMottakerNavn(BrevMottakerUtil.getAnnenMottakerNavn(henleggelsesbrevSamletInfo.getBrevMetadata()));
        henleggelsesbrevDokument.setTilbakekrevingRevurdering(BehandlingType.REVURDERING_TILBAKEKREVING
                .equals(henleggelsesbrevSamletInfo.getBrevMetadata().getBehandlingType()));
        henleggelsesbrevDokument.setFritekstFraSaksbehandler(henleggelsesbrevSamletInfo.getFritekstFraSaksbehandler());
        henleggelsesbrevDokument.valider();
        return henleggelsesbrevDokument;
    }
}
