package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.TekstformatererBrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.CustomHelpers;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.FellesTekstformaterer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.util.Objects;
import no.nav.vedtak.util.StringUtils;

class TekstformatererVedtaksbrev extends FellesTekstformaterer {
    private static Map<String, Template> TEMPLATE_CACHE = new HashMap<>();

    private static final String PARTIAL_PERIODE_FAKTA = "vedtak/periode_fakta";
    private static final String PARTIAL_PERIODE_FORELDELSE = "vedtak/periode_foreldelse";
    private static final String PARTIAL_PERIODE_VILKÅR = "vedtak/periode_vilkår";
    private static final String PARTIAL_PERIODE_SÆRLIGE_GRUNNER = "vedtak/periode_særlige_grunner";

    private TekstformatererVedtaksbrev() {
        // for static access
    }

    static List<Avsnitt> lagVedtaksbrevDeltIAvsnitt(HbVedtaksbrevData vedtaksbrevData, String hovedoverskrift) {
        List<Avsnitt> resultat = new ArrayList<>();
        VedtaksbrevFritekst.settInnMarkeringForFritekst(vedtaksbrevData);
        resultat.add(lagOppsummeringAvsnitt(vedtaksbrevData, hovedoverskrift));
        if (vedtaksbrevData.getFelles().getVedtaksbrevType() == VedtaksbrevType.ORDINÆR) {
            resultat.addAll(lagPerioderAvsnitt(vedtaksbrevData));
        }
        resultat.add(lagAvsluttendeAvsnitt(vedtaksbrevData));
        return resultat;
    }

    static Avsnitt lagOppsummeringAvsnitt(HbVedtaksbrevData vedtaksbrevData, String hovedoverskrift) {
        String tekst = lagVedtakStart(vedtaksbrevData.getFelles());
        Avsnitt.Builder avsnittBuilder = new Avsnitt.Builder().medAvsnittstype(Avsnitt.Avsnittstype.OPPSUMMERING).medOverskrift(hovedoverskrift);
        return parseTekst(tekst, avsnittBuilder, null).build();
    }

    public static String lagVedtakStart(HbVedtaksbrevFelles vedtaksbrevFelles) {
        switch (vedtaksbrevFelles.getVedtaksbrevType()) {
            case FRITEKST_FEILUTBETALING_BORTFALT:
                return konverterMedPartialTemplate("vedtak/fritekstFeilutbetalingBortfalt/fritekstFeilutbetalingBortfalt_start", vedtaksbrevFelles);
            case ORDINÆR:
                return konverterMedPartialTemplate("vedtak/vedtak_start", vedtaksbrevFelles);
            default:
                throw new IllegalArgumentException("Utviklerfeil: ustøttet VedtaksbrevType(" + vedtaksbrevFelles.getVedtaksbrevType() + ") i VedtaksbrevFormatterer");
        }
    }

    static List<Avsnitt> lagPerioderAvsnitt(HbVedtaksbrevData vedtaksbrevData) {
        var resultat = new ArrayList<Avsnitt>();
        for (HbVedtaksbrevPeriode periode : vedtaksbrevData.getPerioder()) {
            resultat.add(lagPeriodeAvsnitt(new HbVedtaksbrevPeriodeOgFelles(vedtaksbrevData.getFelles(), periode)));
        }
        return resultat;
    }

    static Avsnitt lagAvsluttendeAvsnitt(HbVedtaksbrevData vedtaksbrevData) {
        String tekst = konverterMedPartialTemplate("vedtak/vedtak_slutt", vedtaksbrevData);
        Avsnitt.Builder avsnittBuilder = new Avsnitt.Builder().medAvsnittstype(Avsnitt.Avsnittstype.TILLEGGSINFORMASJON);
        return parseTekst(tekst, avsnittBuilder, null).build();
    }

    private static Avsnitt lagPeriodeAvsnitt(HbVedtaksbrevPeriodeOgFelles data) {
        String overskrift = konverterMedPartialTemplate("vedtak/periode_overskrift", data);
        String faktatekst = konverterMedPartialTemplate(PARTIAL_PERIODE_FAKTA, data);
        String foreldelseTekst = konverterMedPartialTemplate(PARTIAL_PERIODE_FORELDELSE, data);
        String vilkårTekst = konverterMedPartialTemplate(PARTIAL_PERIODE_VILKÅR, data);
        String særligeGrunnerTekst = konverterMedPartialTemplate(PARTIAL_PERIODE_SÆRLIGE_GRUNNER, data);
        String avsluttendeTekst = konverterMedPartialTemplate("vedtak/periode_slutt", data);

        Avsnitt.Builder avsnittBuilder = new Avsnitt.Builder()
            .medAvsnittstype(Avsnitt.Avsnittstype.PERIODE)
            .medPeriode(data.getPeriode().getPeriode());
        if (!StringUtils.nullOrEmpty(overskrift)) {
            avsnittBuilder.medOverskrift(fjernOverskriftFormattering(overskrift));
        }

        parseTekst(faktatekst, avsnittBuilder, Underavsnitt.Underavsnittstype.FAKTA);
        parseTekst(foreldelseTekst, avsnittBuilder, Underavsnitt.Underavsnittstype.FORELDELSE);
        parseTekst(vilkårTekst, avsnittBuilder, Underavsnitt.Underavsnittstype.VILKÅR);
        parseTekst(særligeGrunnerTekst, avsnittBuilder, Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER);
        parseTekst(avsluttendeTekst, avsnittBuilder, null);

        return avsnittBuilder.build();
    }

    static Avsnitt.Builder parseTekst(String generertTekst, Avsnitt.Builder avsnittBuilder, Underavsnitt.Underavsnittstype underavsnittstype) {
        List<TekstElement> elementer = parse(generertTekst, underavsnittstype);
        if (!avsnittBuilder.harOverskrift() && !elementer.isEmpty() && elementer.get(0).getTekstType() == TekstType.OVERSKRIFT) {
            TekstElement element = elementer.remove(0);
            avsnittBuilder.medOverskrift(element.getTekst());
        }

        String overskrift = null;
        String brødtekst = null;
        boolean kanHaFritekst = false;
        boolean måHaFritekst = false;
        String fritekst = null;
        for (TekstElement element : elementer) {
            underavsnittstype = element.getUnderavsnittstype();
            TekstType type = element.getTekstType();
            if (kanHaFritekst || (overskrift != null || brødtekst != null) && type == TekstType.OVERSKRIFT || brødtekst != null && type == TekstType.BRØDTEKST) {
                avsnittBuilder.leggTilUnderavsnitt(lagUnderavsnitt(underavsnittstype, overskrift, brødtekst, kanHaFritekst, måHaFritekst, fritekst));
                overskrift = null;
                brødtekst = null;
                kanHaFritekst = false;
                måHaFritekst = false;
                fritekst = null;
            }
            if (type == TekstType.OVERSKRIFT) {
                overskrift = element.getTekst();
            } else if (type == TekstType.BRØDTEKST) {
                brødtekst = element.getTekst();
            } else if (type == TekstType.FRITEKST) {
                kanHaFritekst = true;
                måHaFritekst = false;
                fritekst = element.getTekst();
            } else if (type == TekstType.PÅKREVET_FRITEKST) {
                kanHaFritekst = true;
                måHaFritekst = true;
                fritekst = element.getTekst();
            }
        }
        if (kanHaFritekst || overskrift != null || brødtekst != null) {
            avsnittBuilder.leggTilUnderavsnitt(lagUnderavsnitt(underavsnittstype, overskrift, brødtekst, kanHaFritekst, måHaFritekst, fritekst));
        }
        return avsnittBuilder;
    }

    private static Underavsnitt lagUnderavsnitt(Underavsnitt.Underavsnittstype underavsnittstype, String overskrift, String brødtekst, boolean kanHaFritekst, boolean måHaFritekst, String fritekst) {
        return new Underavsnitt.Builder().
            medUnderavsnittstype(underavsnittstype)
            .medOverskrift(overskrift)
            .medBrødtekst(brødtekst)
            .medErFritekstTillatt(kanHaFritekst)
            .medErFritekstPåkrevet(måHaFritekst)
            .medFritekst(fritekst)
            .build();
    }

    static List<TekstElement> parse(String generertTekst, Underavsnitt.Underavsnittstype underavsnittstype) {
        var resultat = new ArrayList<TekstElement>();
        String[] splittet = generertTekst.split("\r?\n");
        boolean leserFritekst = false;
        Boolean fritekstPåkrevet = null;
        List<String> fritekstLinjer = null;
        for (String linje : splittet) {
            if (VedtaksbrevFritekst.erFritekstStart(linje)) {
                Objects.check(!leserFritekst, "Feil med vedtaksbrev, har markering for 2 fritekst-start etter hverandre");
                fritekstPåkrevet = VedtaksbrevFritekst.erFritekstPåkrevetStart(linje);
                underavsnittstype = parseUnderavsnittstype(linje);
                leserFritekst = true;
                fritekstLinjer = new ArrayList<>();
            } else if (VedtaksbrevFritekst.erFritekstSlutt(linje)) {
                Objects.check(leserFritekst, "Feil med vedtaksbrev, fikk markering for fritekst-slutt før fritekst-start");
                TekstType tekstType = fritekstPåkrevet ? TekstType.PÅKREVET_FRITEKST : TekstType.FRITEKST;
                resultat.add(new TekstElement(tekstType, fritekstLinjer.isEmpty() ? null : String.join("\n", fritekstLinjer), underavsnittstype));
                leserFritekst = false;
                fritekstLinjer = null;
                fritekstPåkrevet = null;
            } else if (leserFritekst) {
                fritekstLinjer.add(linje);
            } else if (erOverskrift(linje)) {
                resultat.add(new TekstElement(TekstType.OVERSKRIFT, fjernOverskriftFormattering(linje), underavsnittstype));
            } else if (!linje.isBlank()) {
                resultat.add(new TekstElement(TekstType.BRØDTEKST, linje, underavsnittstype));
            }
        }
        return resultat;
    }

    enum TekstType {
        OVERSKRIFT,
        BRØDTEKST,
        FRITEKST,
        PÅKREVET_FRITEKST,
    }

    static class TekstElement {
        private TekstType tekstType;
        private Underavsnitt.Underavsnittstype underavsnittstype;
        private String tekst;

        public TekstElement(TekstType tekstType, String tekst, Underavsnitt.Underavsnittstype underavsnittstype) {
            this.tekstType = tekstType;
            this.underavsnittstype = underavsnittstype;
            this.tekst = tekst;
        }

        public TekstType getTekstType() {
            return tekstType;
        }

        public String getTekst() {
            return tekst;
        }

        public Underavsnitt.Underavsnittstype getUnderavsnittstype() {
            return underavsnittstype;
        }
    }

    private static Underavsnitt.Underavsnittstype parseUnderavsnittstype(String tekst) {
        String rest = VedtaksbrevFritekst.fjernFritekstmarkering(tekst);
        for (Underavsnitt.Underavsnittstype underavsnittstype : Underavsnitt.Underavsnittstype.values()) {
            if (underavsnittstype.name().equals(rest)) {
                return underavsnittstype;
            }
        }
        return null;
    }

    private static boolean erOverskrift(String tekst) {
        return tekst.startsWith("_");
    }

    private static String fjernOverskriftFormattering(String tekst) {
        return tekst.substring(1);
    }

    static String lagVedtaksbrevFritekst(HbVedtaksbrevData vedtaksbrevData) {
        switch (vedtaksbrevData.getFelles().getVedtaksbrevType()) {
            case FRITEKST_FEILUTBETALING_BORTFALT:
                return lagVedtaksbrev("vedtak/fritekstFeilutbetalingBortfalt/fritekstFeilutbetalingBortfalt", vedtaksbrevData);
            case ORDINÆR:
                return lagVedtaksbrev("vedtak/vedtak", vedtaksbrevData);
            default:
                throw new IllegalArgumentException("Utviklerfeil: ustøttet VedtaksbrevType(" + vedtaksbrevData.getFelles().getVedtaksbrevType() + ") i VedtaksbrevFormatterer");
        }
    }

    private static String lagVedtaksbrev(String mal, HbVedtaksbrevData vedtaksbrevData) {
        Template template = getTemplate(mal, vedtaksbrevData.getSpråkkode());
        return applyTemplate(template, vedtaksbrevData);
    }

    static String lagVedtaksbrevVedleggHtml(HbVedtaksbrevData vedtaksbrevData) {
        Template template = getTemplate("vedtak/vedlegg", vedtaksbrevData.getSpråkkode());
        return applyTemplate(template, vedtaksbrevData);
    }

    static String lagVedtaksbrevOverskrift(HbVedtaksbrevData vedtaksbrevData, Språkkode språkkode) {
        Template template = getTemplate("vedtak/vedtak_overskrift", språkkode);
        return applyTemplate(template, vedtaksbrevData);
    }

    static String lagFaktaTekst(HbVedtaksbrevPeriodeOgFelles periode) {
        return konverterMedPartialTemplate(PARTIAL_PERIODE_FAKTA, periode);
    }

    /**
     * Hjelpemetode brukt i tester og generering av dokumentasjon.
     */
    static String lagVilkårTekst(HbVedtaksbrevPeriodeOgFelles periode) {
        StringBuilder vilkårTekst = new StringBuilder();
        if (periode.getPeriode().getVurderinger().harForeldelseAvsnitt()) {
            // dobbelt linjeskift trengs for at den genererte teksten til dokumentasjon har lik formattering som i vedtaksbrevet.
            // linjeskiftene "forsvinner" når Foreldelse- og Vilkår-templatane blir generert hver for seg
            vilkårTekst.append(konverterMedPartialTemplate(PARTIAL_PERIODE_FORELDELSE, periode)).append("\n\n");
        }
        vilkårTekst.append(konverterMedPartialTemplate(PARTIAL_PERIODE_VILKÅR, periode));
        return vilkårTekst.toString();
    }

    static String lagSærligeGrunnerTekst(HbVedtaksbrevFelles felles, HbVedtaksbrevPeriode periode) {
        HbVedtaksbrevPeriodeOgFelles data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);
        return konverterMedPartialTemplate(PARTIAL_PERIODE_SÆRLIGE_GRUNNER, data);
    }

    private static String konverterMedPartialTemplate(String partial, HandlebarsData handlebarsData) {
        Template template = getTemplateFraPartial(partial, handlebarsData.getSpråkkode());
        return applyTemplate(template, handlebarsData);
    }

    private static String applyTemplate(Template template, HandlebarsData data) {
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
            throw TekstformatererBrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template getTemplate(String filsti, Språkkode språkkode) {
        String språkstøttetFilsti = lagSpråkstøttetFilsti(filsti, språkkode);
        if (TEMPLATE_CACHE.containsKey(språkstøttetFilsti)) {
            return TEMPLATE_CACHE.get(språkstøttetFilsti);
        }
        TEMPLATE_CACHE.put(språkstøttetFilsti, opprettHandlebarsTemplate(språkstøttetFilsti));
        return TEMPLATE_CACHE.get(språkstøttetFilsti);
    }

    private static Template getTemplateFraPartial(String partial, Språkkode språkkode) {
        String språkstøttetFilsti = lagSpråkstøttetFilsti(partial, språkkode);
        if (TEMPLATE_CACHE.containsKey(språkstøttetFilsti)) {
            return TEMPLATE_CACHE.get(språkstøttetFilsti);
        }
        TEMPLATE_CACHE.put(språkstøttetFilsti, opprettTemplateFraPartials(
            lagSpråkstøttetFilsti("vedtak/vedtak_felles", språkkode),
            språkstøttetFilsti
        ));
        return TEMPLATE_CACHE.get(språkstøttetFilsti);
    }

    private static Template opprettTemplateFraPartials(String... partials) {
        StringBuilder builder = new StringBuilder();
        for (String partial : partials) {
            builder.append("{{> ")
                .append(partial)
                .append("}}")
                .append("\n");
        }

        Handlebars handlebars = opprettVedtakHandlebarsKonfigurasjon();
        try {
            return handlebars.compileInline(builder.toString());
        } catch (IOException e) {
            throw TekstformatterFeil.FACTORY.klarteIkkeKompilerePartialTemplate(partials, e).toException();
        }
    }


    private static Template opprettHandlebarsTemplate(String filsti) {
        Handlebars handlebars = opprettVedtakHandlebarsKonfigurasjon();
        try {
            return handlebars.compile(filsti);
        } catch (IOException e) {
            throw TekstformatterFeil.FACTORY.klarteIkkeKompilereTemplate(filsti, e).toException();
        }
    }

    protected static Handlebars opprettVedtakHandlebarsKonfigurasjon() {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        handlebars.registerHelper("switch", new CustomHelpers.SwitchHelper());
        handlebars.registerHelper("case", new CustomHelpers.CaseHelper());
        handlebars.registerHelper("var", new CustomHelpers.VariableHelper());
        handlebars.registerHelper("lookup-map", new CustomHelpers.MapLookupHelper());
        handlebars.registerHelper("kroner", new CustomHelpers.KroneFormattererMedTusenskille());
        return handlebars;
    }

    interface TekstformatterFeil extends DeklarerteFeil {
        TekstformatterFeil FACTORY = FeilFactory.create(TekstformatterFeil.class);

        @TekniskFeil(feilkode = "FTP-531549", feilmelding = "Klarte ikke å kompiler template %s", logLevel = LogLevel.ERROR)
        Feil klarteIkkeKompilereTemplate(String template, IOException cause);

        @TekniskFeil(feilkode = "FTP-814712", feilmelding = "Klarte ikke å kompiler partial template %s", logLevel = LogLevel.ERROR)
        Feil klarteIkkeKompilerePartialTemplate(String[] template, IOException cause);

    }

}
