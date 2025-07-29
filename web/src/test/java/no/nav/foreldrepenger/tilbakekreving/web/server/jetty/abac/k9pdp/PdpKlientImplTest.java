package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9DataKeys;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9.K9PdpRequestBuilder;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.SifAbacPdpRestKlient;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.SaksinformasjonTilgangskontrollInputDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.resultat.IkkeTilgangÅrsak;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.resultat.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;


@ExtendWith(MockitoExtension.class)
class PdpKlientImplTest {

    private AppPdpKlientImpl pdpKlient;

    @Mock
    private K9AbacAuditlogger abacAuditloggerMock;
    @Mock
    private K9PdpRequestBuilder pdpRequestBuilder;
    @Mock
    private SifAbacPdpRestKlient restKlientMock;

    @BeforeEach
    public void setUp() {
        pdpKlient = new AppPdpKlientImpl(abacAuditloggerMock, pdpRequestBuilder, restKlientMock);
    }

    @Test
    void skalKallePåKlientOgGiTilgang() {
        UUID behandlingUuid = UUID.randomUUID();
        Mockito.when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(new K9AppRessursData.Builder()
            .leggTilRessurs(K9DataKeys.SAKSNUMMER, "SAK1")
            .build());

        when(restKlientMock.sjekkTilgangForInnloggetBruker(any(SaksinformasjonTilgangskontrollInputDto.class))).thenReturn(
            new Tilgangsbeslutning(true, Set.of()));

        K9AbacResultat resultat = pdpKlient.forespørTilgang(BeskyttetRessursAttributter.builder()
            .medActionType(ActionType.READ)
            .medResourceType(ResourceType.FAGSAK)
            .medDataAttributter(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingUuid)
            )
            .medBrukerId("Z000000")
            .medIdentType(IdentType.InternBruker)
            .medServicePath("foo/bar")
            .build());

        Mockito.verify(abacAuditloggerMock).loggUtfall(Mockito.eq(K9AbacResultat.GODKJENT), any(), any());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void skalKallePåKlientOgIkkeGiTilgang() {
        UUID behandlingUuid = UUID.randomUUID();
        Mockito.when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(new K9AppRessursData.Builder()
            .leggTilRessurs(K9DataKeys.SAKSNUMMER, "SAK1")
            .build());

        when(restKlientMock.sjekkTilgangForInnloggetBruker(any(SaksinformasjonTilgangskontrollInputDto.class))).thenReturn(
            new Tilgangsbeslutning(false, Set.of(IkkeTilgangÅrsak.HAR_IKKE_TILGANG_TIL_KODE7_PERSON)));

        K9AbacResultat resultat = pdpKlient.forespørTilgang(BeskyttetRessursAttributter.builder()
            .medActionType(ActionType.READ)
            .medResourceType(ResourceType.FAGSAK)
            .medDataAttributter(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingUuid)
            )
            .medBrukerId("Z000000")
            .medIdentType(IdentType.InternBruker)
            .medServicePath("foo/bar")
            .build());

        Mockito.verify(abacAuditloggerMock).loggUtfall(Mockito.eq(K9AbacResultat.AVSLÅTT_KODE_7), any(), any());
        assertThat(resultat.fikkTilgang()).isFalse();
    }


}
