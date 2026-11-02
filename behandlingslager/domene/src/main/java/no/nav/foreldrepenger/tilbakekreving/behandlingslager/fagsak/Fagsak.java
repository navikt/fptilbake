package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;


@Entity(name = "Fagsak")
@Table(name = "FAGSAK")
public class Fagsak extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAGSAK")
    private Long id;

    @ManyToOne(cascade = {CascadeType.ALL}, optional = false)
    @JoinColumn(name = "bruker_id", nullable = false)
    private NavBruker navBruker;

    /**
     * Offisielt tildelt saksnummer fra FPSAK/K9SAK.
     */
    @NaturalId
    @NotNull
    @Convert(converter = Saksnummer.SaksnummerConverter.class)
    @Column(name = "saksnummer", nullable = false, updatable = false, unique = true)
    private Saksnummer saksnummer;


    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = FagsakYtelseType.KodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private FagsakYtelseType fagsakYtelseType = FagsakYtelseType.FORELDREPENGER;

    protected Fagsak() {
        // Hibernate
    }

    private Fagsak(Saksnummer saksnummer, NavBruker bruker, FagsakYtelseType ytelseType) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(bruker, "bruker");
        this.saksnummer = saksnummer;
        this.navBruker = bruker;
        this.fagsakYtelseType = ytelseType;
    }

    // Kun test
    public static Fagsak opprettNy(Saksnummer saksnummer, NavBruker bruker) {
        return new Fagsak(saksnummer, bruker, FagsakYtelseType.FORELDREPENGER);
    }

    public static Fagsak opprettNy(Saksnummer saksnummer, NavBruker bruker, FagsakYtelseType ytelseType) {
        return new Fagsak(saksnummer, bruker, ytelseType);
    }

    public Long getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public NavBruker getNavBruker() {
        return navBruker;
    }

    public AktørId getAktørId() {
        return getNavBruker().getAktørId();
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Fagsak)) {
            return false;
        }
        Fagsak fagsak = (Fagsak) object;
        return Objects.equals(saksnummer, fagsak.saksnummer)
                && Objects.equals(navBruker, fagsak.navBruker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
                + (id == null ? "" : "id=" + id + ",")
                + " bruker=" + navBruker + ","
                + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer, navBruker);
    }

    public void setId(Long id) {
        this.id = id;
    }
}
