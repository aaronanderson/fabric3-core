/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.introspection.xml.common;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentService;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component service configuration.
 */
public class ComponentServiceLoader extends AbstractExtensibleTypeLoader<ComponentService> {
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName CALLBACK = new QName(SCA_NS, "callback");

    private LoaderHelper loaderHelper;
    private boolean roundTrip;

    public ComponentServiceLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper) {
        super(registry);
        addAttributes("name", "requires", "policySets");
        this.loaderHelper = loaderHelper;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public QName getXMLType() {
        return SERVICE;
    }

    public ComponentService load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }
        ComponentService definition = new ComponentService(name);
        if (roundTrip) {
            definition.enableRoundTrip();
        }

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        validateAttributes(reader, context, definition);

        boolean callback = false;
        while (true) {
            int i = reader.next();
            switch (i) {
                case XMLStreamConstants.START_ELEMENT:
                    Location location = reader.getLocation();
                    callback = CALLBACK.equals(reader.getName());
                    if (callback) {
                        reader.nextTag();
                    }
                    QName elementName = reader.getName();
                    Object type = registry.load(reader, Object.class, context);

                    if (type instanceof ServiceContract) {
                        definition.setServiceContract((ServiceContract) type);
                    } else if (type instanceof BindingDefinition) {
                        BindingDefinition binding = (BindingDefinition) type;
                        if (callback) {
                            if (binding.getName() == null) {
                                // set the default binding name
                                BindingHelper.configureName(binding, definition.getCallbackBindings(), location, context);
                            }
                            boolean check = BindingHelper.checkDuplicateNames(binding, definition.getCallbackBindings(), location, context);
                            if (check) {
                                definition.addCallbackBinding(binding);
                            }

                        } else {
                            if (binding.getName() == null) {
                                // set the default binding name
                                BindingHelper.configureName(binding, definition.getBindings(), location, context);
                            }
                            boolean check = BindingHelper.checkDuplicateNames(binding, definition.getBindings(), location, context);
                            if (check) {
                                definition.addBinding(binding);
                            }
                        }
                    } else if (type instanceof QName) {
                        // external attachment
                        definition.getPolicySets().add((QName) type);
                    } else if (type == null) {
                        // error loading, the element, ignore as an error will have been reported
                        LoaderUtil.skipToEndElement(reader);
                        // check if the last element before the end service tag was at fault, in which case return to avoid reading past the service tag
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && reader.getName().getLocalPart().equals("service")) {
                            return definition;
                        } else {
                            break;
                        }
                    } else {
                        context.addError(new UnrecognizedElement(reader, location, definition));
                        continue;
                    }
                    if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                        throw new AssertionError("Loader must position the cursor to the end element");
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (callback) {
                        callback = false;
                        break;
                    }
                    if (!SERVICE.equals(reader.getName())) {
                        continue;
                    }
                    return definition;
            }
        }
    }

}
