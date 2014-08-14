
package edu.illinois.i3.htrc.registry.entities.workset;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.illinois.i3.htrc.registry.entities.workset package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Tags_QNAME = new QName("http://registry.htrc.i3.illinois.edu/entities/workset", "tags");
    private final static QName _Comments_QNAME = new QName("http://registry.htrc.i3.illinois.edu/entities/workset", "comments");
    private final static QName _Worksets_QNAME = new QName("http://registry.htrc.i3.illinois.edu/entities/workset", "worksets");
    private final static QName _Volumes_QNAME = new QName("http://registry.htrc.i3.illinois.edu/entities/workset", "volumes");
    private final static QName _Workset_QNAME = new QName("http://registry.htrc.i3.illinois.edu/entities/workset", "workset");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.illinois.i3.htrc.registry.entities.workset
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Tags }
     * 
     */
    public Tags createTags() {
        return new Tags();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link Worksets }
     * 
     */
    public Worksets createWorksets() {
        return new Worksets();
    }

    /**
     * Create an instance of {@link Comment }
     * 
     */
    public Comment createComment() {
        return new Comment();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link Volume }
     * 
     */
    public Volume createVolume() {
        return new Volume();
    }

    /**
     * Create an instance of {@link Comments }
     * 
     */
    public Comments createComments() {
        return new Comments();
    }

    /**
     * Create an instance of {@link Volumes }
     * 
     */
    public Volumes createVolumes() {
        return new Volumes();
    }

    /**
     * Create an instance of {@link Workset }
     * 
     */
    public Workset createWorkset() {
        return new Workset();
    }

    /**
     * Create an instance of {@link WorksetMeta }
     * 
     */
    public WorksetMeta createWorksetMeta() {
        return new WorksetMeta();
    }

    /**
     * Create an instance of {@link WorksetContent }
     * 
     */
    public WorksetContent createWorksetContent() {
        return new WorksetContent();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Tags }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", name = "tags")
    public JAXBElement<Tags> createTags(Tags value) {
        return new JAXBElement<Tags>(_Tags_QNAME, Tags.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Comments }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", name = "comments")
    public JAXBElement<Comments> createComments(Comments value) {
        return new JAXBElement<Comments>(_Comments_QNAME, Comments.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Worksets }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", name = "worksets")
    public JAXBElement<Worksets> createWorksets(Worksets value) {
        return new JAXBElement<Worksets>(_Worksets_QNAME, Worksets.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Volumes }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", name = "volumes")
    public JAXBElement<Volumes> createVolumes(Volumes value) {
        return new JAXBElement<Volumes>(_Volumes_QNAME, Volumes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Workset }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://registry.htrc.i3.illinois.edu/entities/workset", name = "workset")
    public JAXBElement<Workset> createWorkset(Workset value) {
        return new JAXBElement<Workset>(_Workset_QNAME, Workset.class, null, value);
    }

}
