package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.util.Properties;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Specializes;

@Dependent
@Specializes
/** mocker denne for junit-testene for å slippe å sette property */
public class MockTilbakekrevingApplicationPropertiesKonfigProvider extends TilbakekrevingApplicationPropertiesKonfigProvider {

    MockTilbakekrevingApplicationPropertiesKonfigProvider() {
        super(new Properties());
    }

}
