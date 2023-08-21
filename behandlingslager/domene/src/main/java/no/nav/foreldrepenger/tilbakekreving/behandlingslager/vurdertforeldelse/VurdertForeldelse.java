package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "VurdertForeldelse")
@Table(name = "VURDERT_FORELDELSE")
public class VurdertForeldelse extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VURDERT_FORELDELSE")
    private Long id;

    @OneToMany(mappedBy = "vurdertForeldelse")
    private List<VurdertForeldelsePeriode> vurdertForeldelsePerioder = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public List<VurdertForeldelsePeriode> getVurdertForeldelsePerioder() {
        return Collections.unmodifiableList(vurdertForeldelsePerioder);
    }

    public void leggTilVurderForeldelsePerioder(VurdertForeldelsePeriode vurdertForeldelsePeriode) {
        vurdertForeldelsePerioder.add(vurdertForeldelsePeriode);
    }

}
