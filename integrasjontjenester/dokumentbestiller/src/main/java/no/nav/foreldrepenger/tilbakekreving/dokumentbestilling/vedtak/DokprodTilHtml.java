package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

public class DokprodTilHtml {

    static String dokprodHovedoverskriftTilHtml(String dokprod) {
        return "<h1>" + konverterNbsp(dokprod) + "</h1>";
    }

    static String dokprodInnholdTilHtml(String dokprod) {
        String[] linjer = dokprod.split("\n");
        StringBuilder builder = new StringBuilder();
        for (String linje : linjer) {
            if (linje.isBlank()) {
                continue;
            }
            boolean overskrift = linje.startsWith("_");
            if (overskrift) {
                boolean erUnderoverskrift = false; //dropper underoverskrifter inntil videre
                if (erUnderoverskrift) {
                    builder.append("<h3>").append(linje.substring(1)).append("</h3>");
                } else {
                    builder.append("<h2>").append(linje.substring(1)).append("</h2>");
                }
            } else {
                builder.append("<p>").append(linje).append("</p>");
            }
        }
        return konverterNbsp(builder.toString());
    }

    static String konverterNbsp(String s) {
        String utf8nonBreakingSpace = "\u00A0";
        String htmlNonBreakingSpace = "&nbsp;";
        return s.replaceAll(utf8nonBreakingSpace, htmlNonBreakingSpace);
    }
}
