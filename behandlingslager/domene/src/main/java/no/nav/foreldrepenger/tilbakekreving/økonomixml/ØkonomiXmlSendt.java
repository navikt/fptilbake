package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "OkoXmlSendt")
@Table(name = "OKO_XML_SENDT")
public class ØkonomiXmlSendt extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_XML_SENDT")
    private Long id;

    @Column(name = "behandling_id", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "melding", nullable = false, updatable = false)
    @Lob
    private String xml;

    @Column(name = "kvittering")
    @Lob
    private String kvitteringXml;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "melding_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + MeldingType.DISCRIMINATOR + "'"))
    private MeldingType meldingType;

    private ØkonomiXmlSendt() {
    }

    ØkonomiXmlSendt(Long behandlingId, String xml) {
        this.behandlingId = behandlingId;
        this.xml = xml;
    }

    ØkonomiXmlSendt(Long behandlingId, String xml, String kvitteringXml) {
        this(behandlingId, xml);
        this.kvitteringXml = kvitteringXml;
    }

    void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    void setMelding(String melding) {
        this.xml = melding;
    }

    void setKvittering(String kvittering) {
        this.kvitteringXml = kvittering;
    }

    Long getBehandlingId() {
        return behandlingId;
    }

    public Long getId() {
        return id;
    }

    public String getMelding() {
        return xml;
    }

    public String getKvittering() {
        return kvitteringXml;
    }

    public MeldingType getMeldingType() {
        return meldingType;
    }

    public void setMeldingType(MeldingType meldingType) {
        this.meldingType = meldingType;
    }
}
