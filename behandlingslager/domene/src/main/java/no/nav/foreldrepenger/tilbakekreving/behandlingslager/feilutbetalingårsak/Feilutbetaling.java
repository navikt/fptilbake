package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "Feilutbetaling")
@Table(name = "FEILUTBETALING")
public class Feilutbetaling extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FEILUTBETALING")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feilutbetalinger")
    private List<FeilutbetalingPeriodeÅrsak> feilutbetaltPerioder = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public List<FeilutbetalingPeriodeÅrsak> getFeilutbetaltPerioder() {
        return Collections.unmodifiableList(feilutbetaltPerioder);
    }

    public void leggTilFeilutbetaltPeriode(FeilutbetalingPeriodeÅrsak feilutbetalingPeriodeÅrsak) {
        feilutbetaltPerioder.add(feilutbetalingPeriodeÅrsak);
    }
}
