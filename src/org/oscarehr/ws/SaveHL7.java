
package org.oscarehr.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for saveHL7 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="saveHL7"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="savedFileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="providerNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveHL7", propOrder = {
    "savedFileName",
    "providerNumber"
})
public class SaveHL7 {

    protected String savedFileName;
    protected String providerNumber;

    /**
     * Gets the value of the savedFileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSavedFileName() {
        return savedFileName;
    }

    /**
     * Sets the value of the savedFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSavedFileName(String value) {
        this.savedFileName = value;
    }

    /**
     * Gets the value of the providerNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderNumber() {
        return providerNumber;
    }

    /**
     * Sets the value of the providerNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderNumber(String value) {
        this.providerNumber = value;
    }

}
