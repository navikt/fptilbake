package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingÅrsakType")
@DiscriminatorValue(BehandlingÅrsakType.DISCRIMINATOR)
public class BehandlingÅrsakType extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_AARSAK"; //$NON-NLS-1$

    public static final BehandlingÅrsakType RE_KLAGE_NFP = new BehandlingÅrsakType("RE_KLAGE_NFP"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_KLAGE_KA = new BehandlingÅrsakType("RE_KLAGE_KA"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_VILKÅR = new BehandlingÅrsakType("RE_VILKÅR"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_FORELDELSE = new BehandlingÅrsakType("RE_FORELDELSE"); //$NON-NLS-1$
    public static final BehandlingÅrsakType BERØRT_BEHANDLING = new BehandlingÅrsakType("BERØRT-BEHANDLING");
    public static final BehandlingÅrsakType ETTER_KLAGE = new BehandlingÅrsakType("ETTER_KLAGE");
    public static final BehandlingÅrsakType INFOBREV_BEHANDLING = new BehandlingÅrsakType("INFOBREV_BEHANDLING");
    public static final BehandlingÅrsakType KØET_BEHANDLING = new BehandlingÅrsakType("KØET-BEHANDLING");
    public static final BehandlingÅrsakType RE_ANNET = new BehandlingÅrsakType("RE-ANNET");
    public static final BehandlingÅrsakType RE_AVAB = new BehandlingÅrsakType("RE-AVAB");
    public static final BehandlingÅrsakType RE_BER_GRUN = new BehandlingÅrsakType("RE-BER-GRUN");
    public static final BehandlingÅrsakType RE_DØD = new BehandlingÅrsakType("RE-DØD");
    public static final BehandlingÅrsakType RE_END_FRA_BRUKER = new BehandlingÅrsakType("RE-END-FRA-BRUKER");
    public static final BehandlingÅrsakType RE_END_INNTEKTSMELD = new BehandlingÅrsakType("RE-END-INNTEKTSMELD");
    public static final BehandlingÅrsakType RE_ENDR_BER_GRUN = new BehandlingÅrsakType("RE-ENDR-BER-GRUN");
    public static final BehandlingÅrsakType RE_FEFAKTA = new BehandlingÅrsakType("RE-FEFAKTA");
    public static final BehandlingÅrsakType RE_FRDLING = new BehandlingÅrsakType("RE-FRDLING");
    public static final BehandlingÅrsakType RE_FRIST = new BehandlingÅrsakType("RE-FRIST");
    public static final BehandlingÅrsakType RE_FØDSEL = new BehandlingÅrsakType("RE-FØDSEL");
    public static final BehandlingÅrsakType RE_HENDELSE_DØD_B = new BehandlingÅrsakType("RE-HENDELSE-DØD-B");
    public static final BehandlingÅrsakType RE_HENDELSE_DØD_F = new BehandlingÅrsakType("RE-HENDELSE-DØD-F");
    public static final BehandlingÅrsakType RE_HENDELSE_DØDFØD = new BehandlingÅrsakType("RE-HENDELSE-DØDFØD");
    public static final BehandlingÅrsakType RE_HENDELSE_FØDSEL = new BehandlingÅrsakType("RE-HENDELSE-FØDSEL");
    public static final BehandlingÅrsakType RE_INNTK = new BehandlingÅrsakType("RE-INNTK");
    public static final BehandlingÅrsakType RE_KLAG_M_INNTK = new BehandlingÅrsakType("RE-KLAG-M-INNTK");
    public static final BehandlingÅrsakType RE_KLAG_U_INNTK = new BehandlingÅrsakType("RE-KLAG-U-INNTK");
    public static final BehandlingÅrsakType RE_LOV = new BehandlingÅrsakType("RE-LOV");
    public static final BehandlingÅrsakType RE_MDL = new BehandlingÅrsakType("RE-MDL");
    public static final BehandlingÅrsakType RE_MF = new BehandlingÅrsakType("RE-MF");
    public static final BehandlingÅrsakType RE_MFIP = new BehandlingÅrsakType("RE-MFIP");
    public static final BehandlingÅrsakType RE_OPTJ = new BehandlingÅrsakType("RE-OPTJ");
    public static final BehandlingÅrsakType RE_PRSSL = new BehandlingÅrsakType("RE-PRSSL");
    public static final BehandlingÅrsakType RE_REGISTEROPPL = new BehandlingÅrsakType("RE-REGISTEROPPL");
    public static final BehandlingÅrsakType RE_RGLF = new BehandlingÅrsakType("RE-RGLF");
    public static final BehandlingÅrsakType RE_SATS_REGULERING = new BehandlingÅrsakType("RE-SATS-REGULERING");
    public static final BehandlingÅrsakType RE_SRTB = new BehandlingÅrsakType("RE-SRTB");
    public static final BehandlingÅrsakType RE_TILST_YT_INNVIL = new BehandlingÅrsakType("RE-TILST-YT-INNVIL");
    public static final BehandlingÅrsakType RE_TILST_YT_OPPH = new BehandlingÅrsakType("RE-TILST-YT-OPPH");
    public static final BehandlingÅrsakType RE_YTELSE = new BehandlingÅrsakType("RE-YTELSE");

    public static final BehandlingÅrsakType UDEFINERT = new BehandlingÅrsakType("-"); //$NON-NLS-1$

    BehandlingÅrsakType() {
        //for Hibernate
    }

    private BehandlingÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }


    public static final Set<BehandlingÅrsakType> KLAGE_ÅRSAKER = Set.of(BehandlingÅrsakType.RE_KLAGE_KA, BehandlingÅrsakType.RE_KLAGE_NFP);

}
