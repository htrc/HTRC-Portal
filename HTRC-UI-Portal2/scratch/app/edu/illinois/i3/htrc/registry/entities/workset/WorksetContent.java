
package edu.illinois.i3.htrc.registry.entities.workset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WorksetContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WorksetContent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://registry.htrc.i3.illinois.edu/entities/workset}volumes"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorksetContent", namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", propOrder = {
    "volumes"
})
public class WorksetContent {

    @XmlElement(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", required = true)
    protected Volumes volumes;

    /**
     * Gets the value of the volumes property.
     * 
     * @return
     *     possible object is
     *     {@link Volumes }
     *     
     */
    public Volumes getVolumes() {
        return volumes;
    }

    /**
     * Sets the value of the volumes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Volumes }
     *     
     */
    public void setVolumes(Volumes value) {
        this.volumes = value;
    }

}
