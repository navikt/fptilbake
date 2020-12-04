package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.RevurderingOpprettetÅrsak;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.YtelseType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;

public class BehandlingTilstandMapperTest {

    @Test
    public void skal_serialisere_riktig() {
        String string = BehandlingTilstandMapper.tilJsonString(lagTestTilstand());
        Assertions.assertThat(string).isEqualTo("{\"funksjonellTid\":\"2020-03-05T13:48:10.001Z\",\"saksnummer\":\"SNR123\",\"ytelseType\":\"SVP\",\"behandlingUuid\":\"dd31dc4f-ea61-4124-8943-7738fa9b7b7a\",\"behandlingType\":\"REVURDERING_TILBAKEKREVING\",\"behandlingStatus\":\"AVSLUTTET\",\"behandlingResultat\":\"HENLAGT_FEILOPPRETTET\",\"erBehandlingManueltOpprettet\":false,\"behandlendeEnhetKode\":\"1234\",\"ansvarligSaksbehandler\":\"Z111111\",\"forrigeBehandling\":\"a93862f2-b72c-4e44-b042-0829599c3b53\",\"venterPaaBruker\":false,\"venterPaaOekonomi\":false,\"revurderingOpprettetAarsak\":\"KLAGE_NFP\"}");
    }

    @Test
    public void skal_serialisere_og_deserialisere() {
        String serialisert = BehandlingTilstandMapper.tilJsonString(lagTestTilstand());
        BehandlingTilstand resultat = BehandlingTilstandMapper.fraJson(serialisert);
        Assertions.assertThat(resultat.getYtelseType()).isEqualTo(YtelseType.SVP);
        Assertions.assertThat(resultat.getSaksnummer()).isEqualTo("SNR123");
        Assertions.assertThat(resultat.getBehandlingUuid()).isEqualTo(UUID.fromString("dd31dc4f-ea61-4124-8943-7738fa9b7b7a"));
        Assertions.assertThat(resultat.getBehandlingResultat()).isEqualTo(BehandlingResultat.HENLAGT_FEILOPPRETTET);
        Assertions.assertThat(resultat.getBehandlingStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
        Assertions.assertThat(resultat.getBehandlingType()).isEqualTo(BehandlingType.REVURDERING_TILBAKEKREVING);
        Assertions.assertThat(resultat.getBehandlendeEnhetKode()).isEqualTo("1234");
        Assertions.assertThat(resultat.getAnsvarligSaksbehandler()).isEqualTo("Z111111");
        Assertions.assertThat(resultat.getFunksjonellTid()).isEqualTo(OffsetDateTime.of(2020, 3, 5, 13, 48, 10, 1000000, ZoneOffset.UTC));
        Assertions.assertThat(resultat.getRevurderingOpprettetÅrsak()).isEqualTo(RevurderingOpprettetÅrsak.KLAGE_NFP);
        Assertions.assertThat(resultat.getForrigeBehandling()).isEqualTo(UUID.fromString("a93862f2-b72c-4e44-b042-0829599c3b53"));
        Assertions.assertThat(resultat.venterPåBruker()).isFalse();
        Assertions.assertThat(resultat.venterPåØkonomi()).isFalse();
    }

    private BehandlingTilstand lagTestTilstand() {
        BehandlingTilstand tilstand = new BehandlingTilstand();
        tilstand.setYtelseType(YtelseType.SVP);
        tilstand.setSaksnummer("SNR123");
        tilstand.setBehandlingUuid(UUID.fromString("dd31dc4f-ea61-4124-8943-7738fa9b7b7a"));
        tilstand.setBehandlingResultat(BehandlingResultat.HENLAGT_FEILOPPRETTET);
        tilstand.setBehandlingStatus(BehandlingStatus.AVSLUTTET);
        tilstand.setBehandlingType(BehandlingType.REVURDERING_TILBAKEKREVING);
        tilstand.setBehandlendeEnhetKode("1234");
        tilstand.setAnsvarligSaksbehandler("Z111111");
        tilstand.setFunksjonellTid(OffsetDateTime.of(2020, 3, 5, 13, 48, 10, 1000000, ZoneOffset.UTC));
        tilstand.setRevurderingOpprettetÅrsak(RevurderingOpprettetÅrsak.KLAGE_NFP);
        tilstand.setForrigeBehandling(UUID.fromString("a93862f2-b72c-4e44-b042-0829599c3b53"));
        tilstand.setVenterPåBruker(false);
        tilstand.setVenterPåØkonomi(false);
        return tilstand;
    }
}
