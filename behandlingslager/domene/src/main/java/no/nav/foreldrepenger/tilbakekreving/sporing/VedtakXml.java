package no.nav.foreldrepenger.tilbakekreving.sporing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity(name = "VedtakXml")
@Table(name = "VEDTAK_XML_OS")
class VedtakXml {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAK_XML_OS")
    private Long id;

    @Column(name = "behandling_id", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "vedtak_xml", nullable = false, updatable = false)
    @Lob
    private String xml;

    @Column(name = "kvittering_xml", updatable = false)
    @Lob
    private String kvitteringXml;

    private VedtakXml() {
    }

    VedtakXml(Long behandlingId, String xml) {
        this.behandlingId = behandlingId;
        this.xml = xml;
    }

    VedtakXml(Long behandlingId, String xml, String kvitteringXml) {
        this(behandlingId, xml);
        this.kvitteringXml = kvitteringXml;
    }

    void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    void setVedtakXml(String vedtakXml) {
        this.xml = vedtakXml;
    }

    void setKvitteringXml(String kvitteringXml) {
        this.kvitteringXml = kvitteringXml;
    }

    Long getBehandlingId() {
        return behandlingId;
    }

    String getVedtakXml() {
        return xml;
    }

    String getKvitteringXml() {
        return kvitteringXml;
    }
}
