package no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk;


/**
 * Oppretter en default behandling
 */
public class ScenarioSimple extends AbstractTestScenario<ScenarioSimple> {

    private ScenarioSimple() {
        super();
    }

    public static ScenarioSimple simple() {
        return new ScenarioSimple();
    }
}
