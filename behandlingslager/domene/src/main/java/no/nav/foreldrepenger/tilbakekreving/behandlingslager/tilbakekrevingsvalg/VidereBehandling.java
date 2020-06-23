package no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VidereBehandling")
@DiscriminatorValue(VidereBehandling.DISCRIMINATOR)
public class VidereBehandling extends Kodeliste {

    public static final String DISCRIMINATOR = "TILBAKEKR_VIDERE_BEH";

    public static final VidereBehandling TILBAKEKREV_I_INFOTRYGD = new VidereBehandling("TILBAKEKR_INFOTRYGD");
    public static final VidereBehandling TILBAKEKR_OPPRETT = new VidereBehandling("TILBAKEKR_OPPRETT");
    public static final VidereBehandling IGNORER_TILBAKEKREVING = new VidereBehandling("TILBAKEKR_IGNORER");
    public static final VidereBehandling INNTREKK = new VidereBehandling("TILBAKEKR_INNTREKK");
    public static final VidereBehandling TILBAKEKR_OPPDATER = new VidereBehandling("TILBAKEKR_OPPDATER");

    public static final VidereBehandling UDEFINERT = new VidereBehandling("-");

    VidereBehandling(){
        // for hibernate
    }

    private VidereBehandling(String kode){
        super(kode,DISCRIMINATOR);
    }

}
