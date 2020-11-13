//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.11.13 at 03:06:23 PM CET
//


package no.nav.foreldrepenger.tilbakekreving.kontrakter.kravgrunnlag.tilbakekrevingsvedtak.vedtak.v1;

import java.math.BigDecimal;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 *  443 - Tilbakekrevingsbelop
 *
 * <p>Java class for Tilbakekrevingsbelop complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Tilbakekrevingsbelop"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="kodeKlasse" type="{urn:no:nav:tilbakekreving:typer:v1}KodeKlasse"/&gt;
 *         &lt;element name="belopOpprUtbet" type="{urn:no:nav:tilbakekreving:typer:v1}belop"/&gt;
 *         &lt;element name="belopNy" type="{urn:no:nav:tilbakekreving:typer:v1}belop"/&gt;
 *         &lt;element name="belopTilbakekreves" type="{urn:no:nav:tilbakekreving:typer:v1}belop"/&gt;
 *         &lt;element name="belopUinnkrevd" type="{urn:no:nav:tilbakekreving:typer:v1}belop" minOccurs="0"/&gt;
 *         &lt;element name="belopSkatt" type="{urn:no:nav:tilbakekreving:typer:v1}belop" minOccurs="0"/&gt;
 *         &lt;element name="kodeResultat" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="kodeAarsak" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="kodeSkyld" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Tilbakekrevingsbelop", propOrder = {
    "kodeKlasse",
    "belopOpprUtbet",
    "belopNy",
    "belopTilbakekreves",
    "belopUinnkrevd",
    "belopSkatt",
    "kodeResultat",
    "kodeAarsak",
    "kodeSkyld"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
public class Tilbakekrevingsbelop {

    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kodeKlasse;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigDecimal belopOpprUtbet;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigDecimal belopNy;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigDecimal belopTilbakekreves;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigDecimal belopUinnkrevd;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected BigDecimal belopSkatt;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kodeResultat;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kodeAarsak;
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    protected String kodeSkyld;

    /**
     * Gets the value of the kodeKlasse property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKodeKlasse() {
        return kodeKlasse;
    }

    /**
     * Sets the value of the kodeKlasse property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKodeKlasse(String value) {
        this.kodeKlasse = value;
    }

    /**
     * Gets the value of the belopOpprUtbet property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigDecimal getBelopOpprUtbet() {
        return belopOpprUtbet;
    }

    /**
     * Sets the value of the belopOpprUtbet property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setBelopOpprUtbet(BigDecimal value) {
        this.belopOpprUtbet = value;
    }

    /**
     * Gets the value of the belopNy property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigDecimal getBelopNy() {
        return belopNy;
    }

    /**
     * Sets the value of the belopNy property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setBelopNy(BigDecimal value) {
        this.belopNy = value;
    }

    /**
     * Gets the value of the belopTilbakekreves property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigDecimal getBelopTilbakekreves() {
        return belopTilbakekreves;
    }

    /**
     * Sets the value of the belopTilbakekreves property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setBelopTilbakekreves(BigDecimal value) {
        this.belopTilbakekreves = value;
    }

    /**
     * Gets the value of the belopUinnkrevd property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigDecimal getBelopUinnkrevd() {
        return belopUinnkrevd;
    }

    /**
     * Sets the value of the belopUinnkrevd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setBelopUinnkrevd(BigDecimal value) {
        this.belopUinnkrevd = value;
    }

    /**
     * Gets the value of the belopSkatt property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public BigDecimal getBelopSkatt() {
        return belopSkatt;
    }

    /**
     * Sets the value of the belopSkatt property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setBelopSkatt(BigDecimal value) {
        this.belopSkatt = value;
    }

    /**
     * Gets the value of the kodeResultat property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKodeResultat() {
        return kodeResultat;
    }

    /**
     * Sets the value of the kodeResultat property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKodeResultat(String value) {
        this.kodeResultat = value;
    }

    /**
     * Gets the value of the kodeAarsak property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKodeAarsak() {
        return kodeAarsak;
    }

    /**
     * Sets the value of the kodeAarsak property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKodeAarsak(String value) {
        this.kodeAarsak = value;
    }

    /**
     * Gets the value of the kodeSkyld property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public String getKodeSkyld() {
        return kodeSkyld;
    }

    /**
     * Sets the value of the kodeSkyld property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-11-13T03:06:23+01:00", comments = "JAXB RI v2.3.0")
    public void setKodeSkyld(String value) {
        this.kodeSkyld = value;
    }

}
