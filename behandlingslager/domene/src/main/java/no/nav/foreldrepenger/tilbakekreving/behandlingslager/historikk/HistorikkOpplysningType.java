package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkOpplysningType")
@DiscriminatorValue(HistorikkOpplysningType.DISCRIMINATOR)
public class HistorikkOpplysningType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_OPPLYSNING_TYPE"; //$NON-NLS-1$

    public static final HistorikkOpplysningType UDEFINIERT = new HistorikkOpplysningType("-");
    public static final HistorikkOpplysningType FODSELSDATO = new HistorikkOpplysningType("FODSELSDATO");
    public static final HistorikkOpplysningType PERIODE_FOM = new HistorikkOpplysningType("PERIODE_FOM");
    public static final HistorikkOpplysningType PERIODE_TOM = new HistorikkOpplysningType("PERIODE_TOM");
    public static final HistorikkOpplysningType TILBAKEKREVING_OPPFYLT_BEGRUNNELSE = new HistorikkOpplysningType("TILBAKEKREVING_OPPFYLT_BEGRUNNELSE");
    public static final HistorikkOpplysningType SÆRLIG_GRUNNER_BEGRUNNELSE = new HistorikkOpplysningType("SÆRLIG_GRUNNER_BEGRUNNELSE");
    public static final HistorikkOpplysningType KRAVGRUNNLAG_VEDTAK_ID = new HistorikkOpplysningType("KRAVGRUNNLAG_VEDTAK_ID");
    public static final HistorikkOpplysningType KRAVGRUNNLAG_STATUS = new HistorikkOpplysningType("KRAVGRUNNLAG_STATUS");

    public HistorikkOpplysningType() {
        //
    }

    public HistorikkOpplysningType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
