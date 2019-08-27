package no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "DokumentMalType")
@DiscriminatorValue(DokumentMalType.DISCRIMINATOR)
public class DokumentMalType extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_MAL_TYPE";

    public static final DokumentMalType INNHENT_DOK = new DokumentMalType("INNHEN");
    public static final DokumentMalType FRITEKST_DOK = new DokumentMalType("FRITKS");
    public static final DokumentMalType VARSEL_DOK = new DokumentMalType("VARS");
    public static final DokumentMalType KORRIGERT_VARSEL_DOK = new DokumentMalType("KORRIGVARS");

    DokumentMalType() {
        // Hibernate trenger default konstruktør
    }

    DokumentMalType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
