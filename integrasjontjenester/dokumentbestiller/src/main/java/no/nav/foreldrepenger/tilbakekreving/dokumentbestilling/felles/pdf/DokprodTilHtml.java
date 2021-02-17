package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

public class DokprodTilHtml {

    public static String dokprodInnholdTilHtml(String dokprod) {
        StringBuilder builder = new StringBuilder();

        String[] avsnittene = hentAvsnittene(dokprod);

        boolean samepageStarted = false;
        for (String avsnitt : avsnittene) {
            boolean inBulletpoints = false;
            boolean harAvsnitt = false;
            String[] linjer = avsnitt.split("\n\r?");
            for (String linje : linjer) {
                if (linje.isBlank()) {
                    builder.append("<br/>");
                    continue;
                }
                if (linje.startsWith("*-")) {
                    inBulletpoints = true;
                    linje = linje.substring(2);
                    builder.append("<ul>");
                    if (linje.isBlank()) {
                        continue;
                    }
                }
                if (inBulletpoints) {
                    if (linje.stripTrailing().endsWith("-*")) {
                        builder.append("<li>").append(linje.replace("-*", "")).append("</li></ul>");
                        inBulletpoints = false;
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
                    if (!harAvsnitt) {
                        harAvsnitt = true;
                        builder.append("<p>");
                    } else {
                        builder.append("<br/>");
                    }
                    builder.append(linje.replaceAll("&(?!amp;)", "&amp;"));
                }
            }
            if (harAvsnitt) {
                builder.append("</p>");
            }
            if (samepageStarted) {
                samepageStarted = false;
                builder.append("</div>");
            }
        }
        return ekstraLinjeskiftFørHilsing(konverterNbsp(builder.toString()));
    }

    private static String[] hentAvsnittene(String dokprod) {
        //avsnitt ved dobbelt linjeskift
        //avsnitt ved overskrift (linje starter med _)
        return dokprod.split("(\n\r?\n\r?)|(\n\r?(?=_))");
    }

    static String konverterNbsp(String s) {
        String utf8nonBreakingSpace = "\u00A0";
        String htmlNonBreakingSpace = "&nbsp;";
        return s.replaceAll(utf8nonBreakingSpace, htmlNonBreakingSpace);
    }

    static String ekstraLinjeskiftFørHilsing(String s) {
        return s
            .replace("<p>Med vennlig hilsen", "<p class=\"hilsen\">Med vennlig hilsen")
            .replace("<p>Med vennleg helsing", "<p class=\"hilsen\">Med vennleg helsing");
    }
}
