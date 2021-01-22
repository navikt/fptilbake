package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkBaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Ved mottak av xml fra oppdragsystemet mellomlagres XML direkte i denne tabellen.
 * Dette gjøres pga kombinasjonen av
 * 1. når melding er lest fra MQ er den tapt hvis fptilbake feiler før meldingen er lagret til fptilbake sin database
 * 2. konvertering fra meldingen fra MQ til domeneobjekter krever konvertering fra fødselsnummer/D-nr til aktørID
 * 3. konvertering til aktørId krever eksten tjeneste, og denne kan være nede
 */
@Entity(name = "ØkonomiXmlMottatt")
@Table(name = "OKO_XML_MOTTATT")
public class ØkonomiXmlMottatt extends KodeverkBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_XML_MOTTATT")
    private Long id;

    @Lob
    @Column(name = "melding", nullable = false)
    private String mottattXml;

    private Henvisning henvisning;

    @Column(name = "saksnummer")
    private String saksnummer;

    @Column(name = "sekvens")
    private Long sekvens;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "tilkoblet", nullable = false)
    private boolean tilkoblet = false;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

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

    public Henvisning getHenvisning() {
        return henvisning;
    }

    public Long getSekvens() {
        return sekvens;
    }

    public boolean isTilkoblet() {
        return tilkoblet;
    }

    public void setHenvisning(Henvisning henvisning, long sekvens) {
        this.henvisning = henvisning;
        this.sekvens = sekvens;
    }

    public void lagTilkobling() {
        this.tilkoblet = true;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }
}
