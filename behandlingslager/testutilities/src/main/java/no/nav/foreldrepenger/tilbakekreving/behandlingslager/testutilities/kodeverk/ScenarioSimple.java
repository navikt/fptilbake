package no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk;


/**
 * Oppretter en default behandling
 */
public class ScenarioSimple extends AbstractTestScenario<ScenarioSimple> {

    public ScenarioSimple() {
        super();
    }

    public static ScenarioSimple simple() {
        return new ScenarioSimple();
    }
}
