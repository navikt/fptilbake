package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;

public interface Kodeverdi extends IndexKey {

    // La stå inntil videre! Er ofte lagret i database som "-" i non-null kolonner.
    String STANDARDKODE_UDEFINERT = "-";

    String getKode();

    String getKodeverk();

    String getNavn();

    @Override
    default String getIndexKey() {
        return getKode();
    }
}
