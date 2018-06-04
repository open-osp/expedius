
package org.oscarehr.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for parseHL7 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="parseHL7"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="savedPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fileId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="labType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parseHL7", propOrder = {
    "className",
    "savedPath",
    "fileId",
    "labType"
})
public class ParseHL7 {

    protected String className;
    protected String savedPath;
    protected int fileId;
    protected String labType;

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the savedPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSavedPath() {
        return savedPath;
    }

    /**
     * Sets the value of the savedPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSavedPath(String value) {
        this.savedPath = value;
    }

    /**
     * Gets the value of the fileId property.
     * 
     */
    public int getFileId() {
        return fileId;
    }

    /**
     * Sets the value of the fileId property.
     * 
     */
    public void setFileId(int value) {
        this.fileId = value;
    }

    /**
     * Gets the value of the labType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabType() {
        return labType;
    }

    /**
     * Sets the value of the labType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabType(String value) {
        this.labType = value;
    }

}
