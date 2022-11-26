package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

public interface CustomHelpers {

    class SwitchHelper implements Helper<Object> {

        @Override
        public Object apply(Object variable, Options options) throws IOException {
            List<String> variabelNavn = new ArrayList<>();
            List<Object> variabelVerdier = new ArrayList<>();
            variabelNavn.add("__condition_fulfilled");
            variabelVerdier.add(0);
            variabelNavn.add("__condition_variable");
            variabelVerdier.add(options.hash.isEmpty() ? variable : options.hash);
            Context ctx = Context.newBlockParamContext(options.context, variabelNavn, variabelVerdier);
            String resultat = options.fn.apply(ctx);


            Integer antall = (Integer) ctx.get("__condition_fulfilled");
            if (Integer.valueOf(1).equals(antall)) {
                return resultat;
            }
            throw new IllegalArgumentException("Switch-case må treffe i 1 case, men traff i " + antall + " med verdien " + ctx.get("__condition_variable"));
        }
    }

    class CaseHelper implements Helper<Object> {
        @Override
        public Object apply(Object konstant, Options options) throws IOException {
            List<Object> konstanter = new ArrayList<>();
            if (options.hash.isEmpty()) {
                konstanter.add(konstant);
                konstanter.addAll(List.of(options.params));
            } else {
                konstanter.add(options.hash);
            }

            Map<String, Object> model = (Map<String, Object>) options.context.model();
            Object condition_variable = model.get("__condition_variable");

            int antallTreff = konstanter.stream()
                .mapToInt(k -> k.equals(condition_variable) ? 1 : 0)
                .reduce(Integer::sum)
                .orElseThrow();

            if (antallTreff > 0) {
                Integer kumulativtAntallTreff = (Integer) model.get("__condition_fulfilled");
                model.put("__condition_fulfilled", kumulativtAntallTreff += antallTreff);
                return options.fn();
            }
            return options.inverse();
        }
    }

    class VariableHelper implements Helper<Object> {
        @Override
        public Object apply(Object context, Options options) throws IOException {
            List<String> variabelNavn = new ArrayList<>();
            List<Object> variabelVerdier = new ArrayList<>();
            for (Map.Entry<String, Object> variable : options.hash.entrySet()) {
                variabelNavn.add(variable.getKey());
                variabelVerdier.add(variable.getValue());
            }

            Context ctx = Context.newBlockParamContext(options.context, variabelNavn, variabelVerdier);
            return options.fn.apply(ctx);
        }
    }

    class MapLookupHelper implements Helper<Object> {
        @Override
        public Object apply(Object context, Options options) {
            String key = context.toString();
            Object defaultVerdi = options.param(0, null);
            Object verdi = options.hash(key, defaultVerdi);
            if (verdi == null) {
                throw new IllegalArgumentException("Fant ikke verdi for " + key + " i " + options.hash);
            }
            return verdi;
        }
    }

    class KroneFormattererMedTusenskille implements Helper<Object> {
        @Override
        public Object apply(Object context, Options options) {
            if (context == null) {
                throw new IllegalArgumentException("Mangler context");
            }
            String key = context.toString();
            if (key == null) {
                throw new IllegalArgumentException("Mangler påkrevd beløp");
            }
            String utf8nonBreakingSpace = "\u00A0";
            BigDecimal beløp = new BigDecimal(key);
            String beløpMedTusenskille = medTusenskille(beløp, utf8nonBreakingSpace);
            String benevning = beløp.compareTo(BigDecimal.ONE) == 0 ? "krone" : "kroner";
            return beløpMedTusenskille + utf8nonBreakingSpace + benevning;
        }

        public static String medTusenskille(BigDecimal verdi, String tusenskille) {
            BigDecimal minsteTallTusenskille = BigDecimal.valueOf(1000);
            if (verdi.compareTo(minsteTallTusenskille) < 0) {
                return verdi.toPlainString();
            }
            String utenTusenskille = verdi.toPlainString();
            int antallFørFørsteTusenskille = utenTusenskille.length() % 3 != 0 ? utenTusenskille.length() % 3 : 3;
            String resultat = utenTusenskille.substring(0, antallFørFørsteTusenskille);
            int index = antallFørFørsteTusenskille;
            while (index < utenTusenskille.length()) {
                resultat += tusenskille;
                resultat += utenTusenskille.substring(index, index + 3);
                index += 3;
            }
            return resultat;
        }
    }

    class PeriodeFormatterer implements Helper<Object> {

        @Override
        public Object apply(Object o, Options options) throws IOException {
            //slår opp tekst for å få riktig på nynorsk
            Object fraTekst = options.hash("fra", "fra");
            Object tilTekst = options.hash("til", "til");
            boolean kompakt = options.hash("kompakt", false);
            if (!(o instanceof JsonNode)) {
                throw new IllegalArgumentException();
            }
            JsonNode jsonNode = (JsonNode) o;
            JsonNode fomNode = Objects.requireNonNull(jsonNode.get(kompakt ? "fom-kompakt" : "fom"), "periode manger fom");
            JsonNode tomNode = Objects.requireNonNull(jsonNode.get(kompakt ? "tom-kompakt" : "tom"), "periode manger tom");
            String fom = Objects.requireNonNull(fomNode.asText(), "fom er null");
            String tom = Objects.requireNonNull(tomNode.asText(), "tom er null");
            if (fom.equals(tom)) {
                return fom;
            } else {
                return fraTekst + " " + fom + " " + tilTekst + " " + tom;
            }
        }
    }

    class PerioderFormatterer implements Helper<Object> {

        @Override
        public Object apply(Object o, Options options) throws IOException {
            //slår opp tekst for å få riktig på nynorsk
            Object fraTekst = options.hash("fra", "fra");
            Object tilTekst = options.hash("til", "til");
            boolean kompakt = options.hash("kompakt", false);
            if (!(o instanceof ArrayNode liste)) {
                throw new IllegalArgumentException("Input må være en liste av perioder, men var " + (o == null ? null : o.getClass()));
            }
            List<String> perioder = new ArrayList<>();
            for (int i = 0; i < liste.size(); i++) {
                JsonNode jsonNode = liste.get(i);
                if (jsonNode.get("periode") != null) {
                    jsonNode = jsonNode.get("periode");
                }
                JsonNode fomNode = Objects.requireNonNull(jsonNode.get(kompakt ? "fom-kompakt" : "fom"), "periode på index " + i + " manger fom");
                JsonNode tomNode = Objects.requireNonNull(jsonNode.get(kompakt ? "tom-kompakt" : "tom"), "periode på index " + i + " manger tom");
                String fom = Objects.requireNonNull(fomNode.asText(), "fom er null i indeks " + i);
                String tom = Objects.requireNonNull(tomNode.asText(), "tom er null i indeks " + i);
                if (fom.equals(tom)) {
                    perioder.add(fom);
                } else {
                    perioder.add("perioden " + fraTekst + " " + fom + " " + tilTekst + " " + tom);
                }
            }
            return storForbokstav(listUt(perioder));
        }

        private String storForbokstav(String tekst) {
            if (tekst.isEmpty()) {
                return tekst;
            }
            String førsteTegn = tekst.substring(0, 1);
            String resten = tekst.substring(1);
            return førsteTegn.toUpperCase() + resten;
        }

        private String listUt(List<String> perioder) {
            String resultat = "";
            for (int i = 0; i < perioder.size(); i++) {
                if (i > 0) {
                    boolean erSiste = i == perioder.size() - 1;
                    resultat += erSiste ? " og " : ", ";
                }
                resultat += perioder.get(i);
            }
            return resultat;
        }
    }


}
