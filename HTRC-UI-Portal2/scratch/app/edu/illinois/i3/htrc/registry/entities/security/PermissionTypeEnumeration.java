
package edu.illinois.i3.htrc.registry.entities.security;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PermissionTypeEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PermissionTypeEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GET"/>
 *     &lt;enumeration value="PUT"/>
 *     &lt;enumeration value="DELETE"/>
 *     &lt;enumeration value="AUTHORIZE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PermissionTypeEnumeration", namespace = "http://registry.htrc.i3.illinois.edu/model/security")
@XmlEnum
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2013-10-31T01:04:26-04:00", comments = "JAXB RI vJAXB 2.1.10 in JDK 6")
public enum PermissionTypeEnumeration {

    GET,
    PUT,
    DELETE,
    AUTHORIZE;

    public String value() {
        return name();
    }

    public static PermissionTypeEnumeration fromValue(String v) {
        return valueOf(v);
    }

}
