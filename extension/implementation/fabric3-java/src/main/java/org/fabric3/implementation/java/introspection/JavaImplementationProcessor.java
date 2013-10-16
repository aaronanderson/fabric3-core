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
package org.fabric3.implementation.java.introspection;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.processor.ImplementationProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Adds metadata for Java component implementations.
 */
@EagerInit
public class JavaImplementationProcessor implements ImplementationProcessor {
    private JavaContractProcessor contractProcessor;

    public JavaImplementationProcessor(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public void process(ComponentDefinition<?> definition, IntrospectionContext context) {
        if (!(definition.getImplementation() instanceof JavaImplementation)) {
            return;
        }
        JavaImplementation implementation = (JavaImplementation) definition.getImplementation();
        Object instance = implementation.getInstance();
        if (instance == null) {
            return;
        }
        InjectingComponentType componentType = implementation.getComponentType();
        componentType.setScope("COMPOSITE");

        if (componentType.getServices().isEmpty()) {
            // introspect services if not defined
            addServiceDefinitions(instance, componentType, context);
        }

    }

    private void addServiceDefinitions(Object instance, InjectingComponentType componentType, IntrospectionContext context) {
        Class<?> implClass = instance.getClass();
        Class[] interfaces = implClass.getInterfaces();
        Class<?> serviceInterface;
        if (interfaces.length == 0) {
            serviceInterface = implClass;
        } else if (interfaces.length == 1) {
            serviceInterface = interfaces[0];
        } else {
            MultipleInterfacesSupported failure = new MultipleInterfacesSupported(implClass, componentType);
            context.addError(failure);
            return;
        }

        String serviceName = serviceInterface.getSimpleName();
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context);
        ServiceDefinition serviceDefinition = new ServiceDefinition(serviceName, contract);
        componentType.add(serviceDefinition);
    }
}
