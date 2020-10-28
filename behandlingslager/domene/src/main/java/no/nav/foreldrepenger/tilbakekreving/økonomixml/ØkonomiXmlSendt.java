package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

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

    @Convert(converter = MeldingType.KodeverdiConverter.class)
    @Column(name = "melding_type", nullable = false)
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

    public Long getBehandlingId() {
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
