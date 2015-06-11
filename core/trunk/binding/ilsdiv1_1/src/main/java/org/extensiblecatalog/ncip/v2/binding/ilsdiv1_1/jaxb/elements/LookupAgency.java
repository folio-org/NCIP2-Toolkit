//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-b10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.11 at 02:59:27 PM CEST 
//


package org.extensiblecatalog.ncip.v2.binding.ilsdiv1_1.jaxb.elements;

import java.util.ArrayList;
import java.util.List;
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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.niso.org/2008/ncip}InitiationHeader" minOccurs="0"/>
 *         &lt;element ref="{http://www.niso.org/2008/ncip}AgencyId"/>
 *         &lt;element ref="{http://www.niso.org/2008/ncip}AgencyElementType" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.niso.org/2008/ncip}Ext" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "initiationHeader",
    "agencyId",
    "agencyElementType",
    "ext"
})
@XmlRootElement(name = "LookupAgency")
public class LookupAgency {

    @XmlElement(name = "InitiationHeader")
    protected InitiationHeader initiationHeader;
    @XmlElement(name = "AgencyId", required = true)
    protected SchemeValuePair agencyId;
    @XmlElement(name = "AgencyElementType", required = true)
    protected List<SchemeValuePair> agencyElementType;
    @XmlElement(name = "Ext")
    protected Ext ext;

    /**
     * Gets the value of the initiationHeader property.
     * 
     * @return
     *     possible object is
     *     {@link InitiationHeader }
     *     
     */
    public InitiationHeader getInitiationHeader() {
        return initiationHeader;
    }

    /**
     * Sets the value of the initiationHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitiationHeader }
     *     
     */
    public void setInitiationHeader(InitiationHeader value) {
        this.initiationHeader = value;
    }

    /**
     * Gets the value of the agencyId property.
     * 
     * @return
     *     possible object is
     *     {@link SchemeValuePair }
     *     
     */
    public SchemeValuePair getAgencyId() {
        return agencyId;
    }

    /**
     * Sets the value of the agencyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemeValuePair }
     *     
     */
    public void setAgencyId(SchemeValuePair value) {
        this.agencyId = value;
    }

    /**
     * Gets the value of the agencyElementType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the agencyElementType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAgencyElementType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemeValuePair }
     * 
     * 
     */
    public List<SchemeValuePair> getAgencyElementType() {
        if (agencyElementType == null) {
            agencyElementType = new ArrayList<SchemeValuePair>();
        }
        return this.agencyElementType;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link Ext }
     *     
     */
    public Ext getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ext }
     *     
     */
    public void setExt(Ext value) {
        this.ext = value;
    }

}