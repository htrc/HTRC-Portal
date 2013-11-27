
package edu.illinois.i3.htrc.registry.entities.workset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Workset complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Workset">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metadata" type="{http://registry.htrc.i3.illinois.edu/entities/workset}WorksetMeta"/>
 *         &lt;element name="content" type="{http://registry.htrc.i3.illinois.edu/entities/workset}WorksetContent" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Workset", namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", propOrder = {
    "metadata",
    "content"
})
public class Workset {

    @XmlElement(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", required = true)
    protected WorksetMeta metadata;
    @XmlElement(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset")
    protected WorksetContent content;

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link WorksetMeta }
     *     
     */
    public WorksetMeta getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorksetMeta }
     *     
     */
    public void setMetadata(WorksetMeta value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link WorksetContent }
     *     
     */
    public WorksetContent getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorksetContent }
     *     
     */
    public void setContent(WorksetContent value) {
        this.content = value;
    }

}
