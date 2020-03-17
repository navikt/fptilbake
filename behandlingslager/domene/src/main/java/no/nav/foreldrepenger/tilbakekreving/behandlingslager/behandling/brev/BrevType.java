package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BrevType")
@DiscriminatorValue(BrevType.DISCRIMINATOR)
public class BrevType extends Kodeliste {

    public static final String DISCRIMINATOR = "BREV_TYPE";

    public static final BrevType VARSEL_BREV = new BrevType("VARSEL");

    public static final BrevType VEDTAK_BREV = new BrevType("VEDTAK");

    public static final BrevType HENLEGGELSE_BREV = new BrevType("HENLEGGELSE");

    public static final BrevType INNHENT_DOKUMENTASJONBREV = new BrevType("INNHENT_DOKUMENTASJON");

    public static final BrevType UDEFINERT = new BrevType("-");

    BrevType(){
        //Hibernate
    }

    private BrevType(String kode){
        super(kode,DISCRIMINATOR);
    }
}

