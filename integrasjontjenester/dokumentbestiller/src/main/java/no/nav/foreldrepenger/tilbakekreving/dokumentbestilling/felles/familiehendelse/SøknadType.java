package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.familiehendelse;

import java.util.Objects;

public enum SøknadType {
    FØDSEL("ST-001"), //$NON-NLS-1$
    ADOPSJON("ST-002"), //$NON-NLS-1$
    ;

    private final String kode;

    SøknadType(String kode) {
        this.kode = kode;
    }

    public static SøknadType fra(String kode) {
        for (SøknadType st : values()) {
            if (Objects.equals(st.kode, kode)) {
                return st;
            }
        }
        throw new IllegalArgumentException("Ukjent " + SøknadType.class.getSimpleName() + ": " + kode); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
