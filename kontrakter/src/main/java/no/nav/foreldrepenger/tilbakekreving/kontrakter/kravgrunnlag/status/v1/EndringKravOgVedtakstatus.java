//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.11.13 at 03:06:23 PM CET
//


package no.nav.foreldrepenger.tilbakekreving.kontrakter.kravgrunnlag.status.v1;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="kravOgVedtakstatus" type="{urn:no:nav:tilbakekreving:status:v1}KravOgVedtakstatus"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "kravOgVedtakstatus"
})
@XmlRootElement(name = "endringKravOgVedtakstatus")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
public class EndringKravOgVedtakstatus {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected KravOgVedtakstatus kravOgVedtakstatus;

    /**
     * Gets the value of the kravOgVedtakstatus property.
     *
     * @return
     *     possible object is
     *     {@link KravOgVedtakstatus }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public KravOgVedtakstatus getKravOgVedtakstatus() {
        return kravOgVedtakstatus;
    }

    /**
     * Sets the value of the kravOgVedtakstatus property.
     *
     * @param value
     *     allowed object is
     *     {@link KravOgVedtakstatus }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKravOgVedtakstatus(KravOgVedtakstatus value) {
        this.kravOgVedtakstatus = value;
    }

}
