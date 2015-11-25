//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.23 at 03:30:50 PM PST 
//


package com.android.sdklib.repositoryv2.generated.addon.v1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.android.sdklib.repositoryv2.generated.common.v1.LibraryType;


/**
 * 
 *                 libraries provided by this addon
 *             
 * 
 * <p>Java class for librariesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="librariesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="library" type="{http://schemas.android.com/sdk/android/repo/common/01}libraryType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "librariesType", propOrder = {
    "library"
})
@SuppressWarnings({
    "override",
    "unchecked"
})
public class LibrariesType
    extends com.android.sdklib.repositoryv2.meta.DetailsTypes.AddonDetailsType.Libraries
{

    @XmlElement(required = true)
    protected List<LibraryType> library;

    /**
     * Gets the value of the library property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the library property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLibrary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LibraryType }
     * 
     * 
     */
    public List<LibraryType> getLibraryInternal() {
        if (library == null) {
            library = new ArrayList<LibraryType>();
        }
        return this.library;
    }

    public List<com.android.sdklib.repositoryv2.meta.DetailsTypes.Library> getLibrary() {
        return ((List) getLibraryInternal());
    }

    public ObjectFactory createFactory() {
        return new ObjectFactory();
    }

}
