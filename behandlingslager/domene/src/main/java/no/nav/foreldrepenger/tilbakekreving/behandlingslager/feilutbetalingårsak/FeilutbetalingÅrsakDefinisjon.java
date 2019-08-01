package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "FeilutbetalingÅrsakDefinisjon")
@Table(name = "FEILUTBETALING_AARSAK_DEF")
//TODO PFP-8580 Flytt til kodeverk
public class FeilutbetalingÅrsakDefinisjon extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FEILUTBETALING_AARSAK_DEF")
    private Long id;

    @Column(name = "aarsak",nullable = false,updatable = false)
    private String årsak;

    FeilutbetalingÅrsakDefinisjon(){
        //For CDI
    }

    public Long getId() {
        return id;
    }

    public String getÅrsak() {
        return årsak;
    }

    public void setÅrsak(String årsak) {
        this.årsak = årsak;
    }
}
