package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;

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
