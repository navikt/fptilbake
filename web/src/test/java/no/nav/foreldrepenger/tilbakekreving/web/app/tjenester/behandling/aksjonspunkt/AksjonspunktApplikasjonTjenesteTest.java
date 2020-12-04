package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.AbstractTestScenario;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.VurderForeldelseDto;

@CdiDbAwareTest
public class AksjonspunktApplikasjonTjenesteTest {

    private static final String BEGRUNNELSE = "begrunnelse";
    private static final LocalDate FOM = LocalDate.now().minusDays(40);
    private static final LocalDate TOM = LocalDate.now().minusDays(7);

    @Inject
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    private AbstractTestScenario<?> lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(aksjonspunktDefinisjon, BehandlingStegType.FORELDELSEVURDERINGSTEG);
        return scenario;
    }

    @Test
    public void test_skal_sette_aksjonspunkt_til_utført_og_lagre_behandling() {
        AbstractTestScenario<?> scenario = lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon.VURDER_FORELDELSE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);

        VurderForeldelseDto dto = new VurderForeldelseDto();
        dto.setForeldelsePerioder(Collections.singletonList(new ForeldelsePeriodeDto(FOM, TOM, ForeldelseVurderingType.IKKE_FORELDET, null, null, BEGRUNNELSE)));

        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(Collections.singletonList(dto), behandling.getId());

        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandling.getId());

        assertThat(oppdatertBehandling.getAksjonspunkter()).first().matches(Aksjonspunkt::erUtført);
    }

}
