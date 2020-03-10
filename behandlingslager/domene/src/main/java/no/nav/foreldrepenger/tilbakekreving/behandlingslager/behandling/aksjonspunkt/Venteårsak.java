package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;


@Entity(name = "Venteårsak")
@DiscriminatorValue(Venteårsak.DISCRIMINATOR)
public class Venteårsak extends Kodeliste {
    public static final String DISCRIMINATOR = "VENT_AARSAK";

    public static final Venteårsak VENT_PÅ_BRUKERTILBAKEMELDING = new Venteårsak("VENT_PÅ_BRUKERTILBAKEMELDING"); //$NON-NLS-1$
    public static final Venteårsak VENT_PÅ_TILBAKEKREVINGSGRUNNLAG = new Venteårsak("VENT_PÅ_TILBAKEKREVINGSGRUNNLAG"); //$NON-NLS-1$

    public static final Venteårsak AVVENTER_DOKUMENTASJON = new Venteårsak("AVV_DOK"); //$NON-NLS-1$
    public static final Venteårsak VENT_PÅ_SCANNING = new Venteårsak("SCANN"); //$NON-NLS-1$
    public static final Venteårsak UTVIDET_TILSVAR_FRIST = new Venteårsak("UTV_TIL_FRIST"); //$NON-NLS-1$
    public static final Venteårsak VENT_PÅ_ØKONOMI = new Venteårsak("VENT_PÅ_ØKONOMI"); //$NON-NLS-1$
    public static final Venteårsak ENDRE_TILKJENT_YTELSE = new Venteårsak("ENDRE_TILKJENT_YTELSE"); //$NON-NLS-1$

    public static final Venteårsak UDEFINERT = new Venteårsak("-"); //$NON-NLS-1$

    public static boolean venterPåBruker(Venteårsak venteårsak) {
        return VENT_PÅ_BRUKERTILBAKEMELDING.equals(venteårsak) || UTVIDET_TILSVAR_FRIST.equals(venteårsak) || AVVENTER_DOKUMENTASJON.equals(venteårsak);
    }

    public static boolean venterPåØkonomi(Venteårsak venteårsak) {
        return VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(venteårsak) || VENT_PÅ_ØKONOMI.equals(venteårsak);
    }

    public Venteårsak() {
    }

    public Venteårsak(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
