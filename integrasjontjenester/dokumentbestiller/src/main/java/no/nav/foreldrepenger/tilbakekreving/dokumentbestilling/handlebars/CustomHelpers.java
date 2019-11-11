package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        public Object apply(Object caseKonstant, Options options) throws IOException {
            Object konstant = options.hash.isEmpty() ? caseKonstant : options.hash;
            Map<String, Object> model = (Map<String, Object>) options.context.model();
            Object condition_variable = model.get("__condition_variable");
            if (konstant.equals(condition_variable)) {
                Integer antall = (Integer) model.get("__condition_fulfilled");
                model.put("__condition_fulfilled", ++antall);
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
}
