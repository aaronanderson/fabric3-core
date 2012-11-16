/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.introspection.xml.common.InvalidAttributes;
import org.fabric3.introspection.xml.common.InvalidPropertyValue;
import org.fabric3.model.type.component.PropertyMany;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * Loads property values configured on a component.
 */
public class PropertyValueLoader extends AbstractExtensibleTypeLoader<PropertyValue> {
    private static final QName PROPERTY = new QName(Constants.SCA_NS, "property");
    private final LoaderHelper helper;

    public PropertyValueLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper helper) {
        super(registry);
        addAttributes("name", "source", "file", "type", "element", "value", "many");
        this.helper = helper;
    }

    public QName getXMLType() {
        return PROPERTY;
    }

    public PropertyValue load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }

        String source = reader.getAttributeValue(null, "source");
        String file = reader.getAttributeValue(null, "file");
        String typeAttribute = reader.getAttributeValue(null, "type");
        QName type = null;
        if (source != null) {
            LoaderUtil.skipToEndElement(reader);
            PropertyValue value = new PropertyValue(name, source);
            value.setType(type);
            if (typeAttribute != null) {
                try {
                    type = helper.createQName(typeAttribute, reader);
                } catch (InvalidPrefixException e) {
                    InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), startLocation, value);
                    context.addError(error);
                }
            }
            return value;
        } else if (file != null) {
            try {
                URI uri = new URI(file);
                if (!uri.isAbsolute()) {
                    uri = context.getSourceBase().toURI().resolve(uri);
                }
                LoaderUtil.skipToEndElement(reader);
                PropertyValue value = new PropertyValue(name, uri);
                if (typeAttribute != null) {
                    try {
                        type = helper.createQName(typeAttribute, reader);
                    } catch (InvalidPrefixException e) {
                        InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), startLocation, value);
                        context.addError(error);
                    }
                }
                value.setType(type);
                return value;
            } catch (URISyntaxException e) {
                InvalidValue failure = new InvalidValue("File specified for property " + name + " is invalid: " + file, startLocation, e);
                context.addError(failure);
                return null;
            }
        } else {
            return loadInlinePropertyValue(name, reader, startLocation, context);
        }
    }

    private PropertyValue loadInlinePropertyValue(String name, XMLStreamReader reader, Location location, IntrospectionContext context)
            throws XMLStreamException {
        String typeAttribute = reader.getAttributeValue(null, "type");
        String elementAttribute = reader.getAttributeValue(null, "element");
        PropertyMany many = parseMany(reader);
        String valueAttribute = reader.getAttributeValue(null, "value");

        QName type = null;
        QName element = null;

        Document value = helper.loadPropertyValues(reader);

        if (valueAttribute != null) {
            value = helper.loadPropertyValue(valueAttribute);
        }

        PropertyValue propertyValue = new PropertyValue(name, value, many);
        propertyValue.setElement(element);
        propertyValue.setType(type);

        if (valueAttribute != null && value.getDocumentElement().getChildNodes().getLength() > 0) {
            InvalidPropertyValue error =
                    new InvalidPropertyValue("Property value configured using a value attribute and inline: " + name, location, propertyValue);
            context.addError(error);
        }

        if (typeAttribute != null) {
            if (elementAttribute != null) {
                InvalidValue failure = new InvalidValue("Cannot supply both type and element for property: " + name, location);
                context.addError(failure);
            }
            try {
                type = helper.createQName(typeAttribute, reader);
            } catch (InvalidPrefixException e) {
                InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), location, propertyValue);
                context.addError(error);
            }
        } else if (elementAttribute != null) {
            try {
                element = helper.createQName(elementAttribute, reader);
            } catch (InvalidPrefixException e) {
                InvalidAttributes error = new InvalidAttributes("Invalid property element namespace:" + e.getMessage(), location, propertyValue);
                context.addError(error);
            }
        }

        return propertyValue;
    }

    private PropertyMany parseMany(XMLStreamReader reader) {
        String manyAttribute = reader.getAttributeValue(null, "many");
        if (manyAttribute == null) {
            return PropertyMany.NOT_SPECIFIED;
        } else if (manyAttribute.equalsIgnoreCase("true")) {
            return PropertyMany.MANY;
        }
        return PropertyMany.SINGLE;
    }

}
