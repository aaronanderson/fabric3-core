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
package org.fabric3.implementation.pojo.component;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.objectfactory.ComponentObjectFactory;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ComponentException;
import org.fabric3.spi.container.component.InstanceDestructionException;
import org.fabric3.spi.container.component.InstanceInitException;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Base class for Java component implementations.
 */
public abstract class PojoComponent implements ScopedComponent {
    private URI uri;
    private ImplementationManagerFactory factory;
    private ScopeContainer scopeContainer;
    private QName deployable;
    private boolean eager;
    private ImplementationManager implementationManager;
    private URI classLoaderId;
    private MonitorLevel level = MonitorLevel.INFO;
    private AtomicBoolean recreate = new AtomicBoolean(true);
    private Object cachedInstance;

    public PojoComponent(URI componentId, ImplementationManagerFactory factory, ScopeContainer scopeContainer, QName deployable, boolean eager) {
        this.uri = componentId;
        this.factory = factory;
        this.scopeContainer = scopeContainer;
        this.deployable = deployable;
        this.eager = eager;
    }

    public void start() throws ComponentException {
        scopeContainer.register(this);
    }

    public void stop() throws ComponentException {
        implementationManager = null;
        cachedInstance = null;
        scopeContainer.unregister(this);
    }

    public void startUpdate() {
        factory.startUpdate();
    }

    public void endUpdate() {
        factory.endUpdate();
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return deployable;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public String getName() {
        return uri.toString();
    }

    public MonitorLevel getLevel() {
        return level;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    public boolean isEagerInit() {
        return eager;
    }

    public Object getInstance() throws InstanceLifecycleException {
        if (cachedInstance != null) {
            return cachedInstance;
        }
        return scopeContainer.getInstance(this);
    }

    public void releaseInstance(Object instance) throws InstanceDestructionException {
        scopeContainer.releaseInstance(this, instance);
    }

    public Object createInstance() throws ObjectCreationException {
        if (recreate.getAndSet(false)) {
            implementationManager = null;
        }
        Object instance = getImplementationManager().newInstance();
        if (Scope.COMPOSITE == scopeContainer.getScope()) {
            cachedInstance = instance;
        }
        return instance;
    }

    public ObjectFactory<Object> createObjectFactory() {
        return new ComponentObjectFactory(this);
    }

    public void startInstance(Object instance) throws InstanceInitException {
        getImplementationManager().start(instance);
    }

    public void stopInstance(Object instance) throws InstanceDestructionException {
        cachedInstance = null;
        getImplementationManager().stop(instance);
    }

    public void reinject(Object instance) throws InstanceLifecycleException {
        getImplementationManager().reinject(instance);
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public Class<?> getImplementationClass() {
        return factory.getImplementationClass();
    }

    /**
     * Sets an object factory.
     *
     * @param injectable    the InjectableAttribute identifying the component reference, property or context artifact the object factory creates instances for
     * @param objectFactory the object factory
     */
    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory) {
        setObjectFactory(injectable, objectFactory, InjectionAttributes.EMPTY_ATTRIBUTES);
    }

    /**
     * Sets an object factory.
     *
     * @param injectable    the injectable identifying the component reference, property or context artifact the object factory creates instances for
     * @param objectFactory the object factory
     * @param attributes    the injection attributes
     */
    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
        factory.setObjectFactory(injectable, objectFactory, attributes);
        List<Object> instances = scopeContainer.getActiveInstances(this);
        String name = injectable.getName();
        for (Object instance : instances) {
            getImplementationManager().updated(instance, name);
        }
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        recreate.set(true);
    }

    public void removeObjectFactory(Injectable injectable) {
        factory.removeObjectFactory(injectable);
        String name = injectable.getName();
        List<Object> instances = scopeContainer.getActiveInstances(this);
        for (Object instance : instances) {
            getImplementationManager().removed(instance, name);
        }
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        recreate.set(true);
    }

    public ObjectFactory<?> getObjectFactory(Injectable injectable) {
        return factory.getObjectFactory(injectable);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

    private ImplementationManager getImplementationManager() {
        if (implementationManager == null) {
            implementationManager = factory.createManager();
        }
        return implementationManager;
    }

}
