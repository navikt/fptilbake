package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.AbstractTestScenario;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.VurderForeldelseDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;
import no.nav.vedtak.util.FPDateUtil;

@RunWith(CdiRunner.class)
public class AksjonspunktApplikasjonTjenesteImplTest {

    private static final String BEGRUNNELSE = "begrunnelse";
    private static final LocalDate FOM = FPDateUtil.iDag().minusDays(40);
    private static final LocalDate TOM = FPDateUtil.iDag().minusDays(7);

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

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

        VurderForeldelseDto dto = new VurderForeldelseDto();
        dto.setForeldelsePerioder(Collections.singletonList(new ForeldelsePeriodeDto(FOM, TOM, ForeldelseVurderingType.IKKE_FORELDET, BEGRUNNELSE)));

        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(Collections.singletonList(dto), behandling.getId());

        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandling.getId());

        assertThat(oppdatertBehandling.getAksjonspunkter()).first().matches(Aksjonspunkt::erUtført);
    }

}
