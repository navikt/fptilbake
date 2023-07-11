package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "VilkårVurdering")
@Table(name = "VILKAAR")
public class VilkårVurderingEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAAR")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vurderinger")
    private List<VilkårVurderingPeriodeEntitet> perioder = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public List<VilkårVurderingPeriodeEntitet> getPerioder() {
        return Collections.unmodifiableList(perioder);
    }

    public void leggTilPeriode(VilkårVurderingPeriodeEntitet periode) {
        perioder.add(periode);
    }

}
