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
*/
package org.fabric3.monitor.appender.component;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a {@link ComponentAppenderDefinition} from an appender configuration.
 */
@EagerInit
public class ComponentAppenderLoader extends AbstractValidatingTypeLoader<ComponentAppenderDefinition> {
    private static final QName SCA_TYPE = new QName(Constants.SCA_NS, "appender.component");
    private static final QName F3_TYPE = new QName(org.fabric3.api.Namespaces.F3, "appender.component");

    private LoaderRegistry registry;

    public ComponentAppenderLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        // register under both namespaces
        registry.registerLoader(F3_TYPE, this);
        registry.registerLoader(SCA_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(F3_TYPE);
        registry.unregisterLoader(SCA_TYPE);
    }

    public ComponentAppenderDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        addAttributes("name");
        validateAttributes(reader, context);
        String componentName = reader.getAttributeValue(null, "name");
        if (componentName == null) {
            ComponentAppenderDefinition definition = new ComponentAppenderDefinition("");
            MissingAttribute error = new MissingAttribute("A component name must be defined for the appender", reader.getLocation(), definition);
            context.addError(error);
            return definition;
        }
        return new ComponentAppenderDefinition(componentName);
    }
}
