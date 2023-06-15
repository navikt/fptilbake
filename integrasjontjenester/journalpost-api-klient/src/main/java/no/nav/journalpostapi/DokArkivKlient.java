package no.nav.journalpostapi;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.dokarkiv.AbstractDokArkivKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "dokarkiv.base.url", endpointDefault = "http://dokarkiv.teamdokumenthandtering/rest/journalpostapi/v1/journalpost",
    scopesProperty = "dokarkiv.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.dokarkiv/.default")
public class DokArkivKlient extends AbstractDokArkivKlient {

    public static String TEMA_FORELDREPENGER_SVANGERSKAPSPENGER = "FOR";
    public static String TEMA_OMSORGSPENGER_PLEIEPENGER_OPPLÆRINGSPENGER = "OMS";
    public static String TEMA_FRISINN = "FRI";

    public static String BEHANDLINGTEMA_FEILUTBETALING = "ab0006";
    public static String BEHANDLINGTEMA_TILBAKEBETALING = "ab0007";

    // Utdøende kodeverk
    public static String DOKUMENTKATEGORI_BREV = "B";
    public static String DOKUMENTKATEGORI_VEDTAKSBREV = "VB";

    protected DokArkivKlient() {
        super();
    }
}
