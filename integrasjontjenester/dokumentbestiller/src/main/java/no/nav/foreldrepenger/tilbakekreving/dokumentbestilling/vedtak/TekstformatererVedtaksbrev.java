package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.CustomHelpers;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.ObjectMapperForUtvekslingAvDataMedHandlebars;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.TekstformatererVarselbrevFeil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.util.Objects;

class TekstformatererVedtaksbrev {
    private static Map<String, Template> TEMPLATE_CACHE = new HashMap<>();

    private static String PARTIAL_PERIODE_OVERSKRIFT = "vedtak/periode_overskrift";
    private static String PARTIAL_PERIODE_FAKTA = "vedtak/periode_fakta";
    private static String PARTIAL_PERIODE_VILKÅR = "vedtak/periode_vilkår";
    private static String PARTIAL_PERIODE_SÆRLIGE_GRUNNER = "vedtak/periode_særlige_grunner";
    private static String PARTIAL_PERIODE_SLUTT = "vedtak/periode_slutt";
    private static String PARTIAL_VEDTAK_START = "vedtak/vedtak_start";
    private static String PARTIAL_VEDTAK_SLUTT = "vedtak/vedtak_slutt";
    private static String PARTIAL_VEDTAK_FELLES = "vedtak/vedtak_felles";
    static String FRITEKST_MARKERING_START = "\\\\FRITEKST_START";
    static String FRITEKST_PÅKREVET_MARKERING_START = "\\\\PÅKREVET_FRITEKST_START";
    static String FRITEKST_MARKERING_SLUTT = "\\\\FRITEKST_SLUTT";

    private static final ObjectMapper OM = ObjectMapperForUtvekslingAvDataMedHandlebars.INSTANCE;

    private TekstformatererVedtaksbrev() {
        // for static access
    }

    static List<Avsnitt> lagVedtaksbrevDeltIAvsnitt(HbVedtaksbrevData vedtaksbrevData, String hovedoverskrift) {
        List<Avsnitt> resultat = new ArrayList<>();
        settInnMarkeringForFritekst(vedtaksbrevData);
        resultat.add(lagOppsummeringAvsnitt(vedtaksbrevData, hovedoverskrift));
        resultat.addAll(lagPerioderAvsnitt(vedtaksbrevData));
        resultat.add(lagAvsluttendeAvsnitt(vedtaksbrevData));
        return resultat;
    }

    private static void settInnMarkeringForFritekst(HbVedtaksbrevData vedtaksbrevData) {
        for (HbVedtaksbrevPeriode periode : vedtaksbrevData.getPerioder()) {
            periode.setFritekstFakta(markerFritekst(periode.getFritekstFakta(), Underavsnitt.Underavsnittstype.FAKTA));
            periode.setFritekstVilkår(markerFritekst(periode.getFritekstVilkår(), Underavsnitt.Underavsnittstype.VILKÅR));
            periode.setFritekstSærligeGrunner(markerFritekst(periode.getFritekstSærligeGrunner(), Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER));
            periode.setFritekstSærligeGrunnerAnnet(markerFritekst(periode.getFritekstSærligeGrunnerAnnet(), Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER_ANNET));
        }
        vedtaksbrevData.getFelles().setFritekstOppsummering(markerFritekst(vedtaksbrevData.getFelles().getFritekstOppsummering()));
    }

    static String markerFritekst(String fritekst) {
        return markerFritekst(fritekst, null);
    }

    static String markerFritekst(String fritekst, Underavsnitt.Underavsnittstype underavsnittstype) {
        String startmarkør = underavsnittstype == null
            ? FRITEKST_MARKERING_START
            : FRITEKST_MARKERING_START + underavsnittstype;
        return fritekst == null
            ? startmarkør + "\n" + FRITEKST_MARKERING_SLUTT
            : startmarkør + "\n" + fritekst + "\n" + FRITEKST_MARKERING_SLUTT;
    }

    static String markerPåkrevetFritekst(String fritekst) {
        return markerPåkrevetFritekst(fritekst, null);
    }

    static String markerPåkrevetFritekst(String fritekst, Underavsnitt.Underavsnittstype underavsnittstype) {
        String startmarkør = underavsnittstype == null
            ? FRITEKST_PÅKREVET_MARKERING_START
            : FRITEKST_PÅKREVET_MARKERING_START + underavsnittstype;
        return fritekst == null
            ? startmarkør + "\n" + FRITEKST_MARKERING_SLUTT
            : startmarkør + "\n" + fritekst + "\n" + FRITEKST_MARKERING_SLUTT;
    }

    static Avsnitt lagOppsummeringAvsnitt(HbVedtaksbrevData vedtaksbrevData, String hovedoverskrift) {
        String tekst = konverterMedPartialTemplate(PARTIAL_VEDTAK_START, vedtaksbrevData);
        Avsnitt.Builder avsnittBuilder = new Avsnitt.Builder().medAvsnittstype(Avsnitt.Avsnittstype.OPPSUMMERING).medOverskrift(hovedoverskrift);
        return parseTekst(tekst, avsnittBuilder, null).build();
    }

    static List<Avsnitt> lagPerioderAvsnitt(HbVedtaksbrevData vedtaksbrevData) {
        var resultat = new ArrayList<Avsnitt>();
        for (HbVedtaksbrevPeriode periode : vedtaksbrevData.getPerioder()) {
            resultat.add(lagPeriodeAvsnitt(new HbVedtaksbrevPeriodeOgFelles(vedtaksbrevData.getFelles(), periode)));
        }
        return resultat;
    }

    static Avsnitt lagAvsluttendeAvsnitt(HbVedtaksbrevData vedtaksbrevData) {
        String tekst = konverterMedPartialTemplate(PARTIAL_VEDTAK_SLUTT, vedtaksbrevData);
        Avsnitt.Builder avsnittBuilder = new Avsnitt.Builder().medAvsnittstype(Avsnitt.Avsnittstype.TILLEGGSINFORMASJON);
        return parseTekst(tekst, avsnittBuilder, null).build();
    }

    private static Avsnitt lagPeriodeAvsnitt(HbVedtaksbrevPeriodeOgFelles data) {
        String overskrift = konverterMedPartialTemplate(PARTIAL_PERIODE_OVERSKRIFT, data);
        String faktatekst = konverterMedPartialTemplate(PARTIAL_PERIODE_FAKTA, data);
        String vilkårTekst = konverterMedPartialTemplate(PARTIAL_PERIODE_VILKÅR, data);
        String særligeGrunnerTekst = konverterMedPartialTemplate(PARTIAL_PERIODE_SÆRLIGE_GRUNNER, data);
        String avsluttendeTekst = konverterMedPartialTemplate(PARTIAL_PERIODE_SLUTT, data);

        Avsnitt.Builder avsnittBuilder = new Avsnitt.Builder()
            .medAvsnittstype(Avsnitt.Avsnittstype.PERIODE)
            .medPeriode(data.getPeriode().getPeriode())
            .medOverskrift(fjernOverskriftFormattering(overskrift));

        parseTekst(faktatekst, avsnittBuilder, Underavsnitt.Underavsnittstype.FAKTA);
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
            if (erFritekstStart(linje)) {
                Objects.check(!leserFritekst, "Feil med vedtaksbrev, har markering for 2 fritekst-start etter hverandre");
                fritekstPåkrevet = erFritekstPåkrevetStart(linje);
                underavsnittstype = parseUnderavsnittstype(linje);
                leserFritekst = true;
                fritekstLinjer = new ArrayList<>();
            } else if (erFritekstSlutt(linje)) {
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

    private static boolean erFritekstStart(String tekst) {
        return tekst.startsWith(FRITEKST_MARKERING_START) || tekst.startsWith(FRITEKST_PÅKREVET_MARKERING_START);
    }

    private static boolean erFritekstPåkrevetStart(String tekst) {
        return tekst.startsWith(FRITEKST_PÅKREVET_MARKERING_START);
    }

    private static Underavsnitt.Underavsnittstype parseUnderavsnittstype(String tekst) {
        String rest = null;
        if (tekst.startsWith(FRITEKST_MARKERING_START)) {
            rest = tekst.substring(FRITEKST_MARKERING_START.length());
        } else if (tekst.startsWith(FRITEKST_PÅKREVET_MARKERING_START)) {
            rest = tekst.substring(FRITEKST_PÅKREVET_MARKERING_START.length());
        } else {
            throw new IllegalArgumentException("Utvikler-feil: denne metoden skal bare brukes på fritekstmarkering-start");
        }
        for (Underavsnitt.Underavsnittstype underavsnittstype : Underavsnitt.Underavsnittstype.values()) {
            if (underavsnittstype.name().equals(rest)) {
                return underavsnittstype;
            }
        }
        return null;
    }

    private static boolean erFritekstSlutt(String tekst) {
        return FRITEKST_MARKERING_SLUTT.equals(tekst);
    }

    private static boolean erOverskrift(String tekst) {
        return tekst.startsWith("_");
    }

    private static String fjernOverskriftFormattering(String tekst) {
        return tekst.substring(1);
    }

    static String lagVedtaksbrevFritekst(HbVedtaksbrevData vedtaksbrevData) {
        Template template = getTemplate("vedtak");
        return applyTemplate(template, vedtaksbrevData);
    }

    static String lagFaktaTekst(HbVedtaksbrevPeriodeOgFelles periode) {
        return konverterMedPartialTemplate(PARTIAL_PERIODE_FAKTA, periode);
    }

    static String lagVilkårTekst(HbVedtaksbrevPeriodeOgFelles periode) {
        return konverterMedPartialTemplate(PARTIAL_PERIODE_VILKÅR, periode);
    }

    static String lagSærligeGrunnerTekst(HbVedtaksbrevFelles felles, HbVedtaksbrevPeriode periode) {
        HbVedtaksbrevPeriodeOgFelles data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);
        return konverterMedPartialTemplate(PARTIAL_PERIODE_SÆRLIGE_GRUNNER, data);
    }

    private static String konverterMedPartialTemplate(String partial, HandlebarsData vedtaksbrevPeriode) {
        Template template = getTemplateFraPartial(partial);
        return applyTemplate(template, vedtaksbrevPeriode);
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
            throw TekstformatererVarselbrevFeil.FACTORY.feilVedTekstgenerering(e).toException();
        }
    }

    private static Template getTemplate(String filsti) {
        if (TEMPLATE_CACHE.containsKey(filsti)) {
            return TEMPLATE_CACHE.get(filsti);
        }
        TEMPLATE_CACHE.put(filsti, opprettHandlebarsTemplate(filsti));
        return TEMPLATE_CACHE.get(filsti);
    }

    private static Template getTemplateFraPartial(String partial) {
        if (TEMPLATE_CACHE.containsKey(partial)) {
            return TEMPLATE_CACHE.get(partial);
        }
        TEMPLATE_CACHE.put(partial, opprettTemplateFraPartials(PARTIAL_VEDTAK_FELLES, partial));
        return TEMPLATE_CACHE.get(partial);
    }

    private static Template opprettTemplateFraPartials(String... partials) {
        StringBuilder builder = new StringBuilder();
        for (String partial : partials) {
            builder.append("{{> ")
                .append(partial)
                .append("}}")
                .append("\n");
        }

        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        try {
            return handlebars.compileInline(builder.toString());
        } catch (IOException e) {
            throw TekstformatterFeil.FACTORY.klarteIkkeKompilerePartialTemplate(partials, e).toException();
        }
    }


    private static Template opprettHandlebarsTemplate(String filsti) {
        Handlebars handlebars = opprettHandlebarsKonfigurasjon();
        try {
            return handlebars.compile(filsti);
        } catch (IOException e) {
            throw TekstformatterFeil.FACTORY.klarteIkkeKompilereTemplate(filsti, e).toException();
        }
    }

    private static Handlebars opprettHandlebarsKonfigurasjon() {
        ClassPathTemplateLoader loader = new ClassPathTemplateLoader();
        loader.setCharset(Charset.forName("UTF-8"));
        loader.setPrefix("/templates/");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);

        handlebars.setCharset(Charset.forName("UTF-8"));
        handlebars.setInfiniteLoops(false);
        handlebars.setPrettyPrint(true);

        handlebars.registerHelper("switch", new CustomHelpers.SwitchHelper());
        handlebars.registerHelper("case", new CustomHelpers.CaseHelper());
        handlebars.registerHelper("var", new CustomHelpers.VariableHelper());
        handlebars.registerHelper("lookup-map", new CustomHelpers.MapLookupHelper());
        handlebars.registerHelpers(ConditionalHelpers.class);
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
