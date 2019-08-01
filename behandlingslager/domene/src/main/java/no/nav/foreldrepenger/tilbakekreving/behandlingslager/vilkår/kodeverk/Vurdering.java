package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import javax.persistence.MappedSuperclass;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;


@MappedSuperclass
public abstract class Vurdering extends Kodeliste {

    Vurdering() {
        // For hibernate
    }

    Vurdering(String kode, String discriminator) {
        super(kode, discriminator);
    }
}

