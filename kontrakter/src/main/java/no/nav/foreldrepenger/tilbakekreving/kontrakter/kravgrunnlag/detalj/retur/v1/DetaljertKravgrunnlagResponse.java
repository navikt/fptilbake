//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.11.13 at 03:06:23 PM CET
//


package no.nav.foreldrepenger.tilbakekreving.kontrakter.kravgrunnlag.detalj.retur.v1;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.typer.v1.Mmel;


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
 *         &lt;element name="mmel" type="{urn:no:nav:tilbakekreving:typer:v1}Mmel"/&gt;
 *         &lt;element name="detaljertKravgrunnlag" type="{urn:no:nav:tilbakekreving:kravgrunnlag:detalj:v1}DetaljertKravgrunnlag" minOccurs="0"/&gt;
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
    "mmel",
    "detaljertKravgrunnlag"
})
@XmlRootElement(name = "detaljertKravgrunnlagResponse")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
public class DetaljertKravgrunnlagResponse {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected Mmel mmel;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected DetaljertKravgrunnlag detaljertKravgrunnlag;

    /**
     * Gets the value of the mmel property.
     *
     * @return
     *     possible object is
     *     {@link Mmel }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public Mmel getMmel() {
        return mmel;
    }

    /**
     * Sets the value of the mmel property.
     *
     * @param value
     *     allowed object is
     *     {@link Mmel }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setMmel(Mmel value) {
        this.mmel = value;
    }

    /**
     * Gets the value of the detaljertKravgrunnlag property.
     *
     * @return
     *     possible object is
     *     {@link DetaljertKravgrunnlag }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public DetaljertKravgrunnlag getDetaljertKravgrunnlag() {
        return detaljertKravgrunnlag;
    }

    /**
     * Sets the value of the detaljertKravgrunnlag property.
     *
     * @param value
     *     allowed object is
     *     {@link DetaljertKravgrunnlag }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setDetaljertKravgrunnlag(DetaljertKravgrunnlag value) {
        this.detaljertKravgrunnlag = value;
    }

}
