package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottakerUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.BaseDokument;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.OverskriftBrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.periode.HbPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto.VarselbrevDokument;

public class TekstformatererVarselbrev extends FellesTekstformaterer {
    private static Set<FagsakYtelseType> støttetTyper = Set.of(
        FagsakYtelseType.FORELDREPENGER,
        FagsakYtelseType.ENGANGSTØNAD,
        FagsakYtelseType.SVANGERSKAPSPENGER,
        FagsakYtelseType.OMSORGSPENGER,
        FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
        FagsakYtelseType.FRISINN);

    private TekstformatererVarselbrev() {
        // for static access
    }

    public static String lagVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo) {
        try {
            Template template = opprettHandlebarsTemplate("varsel/varsel", varselbrevSamletInfo.getBrevMetadata().getSpråkkode());
            VarselbrevDokument varselbrevDokument = mapTilVarselbrevDokument(
                varselbrevSamletInfo);

            return applyTemplate(template, varselbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    public static String lagVarselbrevOverskrift(BrevMetadata brevMetadata) {
        try {
            Template template = opprettHandlebarsTemplate("varsel/varsel_overskrift", brevMetadata.getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(brevMetadata);

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    public static String lagKorrigertVarselbrevFritekst(VarselbrevSamletInfo varselbrevSamletInfo, VarselInfo varselInfo) {
        try {
            Template template = opprettHandlebarsTemplate("varsel/korrigert_varsel", varselbrevSamletInfo.getBrevMetadata().getSpråkkode());
            VarselbrevDokument varselbrevDokument = mapTilKorrigertVarselbrevDokument(
                varselbrevSamletInfo,
                varselInfo);

            return applyTemplate(template, varselbrevDokument);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    public static String lagKorrigertVarselbrevOverskrift(BrevMetadata brevMetadata) {
        try {
            Template template = opprettHandlebarsTemplate("varsel/korrigert_varsel_overskrift", brevMetadata.getSpråkkode());
            OverskriftBrevData overskriftBrevData = lagOverskriftBrevData(brevMetadata);
            overskriftBrevData.setEngangsstønad(FagsakYtelseType.ENGANGSTØNAD.equals(brevMetadata.getFagsaktype()));

            return template.apply(overskriftBrevData);
        } catch (IOException e) {
            throw TekstformatererBrevFeil.feilVedTekstgenerering(e);
        }
    }

    private static Template opprettHandlebarsTemplate(String filsti, Språkkode språkkode) throws IOException {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        return handlebars.compile(lagSpråkstøttetFilsti(filsti, språkkode));
    }

    private static void settFagsaktype(BaseDokument baseDokument, FagsakYtelseType fagsaktype) {
        if (!støttetTyper.contains(fagsaktype)) {
            throw new IllegalArgumentException("Utviklerfeil - ikkje støttet FagsakYtelseType: " + fagsaktype);
        }
        baseDokument.setYtelsetype(fagsaktype);
    }

    private static void settSenesteOgTidligsteDatoer(VarselbrevDokument varselbrevDokument, List<HbPeriode> feilutbetaltPerioder) {
        if (feilutbetaltPerioder != null && feilutbetaltPerioder.size() == 1) {
            LocalDate fom = feilutbetaltPerioder.get(0).getFom();
            LocalDate tom = feilutbetaltPerioder.get(0).getTom();
            varselbrevDokument.setDatoerHvisSammenhengendePeriode(HbPeriode.of(fom, tom));
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
        varselbrevDokument.setAnnenMottakerNavn(BrevMottakerUtil.getAnnenMottakerNavn(varselbrevSamletInfo.getBrevMetadata()));
        varselbrevDokument.setFinnesVerge(varselbrevSamletInfo.getBrevMetadata().isFinnesVerge());
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
