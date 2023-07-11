package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "ØkonomiXmlMottattArkiv")
@Table(name = "OKO_XML_MOTTATT_ARKIV")
public class ØkonomiXmlMottattArkiv extends BaseEntitet {

    @Id
    private Long id;

    @Lob
    @Column(name = "melding", nullable = false)
    private String mottattXml;

    ØkonomiXmlMottattArkiv() {
        // for hibernate
    }

    public ØkonomiXmlMottattArkiv(Long id, String mottattXml) {
        this.id = id;
        this.mottattXml = mottattXml;
    }

    public Long getId() {
        return id;
    }

    public String getMottattXml() {
        return mottattXml;
    }
}
