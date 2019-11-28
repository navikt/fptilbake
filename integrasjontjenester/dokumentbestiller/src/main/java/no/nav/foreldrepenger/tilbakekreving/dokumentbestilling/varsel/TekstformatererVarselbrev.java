package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.ConditionalHelpers;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto.VarselbrevDokument;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.FPDateUtil;

public class TekstformatererVarselbrev {

    private TekstformatererVarselbrev() {
        // for static access
    }

    public static String lagVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("/templates/varsel");
            VarselbrevDokument varselbrevDokument = mapTilVarselbrevDokument(
                varselbrevSamletInfo);

            return template.apply(varselbrevDokument);
        } catch (IOException e) {
            throw TekstformatererVarselbrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    public static String lagKorrigertVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo, VarselInfo varselInfo) {
        try {
            Template template = opprettHandlebarsTemplate("/templates/korrigert_varsel");
            VarselbrevDokument varselbrevDokument = mapTilKorrigertVarselbrevDokument(
                varselbrevSamletInfo,
                varselInfo);

            return template.apply(varselbrevDokument);
        } catch (IOException e) {
            throw TekstformatererVarselbrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti) throws IOException {
        Handlebars handlebars = new Handlebars();

        handlebars.setCharset(Charset.forName("latin1")); //TODO begge maler skal bruke UTF-8
        handlebars.setInfiniteLoops(false);
        handlebars.setPrettyPrint(true);
        handlebars.registerHelper("datoformat", datoformatHelper());
        handlebars.registerHelpers(ConditionalHelpers.class);
        return handlebars.compile(filsti);
    }

    private static Helper<Object> datoformatHelper() {
        return (value, options) -> konverterFraLocaldateTilTekst((LocalDate) value);
    }

     static String konverterFraLocaldateTilTekst(LocalDate dato) {
        DateFormat dateInstance = DateFormat.getDateInstance(1, new Locale("no"));
        return dateInstance.format(Date.valueOf(dato));
    }


    private static void settFagsaktype(BaseDokument baseDokument, FagsakYtelseType fagsaktype) {
        if (FagsakYtelseType.ENGANGSTØNAD.equals(fagsaktype)) {
            baseDokument.setEngangsstonad(true);
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

    static BaseDokument.Lokale finnRiktigSpråk(Språkkode språkkode) {
        if (Språkkode.nn.equals(språkkode)) {
            return BaseDokument.Lokale.NYNORSK;
        } else if (Språkkode.nb.equals(språkkode) || Språkkode.en.equals(språkkode)
            || "NO".equals(språkkode.getKode()) || Språkkode.UDEFINERT.equals(språkkode)) {
            return BaseDokument.Lokale.BOKMÅL;
        } else {
            throw new IllegalArgumentException("Utviklerfeil - ugyldig språkkode: " + språkkode.getKode());
        }
    }

    static VarselbrevDokument mapTilVarselbrevDokument(VarselbrevSamletInfo varselbrevSamletInfo) {
        VarselbrevDokument varselbrevDokument = new VarselbrevDokument();
        varselbrevDokument.setBelop(varselbrevSamletInfo.getSumFeilutbetaling());
        varselbrevDokument.setEndringsdato(varselbrevSamletInfo.getRevurderingVedtakDato()!=null ? varselbrevSamletInfo.getRevurderingVedtakDato(): FPDateUtil.iDag());
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
        varselbrevDokument.setVarsletBelop(varselInfo.getVarselBeløp());

        varselbrevDokument.valider();
        return varselbrevDokument;
    }

}
