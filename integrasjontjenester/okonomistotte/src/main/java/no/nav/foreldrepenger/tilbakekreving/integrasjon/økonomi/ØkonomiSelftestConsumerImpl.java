package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingPortType;

public class ØkonomiSelftestConsumerImpl implements ØkonomiSelftestConsumer {

    private TilbakekrevingPortType port; //NOSONAR
    private String endpointUrl;

    public ØkonomiSelftestConsumerImpl(TilbakekrevingPortType port, String endpointUrl) {
        this.port = port;
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void ping() {
        //FIXME økonomi må implementere ping-tjeneste
        // port.ping(); //NOSONAR
        throw new IllegalStateException("Mangler ping-tjeneste hos tilbyder");
    }

    @Override
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
