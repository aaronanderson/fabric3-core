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
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.Source;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.introspection.xml.common.BindingHelper;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a service definition from a composite.
 */
public class CompositeServiceLoader extends AbstractValidatingTypeLoader<CompositeService> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private LoaderRegistry registry;
    private LoaderHelper loaderHelper;
    private boolean roundTrip;

    public CompositeServiceLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper) {
        this.registry = registry;
        this.loaderHelper = loaderHelper;
        addAttributes("name", "requires", "promote", "policySets");
    }

    @Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public CompositeService load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Service name not specified", startLocation);
            context.addError(failure);
            return null;
        }
        CompositeService service = new CompositeService(name);

        URI uri = parsePromote(service, reader, startLocation, context);
        service.setPromote(uri);

        if (roundTrip) {
            service.enableRoundTrip();
        }


        loaderHelper.loadPolicySetsAndIntents(service, reader, context);

        validateAttributes(reader, context, service);

        boolean callback = false;
        while (true) {
            int i = reader.next();
            switch (i) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                QName elementName = reader.getName();
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    service.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    if (callback) {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, service.getCallbackBindings(), location, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, service.getCallbackBindings(), location, context);
                        if (check) {
                            service.addCallbackBinding(binding);
                        }
                    } else {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, service.getBindings(), location, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, service.getBindings(), location, context);
                        if (check) {
                            service.addBinding(binding);
                        }
                    }
                } else if (type == null) {
                    // there was an error loading the element, ignore it as the errors will have been reported
                    continue;
                } else {
                    context.addError(new UnrecognizedElement(reader, location, service));
                    continue;
                }
                if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                    throw new AssertionError("Loader must position the cursor to the end element");
                }
                break;
            case END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return service;
            }
        }
    }

    private URI parsePromote(CompositeService service, XMLStreamReader reader, Location startLocation, IntrospectionContext context) {
        String name = service.getName();
        String promote = reader.getAttributeValue(null, "promote");
        if (promote == null) {
            MissingPromotion error = new MissingPromotion("Promotion not specified on composite service " + name, startLocation, service);
            context.addError(error);
        }
        URI uri;
        try {
            uri = loaderHelper.parseUri(promote);
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid promote URI specified on service " + name, startLocation, e, service);
            context.addError(error);
            uri = URI.create("");
        }
        if (uri == null) {
            InvalidValue error = new InvalidValue("Empty promote URI specified on service " + name, startLocation, service);
            context.addError(error);
            uri = URI.create("");
        }
        return uri;
    }

}
