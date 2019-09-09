package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev;

public class FritekstbrevXmlUtil {

    public static String fjernNamespace(String xml) {
        return xml.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
            replaceAll(" xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
            .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
            .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
    }
}
