package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;


@Entity(name = "Bruker")
@Table(name = "BRUKER")
public class NavBruker extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BRUKER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "sprak_kode", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Språkkode.DISCRIMINATOR + "'"))
    private Språkkode språkkode = Språkkode.UDEFINERT;

    private NavBruker() {
        super(null);
        // For Hibernate
    }

    private NavBruker(final AktørId aktørId, final Språkkode språkkode) {
        super(aktørId);
        this.språkkode = språkkode;
    }

    public static NavBruker opprettNy(final AktørId aktørId, final Språkkode språkkode) {
        return new NavBruker(aktørId, språkkode);
    }

    public Long getId() {
        return id;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }
}
