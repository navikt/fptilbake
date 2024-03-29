package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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

    @Convert(converter = Språkkode.KodeverdiConverter.class)
    @Column(name = "sprak_kode", nullable = false)
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

    /**
     * @return Språkkode fra tps
     * @deprecated Ikke bruk denne metoden. Bruk språkkode i grunninformasjonen fra fpsak.
     */
    @Deprecated(since = "03.2023", forRemoval = true)
    public Språkkode getSpråkkode() {
        return språkkode;
    }
}
