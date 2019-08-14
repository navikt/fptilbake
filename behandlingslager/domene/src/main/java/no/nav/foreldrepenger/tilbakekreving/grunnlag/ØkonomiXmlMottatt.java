package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;

/**
 * Ved mottak av xml fra oppdragsystemet mellomlagres XML direkte i denne tabellen.
 * Dette gjøres pga kombinasjonen av
 * 1. når melding er lest fra MQ er den tapt hvis fptilbake feiler før meldingen er lagret til fptilbake sin database
 * 2. konvertering fra meldingen fra MQ til domeneobjekter krever konvertering fra fødselsnummer/D-nr til aktørID
 * 3. konvertering til aktørId krever eksten tjeneste, og denne kan være nede
 */
@Entity(name = "ØkonomiXmlMottatt")
@Table(name = "OKO_XML_MOTTATT")
public class ØkonomiXmlMottatt extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_XML_MOTTATT")
    private Long id;

    @Lob
    @Column(name = "melding", nullable = false)
    private String mottattXml;

    @Column(name = "ekstern_behandling_id")
    private String eksternBehandlingId;

    @Column(name = "sekvens")
    private Long sekvens;

    ØkonomiXmlMottatt() {
        //for hibernate
    }

    public ØkonomiXmlMottatt(String mottattXml) {
        this.mottattXml = mottattXml;
    }

    public String getMottattXml() {
        return mottattXml;
    }

    public Long getId() {
        return id;
    }

    public String getEksternBehandlingId() {
        return eksternBehandlingId;
    }

    public Long getSekvens() {
        return sekvens;
    }

    public void setEksternBehandling(String eksternBehandlingId, long sekvens) {
        this.eksternBehandlingId = eksternBehandlingId;
        this.sekvens = sekvens;
    }
}
