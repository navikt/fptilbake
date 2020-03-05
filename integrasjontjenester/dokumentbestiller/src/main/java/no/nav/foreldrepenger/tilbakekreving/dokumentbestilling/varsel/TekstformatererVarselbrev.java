package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevSpråkUtil.finnRiktigSpråk;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.CustomHelpers;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.OverskriftBrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TekstformatererVarselbrev extends FellesTekstformaterer {

    private TekstformatererVarselbrev() {
        // for static access
    }

    public static String lagVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("varsel");
            VarselbrevDokument varselbrevDokument = mapTilVarselbrevDokument(
                varselbrevSamletInfo);

            return template.apply(varselbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    public static String lagVarselbrevOverskrift(BrevMetadata brevMetadata) {
        try {
            Template template = opprettHandlebarsTemplate("varsel/varsel_overskrift");
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(brevMetadata);

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    public static String lagKorrigertVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo, VarselInfo varselInfo) {
        try {
            Template template = opprettHandlebarsTemplate("korrigert_varsel");
            VarselbrevDokument varselbrevDokument = mapTilKorrigertVarselbrevDokument(
                varselbrevSamletInfo,
                varselInfo);

            return template.apply(varselbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    public static String lagKorrigertVarselbrevOverskrift(BrevMetadata brevMetadata) {
        try {
            Template template = opprettHandlebarsTemplate("varsel/korrigert_varsel_overskrift");
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(brevMetadata);
            overskriftBrevData.setEngangsstønad(FagsakYtelseType.ENGANGSTØNAD.equals(brevMetadata.getFagsaktype()));

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        handlebars.registerHelper("datoformat", datoformatHelper());
        handlebars.registerHelper("kroner", new CustomHelpers.KroneFormattererMedTusenskille());
        return handlebars.compile(filsti);
    }

    private static void settFagsaktype(BaseDokument baseDokument, FagsakYtelseType fagsaktype) {
        if (FagsakYtelseType.ENGANGSTØNAD.equals(fagsaktype)) {
            baseDokument.setEngangsstønad(true);
        } else if (FagsakYtelseType.FORELDREPENGER.equals(fagsaktype)) {
            baseDokument.setForeldrepenger(true);
        } else if (FagsakYtelseType.SVANGERSKAPSPENGER.equals(fagsaktype)) {
            baseDokument.setSvangerskapspenger(true);
        } else {
            throw new IllegalArgumentException("Utviklerfeil - Kunne ikke finne fagsaktype: " + fagsaktype);
        }
    }

    private static void settSenesteOgTidligsteDatoer(VarselbrevDokument varselbrevDokument, List<Periode> feilutbetaltPerioder) {
        if (feilutbetaltPerioder != null && feilutbetaltPerioder.size() == 1) {
            LocalDate fom = feilutbetaltPerioder.get(0).getFom();
            LocalDate tom = feilutbetaltPerioder.get(0).getTom();
            varselbrevDokument.setDatoerHvisSammenhengendePeriode(new Periode(fom, tom));
        }
    }

    static VarselbrevDokument mapTilVarselbrevDokument(VarselbrevSamletInfo varselbrevSamletInfo) {
        VarselbrevDokument varselbrevDokument = new VarselbrevDokument();
        varselbrevDokument.setBeløp(varselbrevSamletInfo.getSumFeilutbetaling());
        varselbrevDokument.setEndringsdato(varselbrevSamletInfo.getRevurderingVedtakDato() != null ? varselbrevSamletInfo.getRevurderingVedtakDato() : LocalDate.now());
        varselbrevDokument.setFristdatoForTilbakemelding(varselbrevSamletInfo.getFristdato());
        varselbrevDokument.setVarseltekstFraSaksbehandler(varselbrevSamletInfo.getFritekstFraSaksbehandler());
        varselbrevDokument.setFeilutbetaltePerioder(varselbrevSamletInfo.getFeilutbetaltePerioder());
        varselbrevDokument.setFagsaktypeNavn(varselbrevSamletInfo.getBrevMetadata().getFagsaktypenavnPåSpråk());
        varselbrevDokument.setLocale(finnRiktigSpråk(varselbrevSamletInfo.getBrevMetadata().getSpråkkode()));
        settFagsaktype(varselbrevDokument, varselbrevSamletInfo.getBrevMetadata().getFagsaktype());
        settSenesteOgTidligsteDatoer(varselbrevDokument, varselbrevSamletInfo.getFeilutbetaltePerioder());

        varselbrevDokument.valider();
        return varselbrevDokument;
    }

    static VarselbrevDokument mapTilKorrigertVarselbrevDokument(VarselbrevSamletInfo varselbrevSamletInfo, VarselInfo varselInfo) {
        VarselbrevDokument varselbrevDokument = mapTilVarselbrevDokument(varselbrevSamletInfo);
        varselbrevDokument.setKorrigert(true);
        varselbrevDokument.setVarsletDato(varselInfo.getOpprettetTidspunkt().toLocalDate());
        varselbrevDokument.setVarsletBeløp(varselInfo.getVarselBeløp());

        varselbrevDokument.valider();
        return varselbrevDokument;
    }
}
