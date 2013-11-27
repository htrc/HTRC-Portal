
package edu.illinois.i3.htrc.registry.entities.workset;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Worksets complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Worksets">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://registry.htrc.i3.illinois.edu/entities/workset}workset" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Worksets", namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", propOrder = {
    "workset"
})
public class Worksets {

    @XmlElement(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset")
    protected List<Workset> workset;

    /**
     * Gets the value of the workset property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the workset property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWorkset().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Workset }
     * 
     * 
     */
    public List<Workset> getWorkset() {
        if (workset == null) {
            workset = new ArrayList<Workset>();
        }
        return this.workset;
    }

}
