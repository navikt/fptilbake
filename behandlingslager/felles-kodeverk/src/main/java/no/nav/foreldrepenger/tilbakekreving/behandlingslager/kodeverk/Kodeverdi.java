package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;

public interface Kodeverdi extends IndexKey {
    String getKode();

    String getKodeverk();

    String getNavn();

    @Override
    default String getIndexKey() {
        return getKode();
    }
}
