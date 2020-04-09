package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkBaseEntitet;

@Entity(name = "ØkonomiXmlMottattArkiv")
@Table(name = "OKO_XML_MOTTATT_ARKIV")
public class ØkonomiXmlMottattArkiv extends KodeverkBaseEntitet {

    @Id
    private Long id;

    @Lob
    @Column(name = "melding", nullable = false)
    private String mottattXml;

     ØkonomiXmlMottattArkiv() {
         // for hibernate
    }

    public ØkonomiXmlMottattArkiv(Long id, String mottattXml){
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
