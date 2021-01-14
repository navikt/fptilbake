package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

public class HelgHarYtelsedager {

    public static boolean helgHarYtelsedager(FagsakYtelseType fagsakYtelseType) {
        return fagsakYtelseType == FagsakYtelseType.OMSORGSPENGER || fagsakYtelseType == FagsakYtelseType.ENGANGSTØNAD;
    }

    public static boolean helgHarYtelsedager(FagOmrådeKode fagOmrådeKode) {
        return fagOmrådeKode == FagOmrådeKode.OMSORGSPENGER || fagOmrådeKode == FagOmrådeKode.ENGANGSSTØNAD;
    }
}
