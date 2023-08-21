package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "FaktaFeilutbetaling")
@Table(name = "FAKTA_FEILUTBETALING")
public class FaktaFeilutbetaling extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAKTA_FEILUTBETALING")
    private Long id;

    @Column(name = "begrunnelse", nullable = false)
    private String begrunnelse;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "faktaFeilutbetaling")
    private List<FaktaFeilutbetalingPeriode> feilutbetaltPerioder = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public List<FaktaFeilutbetalingPeriode> getFeilutbetaltPerioder() {
        return Collections.unmodifiableList(feilutbetaltPerioder);
    }

    public void leggTilFeilutbetaltPeriode(FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode) {
        feilutbetaltPerioder.add(faktaFeilutbetalingPeriode);
    }
}
