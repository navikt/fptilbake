package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.BaseEntitet;


@Entity(name = "Fagsak")
@Table(name = "FAGSAK")
public class Fagsak extends BaseEntitet {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(cascade = {CascadeType.ALL}, optional = false)
    @JoinColumn(name = "bruker_id", nullable = false)
    private NavBruker navBruker;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "fagsak_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FagsakStatus.DISCRIMINATOR + "'"))
    private FagsakStatus fagsakStatus = FagsakStatus.DEFAULT;

    /**
     * Offisielt tildelt saksnummer fra GSAK.
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer", unique = true)))
    private Saksnummer saksnummer;


    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Fagsak() {
        // Hibernate
    }

    private Fagsak(long fagsakId, Saksnummer saksnummer, NavBruker bruker) {
        this.id = fagsakId;
        this.saksnummer = saksnummer;
        this.navBruker = bruker;
    }

    public static Fagsak opprettNy(long fagsakId, Saksnummer saksnummer, NavBruker bruker) {
        return new Fagsak(fagsakId, saksnummer, bruker);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public boolean erÅpen() {
        return !getFagsakStatus().equals(FagsakStatus.AVSLUTTET);
    }

    public FagsakStatus getStatus() {
        return getFagsakStatus();
    }

    public void setAvsluttet() {
        oppdaterStatus(FagsakStatus.AVSLUTTET);
    }

    void oppdaterStatus(FagsakStatus status) {
        this.setFagsakStatus(status);
    }

    public NavBruker getNavBruker() {
        return navBruker;
    }

    public AktørId getAktørId() {
        return getNavBruker().getAktørId();
    }

    private FagsakStatus getFagsakStatus() {
        return fagsakStatus;
    }

    private void setFagsakStatus(FagsakStatus fagsakStatus) {
        this.fagsakStatus = fagsakStatus;
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
        return getClass().getSimpleName() + "<" //$NON-NLS-1$
                + (id == null ? "" : "id=" + id + ",") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " bruker=" + navBruker + "," //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer, navBruker);
    }

}
