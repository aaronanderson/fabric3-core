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
package org.fabric3.implementation.java.runtime;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Builds a Java component from a physical definition.
 */
@EagerInit
public class JavaComponentBuilder extends PojoComponentBuilder<JavaComponentDefinition, JavaComponent> {
    private ScopeRegistry scopeRegistry;
    private ImplementationManagerFactoryBuilder factoryBuilder;

    public JavaComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                @Reference ImplementationManagerFactoryBuilder factoryBuilder,
                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                @Reference PropertyObjectFactoryBuilder propertyBuilder,
                                @Reference ManagementService managementService,
                                @Reference IntrospectionHelper helper,
                                @Reference HostInfo info) {
        super(classLoaderRegistry, propertyBuilder, managementService, helper, info);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
    }

    public JavaComponent build(JavaComponentDefinition definition) throws BuilderException {
        if (definition.getInstance() != null) {
            return buildNonManagedComponent(definition);
        } else {
            return buildManagedComponent(definition);
        }
    }

    public void dispose(JavaComponentDefinition definition, JavaComponent component) throws BuilderException {
        dispose(definition);
    }

    private JavaComponent buildManagedComponent(JavaComponentDefinition definition) throws BuilderException {
        URI uri = definition.getComponentUri();

        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        String scopeName = definition.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);

        // create the InstanceFactoryProvider based on the definition in the model
        ImplementationManagerDefinition managerDefinition = definition.getFactoryDefinition();
        ImplementationManagerFactory factory = factoryBuilder.build(managerDefinition, classLoader);

        createPropertyFactories(definition, factory);

        boolean eager = definition.isEagerInit();

        JavaComponent component = new JavaComponent(uri, factory, scopeContainer, deployable, eager);
        buildContexts(component, factory);
        export(definition, classLoader, component);
        return component;
    }

    private JavaComponent buildNonManagedComponent(JavaComponentDefinition definition) {
        URI componentUri = definition.getComponentUri();
        String scopeName = definition.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);
        Object instance = definition.getInstance();
        NonManagedImplementationManagerFactory factory = new NonManagedImplementationManagerFactory(instance);
        return new JavaComponent(componentUri, factory, scopeContainer, definition.getDeployable(), false);
    }

}
