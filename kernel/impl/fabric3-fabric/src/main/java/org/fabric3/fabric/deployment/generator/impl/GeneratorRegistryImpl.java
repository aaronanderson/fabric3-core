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
package org.fabric3.fabric.deployment.generator.impl;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.fabric.deployment.generator.GeneratorNotFoundException;
import org.fabric3.fabric.deployment.generator.GeneratorRegistry;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.api.model.type.component.ResourceReferenceDefinition;
import org.fabric3.spi.deployment.generator.binding.BindingGenerator;
import org.fabric3.spi.deployment.generator.component.ComponentGenerator;
import org.fabric3.spi.deployment.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.deployment.generator.channel.EventStreamHandlerGenerator;
import org.fabric3.spi.deployment.generator.wire.InterceptorGenerator;
import org.fabric3.spi.deployment.generator.resource.ResourceGenerator;
import org.fabric3.spi.deployment.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class GeneratorRegistryImpl implements GeneratorRegistry {
    private Map<Class<?>, ComponentGenerator<?>> componentGenerators = new HashMap<Class<?>, ComponentGenerator<?>>();
    private Map<Class<?>, BindingGenerator<?>> bindingGenerators = new HashMap<Class<?>, BindingGenerator<?>>();
    private Map<Class<?>, ConnectionBindingGenerator<?>> connectionBindingGenerators = new ConcurrentHashMap<Class<?>, ConnectionBindingGenerator<?>>();
    private Map<QName, InterceptorGenerator> interceptorGenerators = new HashMap<QName, InterceptorGenerator>();
    private Map<QName, EventStreamHandlerGenerator> handlerGenerators = new HashMap<QName, EventStreamHandlerGenerator>();
    private Map<Class<?>, ResourceReferenceGenerator<?>> resourceReferenceGenerators = new HashMap<Class<?>, ResourceReferenceGenerator<?>>();
    private Map<Class<?>, ResourceGenerator<?>> resourceGenerators = new HashMap<Class<?>, ResourceGenerator<?>>();

    @Reference(required = false)
    public void setComponentGenerators(Map<Class<?>, ComponentGenerator<?>> componentGenerators) {
        this.componentGenerators = componentGenerators;
    }

    @Reference(required = false)
    public void setBindingGenerators(Map<Class<?>, BindingGenerator<?>> bindingGenerators) {
        this.bindingGenerators = bindingGenerators;
    }

    @Reference(required = false)
    public void setConnectionBindingGenerators(Map<Class<?>, ConnectionBindingGenerator<?>> bindingGenerators) {
        this.connectionBindingGenerators = bindingGenerators;
    }

    @Reference(required = false)
    public void setResourceReferenceGenerators(Map<Class<?>, ResourceReferenceGenerator<?>> resourceReferenceGenerators) {
        this.resourceReferenceGenerators = resourceReferenceGenerators;
    }

    @Reference(required = false)
    public void setInterceptorGenerators(Map<QName, InterceptorGenerator> interceptorGenerators) {
        this.interceptorGenerators = interceptorGenerators;
    }

    @Reference(required = false)
    public void setResourceGenerators(Map<Class<?>, ResourceGenerator<?>> resourceGenerators) {
        this.resourceGenerators = resourceGenerators;
    }

    @Reference(required = false)
    public void setHandlerGenerators(Map<QName, EventStreamHandlerGenerator> handlerGenerators) {
        this.handlerGenerators = handlerGenerators;
    }

    public <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    public <T extends ResourceReferenceDefinition> void register(Class<T> clazz, ResourceReferenceGenerator<T> generator) {
        resourceReferenceGenerators.put(clazz, generator);
    }

    public <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator<T> generator) {
        bindingGenerators.put(clazz, generator);
    }

    @SuppressWarnings("unchecked")
    public <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        ComponentGenerator<LogicalComponent<T>> generator = (ComponentGenerator<LogicalComponent<T>>) componentGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    @SuppressWarnings("unchecked")
    public <T extends BindingDefinition> BindingGenerator<T> getBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        BindingGenerator<T> generator = (BindingGenerator<T>) bindingGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BindingDefinition> ConnectionBindingGenerator<T> getConnectionBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        ConnectionBindingGenerator<T> generator = (ConnectionBindingGenerator<T>) connectionBindingGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceReferenceDefinition> ResourceReferenceGenerator<T> getResourceReferenceGenerator(Class<T> clazz)
            throws GeneratorNotFoundException {
        ResourceReferenceGenerator<T> generator = (ResourceReferenceGenerator<T>) resourceReferenceGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    public InterceptorGenerator getInterceptorGenerator(QName extensionName) throws GeneratorNotFoundException {
        InterceptorGenerator generator = interceptorGenerators.get(extensionName);
        if (generator == null) {
            throw new GeneratorNotFoundException(extensionName);
        }
        return generator;
    }

    public EventStreamHandlerGenerator getEventStreamHandlerGenerator(QName extensionName) throws GeneratorNotFoundException {
        EventStreamHandlerGenerator generator = handlerGenerators.get(extensionName);
        if (generator == null) {
            throw new GeneratorNotFoundException(extensionName);
        }
        return generator;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends ResourceDefinition> ResourceGenerator<T> getResourceGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        ResourceGenerator<T> generator = (ResourceGenerator<T>) resourceGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

}
