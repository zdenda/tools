//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.23 at 03:30:50 PM PST 
//


package com.android.sdklib.repositoryv2.generated.addon.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.android.repository.impl.generated.v1.TypeDetails;
import com.android.sdklib.repositoryv2.IdDisplay;
import com.android.sdklib.repositoryv2.generated.common.v1.IdDisplayType;


/**
 * <p>Java class for mavenType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mavenType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://schemas.android.com/repository/android/common/01}typeDetails"&gt;
 *       &lt;all&gt;
 *         &lt;element name="vendor" type="{http://schemas.android.com/sdk/android/repo/common/01}idDisplayType"/&gt;
 *       &lt;/all&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mavenType", propOrder = {
    "vendor"
})
@SuppressWarnings({
    "override",
    "unchecked"
})
public class MavenType
    extends TypeDetails
    implements com.android.sdklib.repositoryv2.meta.DetailsTypes.MavenType
{

    @XmlElement(required = true)
    protected IdDisplayType vendor;

    /**
     * Gets the value of the vendor property.
     * 
     * @return
     *     possible object is
     *     {@link IdDisplayType }
     *     
     */
    public IdDisplayType getVendor() {
        return vendor;
    }

    /**
     * Sets the value of the vendor property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdDisplayType }
     *     
     */
    public void setVendorInternal(IdDisplayType value) {
        this.vendor = value;
    }

    public void setVendor(IdDisplay value) {
        setVendorInternal(((IdDisplayType) value));
    }

    public ObjectFactory createFactory() {
        return new ObjectFactory();
    }

}
