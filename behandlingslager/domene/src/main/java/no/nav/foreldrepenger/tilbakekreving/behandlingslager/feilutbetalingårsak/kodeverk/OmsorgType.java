package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "OmsorgType")
@DiscriminatorValue(OmsorgType.DISCRIMINATOR)
public class OmsorgType extends Kodeliste{

    public static final String DISCRIMINATOR = "OMSORG_TYPE";

    public static final OmsorgType IKKE_OMSORG_BARN = new OmsorgType("IKKE_OMSORG_BARN");
    public static final OmsorgType IKKE_ALENEOMSORG = new OmsorgType("IKKE_ALENEOMSORG");


    public OmsorgType() {
        // For Hibernate
    }

    public OmsorgType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}