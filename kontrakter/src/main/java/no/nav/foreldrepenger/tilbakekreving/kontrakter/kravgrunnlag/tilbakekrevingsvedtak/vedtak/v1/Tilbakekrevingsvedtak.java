//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.11.13 at 03:06:23 PM CET
//


package no.nav.foreldrepenger.tilbakekreving.kontrakter.kravgrunnlag.tilbakekrevingsvedtak.vedtak.v1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 *  441 - Tilbakekrevingsvedtak
 *
 * <p>Java class for Tilbakekrevingsvedtak complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Tilbakekrevingsvedtak"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="kodeAksjon" type="{urn:no:nav:tilbakekreving:typer:v1}KodeAksjon"/&gt;
 *         &lt;element name="vedtakId" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *         &lt;element name="datoVedtakFagsystem" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="kodeHjemmel" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="renterBeregnes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="enhetAnsvarlig" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="kontrollfelt" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="saksbehId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tilbakekrevingsperiode" type="{urn:no:nav:tilbakekreving:tilbakekrevingsvedtak:vedtak:v1}Tilbakekrevingsperiode" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Tilbakekrevingsvedtak", propOrder = {
    "kodeAksjon",
    "vedtakId",
    "datoVedtakFagsystem",
    "kodeHjemmel",
    "renterBeregnes",
    "enhetAnsvarlig",
    "kontrollfelt",
    "saksbehId",
    "tilbakekrevingsperiode"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
public class Tilbakekrevingsvedtak {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kodeAksjon;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigInteger vedtakId;
    @XmlSchemaType(name = "date")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected XMLGregorianCalendar datoVedtakFagsystem;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kodeHjemmel;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String renterBeregnes;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String enhetAnsvarlig;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kontrollfelt;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String saksbehId;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected List<Tilbakekrevingsperiode> tilbakekrevingsperiode;

    /**
     * Gets the value of the kodeAksjon property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKodeAksjon() {
        return kodeAksjon;
    }

    /**
     * Sets the value of the kodeAksjon property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKodeAksjon(String value) {
        this.kodeAksjon = value;
    }

    /**
     * Gets the value of the vedtakId property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigInteger getVedtakId() {
        return vedtakId;
    }

    /**
     * Sets the value of the vedtakId property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setVedtakId(BigInteger value) {
        this.vedtakId = value;
    }

    /**
     * Gets the value of the datoVedtakFagsystem property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public XMLGregorianCalendar getDatoVedtakFagsystem() {
        return datoVedtakFagsystem;
    }

    /**
     * Sets the value of the datoVedtakFagsystem property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setDatoVedtakFagsystem(XMLGregorianCalendar value) {
        this.datoVedtakFagsystem = value;
    }

    /**
     * Gets the value of the kodeHjemmel property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKodeHjemmel() {
        return kodeHjemmel;
    }

    /**
     * Sets the value of the kodeHjemmel property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKodeHjemmel(String value) {
        this.kodeHjemmel = value;
    }

    /**
     * Gets the value of the renterBeregnes property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getRenterBeregnes() {
        return renterBeregnes;
    }

    /**
     * Sets the value of the renterBeregnes property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setRenterBeregnes(String value) {
        this.renterBeregnes = value;
    }

    /**
     * Gets the value of the enhetAnsvarlig property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getEnhetAnsvarlig() {
        return enhetAnsvarlig;
    }

    /**
     * Sets the value of the enhetAnsvarlig property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setEnhetAnsvarlig(String value) {
        this.enhetAnsvarlig = value;
    }

    /**
     * Gets the value of the kontrollfelt property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKontrollfelt() {
        return kontrollfelt;
    }

    /**
     * Sets the value of the kontrollfelt property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKontrollfelt(String value) {
        this.kontrollfelt = value;
    }

    /**
     * Gets the value of the saksbehId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getSaksbehId() {
        return saksbehId;
    }

    /**
     * Sets the value of the saksbehId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setSaksbehId(String value) {
        this.saksbehId = value;
    }

    /**
     * Gets the value of the tilbakekrevingsperiode property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tilbakekrevingsperiode property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTilbakekrevingsperiode().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Tilbakekrevingsperiode }
     *
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public List<Tilbakekrevingsperiode> getTilbakekrevingsperiode() {
        if (tilbakekrevingsperiode == null) {
            tilbakekrevingsperiode = new ArrayList<Tilbakekrevingsperiode>();
        }
        return this.tilbakekrevingsperiode;
    }

}
