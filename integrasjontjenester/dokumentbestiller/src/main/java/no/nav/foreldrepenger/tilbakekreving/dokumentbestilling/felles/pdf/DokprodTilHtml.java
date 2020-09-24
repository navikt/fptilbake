package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

public class DokprodTilHtml {

    public static String dokprodInnholdTilHtml(String dokprod) {
        String[] linjer = dokprod.split("\n");
        StringBuilder builder = new StringBuilder();
        boolean samepageStarted = false;
        boolean inBulletpoints = false;
        for (String linje : linjer) {
            if (linje.isBlank()) {
                continue;
            }
            if (linje.startsWith("*-")) {
                inBulletpoints = true;
                linje = linje.substring(2);
                builder.append("<ul>");
                if (linje.isBlank()){
                    continue;
                }
            }
            if (inBulletpoints) {
                if (linje.stripTrailing().endsWith("-*")) {
                    builder.append("<li>").append(linje.replace("-*", "")).append("</li></ul>");
                    inBulletpoints = false;
                    if (samepageStarted) {
                        samepageStarted = false;
                        builder.append("</div>");
                    }
                } else {
                    builder.append("<li>").append(linje).append("</li>");
                }
                continue;
            }

            boolean overskrift = linje.startsWith("_");
            if (overskrift) {
                boolean erUnderoverskrift = false; //dropper underoverskrifter inntil videre
                if (samepageStarted) {
                    builder.append("</div>");
                } else {
                    samepageStarted = true;
                }
                builder.append("<div class=\"samepage\">");
                if (erUnderoverskrift) {
                    builder.append("<h3>").append(linje.substring(1)).append("</h3>");
                } else {
                    builder.append("<h2>").append(linje.substring(1)).append("</h2>");
                }
            } else {
                builder.append("<p>").append(linje).append("</p>");
                if (samepageStarted) {
                    samepageStarted = false;
                    builder.append("</div>");
                }
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
