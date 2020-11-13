//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.11.13 at 03:06:23 PM CET
//


package no.nav.foreldrepenger.tilbakekreving.kontrakter.kravgrunnlag.detalj.v1;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import no.nav.tilbakekreving.typer.v1.Periode;


/**
 *  432 - Detaljert kravgrunnlag periode
 *
 * <p>Java class for DetaljertKravgrunnlagPeriode complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DetaljertKravgrunnlagPeriode"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="periode" type="{urn:no:nav:tilbakekreving:typer:v1}Periode"/&gt;
 *         &lt;element name="belopSkattMnd" type="{urn:no:nav:tilbakekreving:typer:v1}belop"/&gt;
 *         &lt;element name="tilbakekrevingsBelop" type="{urn:no:nav:tilbakekreving:kravgrunnlag:detalj:v1}DetaljertKravgrunnlagBelop" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DetaljertKravgrunnlagPeriode", propOrder = {
    "periode",
    "belopSkattMnd",
    "tilbakekrevingsBelop"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
public class DetaljertKravgrunnlagPeriode {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected Periode periode;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigDecimal belopSkattMnd;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected List<DetaljertKravgrunnlagBelop> tilbakekrevingsBelop;

    /**
     * Gets the value of the periode property.
     *
     * @return
     *     possible object is
     *     {@link Periode }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public Periode getPeriode() {
        return periode;
    }

    /**
     * Sets the value of the periode property.
     *
     * @param value
     *     allowed object is
     *     {@link Periode }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setPeriode(Periode value) {
        this.periode = value;
    }

    /**
     * Gets the value of the belopSkattMnd property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigDecimal getBelopSkattMnd() {
        return belopSkattMnd;
    }

    /**
     * Sets the value of the belopSkattMnd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setBelopSkattMnd(BigDecimal value) {
        this.belopSkattMnd = value;
    }

    /**
     * Gets the value of the tilbakekrevingsBelop property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tilbakekrevingsBelop property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTilbakekrevingsBelop().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DetaljertKravgrunnlagBelop }
     *
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public List<DetaljertKravgrunnlagBelop> getTilbakekrevingsBelop() {
        if (tilbakekrevingsBelop == null) {
            tilbakekrevingsBelop = new ArrayList<DetaljertKravgrunnlagBelop>();
        }
        return this.tilbakekrevingsBelop;
    }

}
