package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

public class BehandlingModellEnkelTest {

    private static final BehandlingStegType STEG_1 = BehandlingStegType.FAKTA_VERGE;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.FAKTA_FEILUTBETALING;
    private static final BehandlingStegType STEG_3 = BehandlingStegType.FORELDELSEVURDERINGSTEG;

    private static final BehandlingType BEHANDLING_TYPE = BehandlingType.UDEFINERT;

    private final DummySteg dummyBehandlingSteg_1 = new DummySteg();
    private final DummySteg dummyBehandlingSteg_2 = new DummySteg();
    private final DummySteg dummyBehandlingSteg_3 = new DummySteg();

    private final BehandlingModellImpl.BiFunction<BehandlingStegType, BehandlingType, BehandlingSteg> finnSteg = DummySteg.map(
            List.of(
                    new TestStegKonfig(STEG_1, BEHANDLING_TYPE, dummyBehandlingSteg_1),
                    new TestStegKonfig(STEG_2, BEHANDLING_TYPE, dummyBehandlingSteg_2),
                    new TestStegKonfig(STEG_3, BEHANDLING_TYPE, dummyBehandlingSteg_3)));

    @Test
    public void skal_bygge_behandlingskontroll_med_ett_steg() throws Exception {
        try (var modell = new BehandlingModellImpl(BehandlingType.TILBAKEKREVING, finnSteg)) {
            modell.leggTil(STEG_1, BEHANDLING_TYPE);

            assertThat(modell.finnSteg(STEG_1)).isNotNull();
            assertThat(modell.finnSteg(STEG_1).getSteg()).isEqualTo(dummyBehandlingSteg_1);

            assertThat(modell.finnForrigeSteg(STEG_1)).isNull();
            assertThat(modell.finnNesteSteg(STEG_1)).isNull();
        }
    }

    @Test
    public void skal_bygge_behandlingskontroll_med_3_steg() throws Exception {
        // Arrange
        try (var modell = new BehandlingModellImpl(BehandlingType.TILBAKEKREVING, finnSteg)) {
            modell.leggTil(STEG_1, BEHANDLING_TYPE);
            modell.leggTil(STEG_2, BEHANDLING_TYPE);
            modell.leggTil(STEG_3, BEHANDLING_TYPE);

            // Act - Assert
            var finnSteg2 = modell.finnSteg(STEG_2);
            assertThat(finnSteg2).isNotNull();
            assertThat(finnSteg2.getSteg()).isSameAs(dummyBehandlingSteg_2);

            assertThat(modell.finnForrigeSteg(STEG_1)).isNull();

            assertThat(modell.finnForrigeSteg(STEG_2)).isNotNull();
            assertThat(modell.finnForrigeSteg(STEG_2).getSteg()).isSameAs(dummyBehandlingSteg_1);

            assertThat(modell.finnNesteSteg(STEG_2)).isNotNull();
            assertThat(modell.finnNesteSteg(STEG_2).getSteg()).isSameAs(dummyBehandlingSteg_3);

            assertThat(modell.finnNesteSteg(STEG_3)).isNull();
        }
    }
}
