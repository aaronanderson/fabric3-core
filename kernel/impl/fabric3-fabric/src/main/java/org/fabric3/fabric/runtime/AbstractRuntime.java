/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.fabric.runtime;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import javax.management.MBeanServer;
import javax.xml.namespace.QName;

import org.fabric3.contribution.MetaDataStoreImpl;
import org.fabric3.contribution.ProcessorRegistryImpl;
import org.fabric3.fabric.channel.ChannelConnectionImpl;
import org.fabric3.fabric.channel.ChannelImpl;
import org.fabric3.fabric.channel.ChannelManagerImpl;
import org.fabric3.fabric.channel.EventStreamImpl;
import org.fabric3.fabric.channel.SyncFanOutHandler;
import org.fabric3.fabric.classloader.ClassLoaderRegistryImpl;
import org.fabric3.fabric.cm.ComponentManagerImpl;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.lcm.LogicalComponentManagerImpl;
import org.fabric3.fabric.repository.RepositoryImpl;
import org.fabric3.host.Names;
import static org.fabric3.host.Names.RUNTIME_DOMAIN_CHANNEL_URI;
import org.fabric3.host.Namespaces;
import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.repository.Repository;
import org.fabric3.host.repository.RepositoryException;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.monitor.runtime.JDKMonitorProxyService;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.RegistrationException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRuntime implements Fabric3Runtime, RuntimeServices {
    private HostInfo hostInfo;
    private MonitorProxyService monitorService;
    private LogicalComponentManager logicalComponentManager;
    private ComponentManager componentManager;
    private ChannelManager channelManager;
    private ScopeContainer scopeContainer;
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore metaDataStore;
    private ScopeRegistry scopeRegistry;
    private MBeanServer mbServer;
    private MonitorEventDispatcher dispatcher;
    private Repository repository;
    private Level level = Level.INFO;

    protected AbstractRuntime(RuntimeConfiguration configuration) {
        hostInfo = configuration.getHostInfo();
        mbServer = configuration.getMBeanServer();
        dispatcher = configuration.getDispatcher();
        repository = configuration.getRepository();
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    public MonitorProxyService getMonitorservice() {
        return monitorService;
    }

    public MBeanServer getMBeanServer() {
        return mbServer;
    }

    public String getName() {
        return Names.RUNTIME_NAME;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void boot() throws InitializationException {
        logicalComponentManager = new LogicalComponentManagerImpl();
        componentManager = new ComponentManagerImpl();
        channelManager = new ChannelManagerImpl();
        QName deployable = new QName(Namespaces.CORE, "boot");
        dispatcher.start();
        registerChannel(deployable);
        classLoaderRegistry = new ClassLoaderRegistryImpl();
        ProcessorRegistry processorRegistry = new ProcessorRegistryImpl();
        metaDataStore = new MetaDataStoreImpl(processorRegistry);
        monitorService = new JDKMonitorProxyService(this, channelManager);
        ScopeContainerMonitor monitor;
        try {
            monitor = monitorService.createMonitor(ScopeContainerMonitor.class, Names.RUNTIME_DOMAIN_CHANNEL_URI);
        } catch (MonitorCreationException e) {
            throw new InitializationException(e);
        }
        scopeContainer = new CompositeScopeContainer(monitor);
        scopeContainer.start();
        scopeRegistry = new ScopeRegistryImpl();
        scopeRegistry.register(scopeContainer);
        if (repository == null) {
            // if the runtime has not been configured with a repository, create one
            repository = createRepository();
        }
    }

    private void registerChannel(QName deployable) throws InitializationException {
        SyncFanOutHandler handler = new SyncFanOutHandler();
        Channel channel = new ChannelImpl(RUNTIME_DOMAIN_CHANNEL_URI, deployable, handler);
        ChannelConnection connection = new ChannelConnectionImpl();
        EventStream stream = new EventStreamImpl(new PhysicalEventStreamDefinition("dispatcher"));
        stream.addHandler(new DispatcherWrapper(dispatcher));
        connection.addEventStream(stream);
        channel.subscribe(URI.create("Fabric3RuntimeDispatcher"), connection);
        try {
            channelManager.register(channel);
        } catch (
                RegistrationException e) {
            throw new InitializationException(e);
        }
    }

    public void destroy() throws ShutdownException {
        // destroy system components
        WorkContext workContext = new WorkContext();
        scopeContainer.stopAllContexts(workContext);
        try {
            repository.shutdown();
        } catch (RepositoryException e) {
            throw new ShutdownException(e);
        }
        dispatcher.stop();
    }

    public <I> I getComponent(Class<I> service, URI uri) {
        if (RuntimeServices.class.equals(service)) {
            return service.cast(this);
        }
        AtomicComponent<?> component = (AtomicComponent<?>) componentManager.getComponent(uri);
        if (component == null) {
            return null;
        }

        WorkContext workContext = new WorkContext();
        WorkContext oldContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            InstanceWrapper<?> wrapper = scopeContainer.getWrapper(component, workContext);
            return service.cast(wrapper.getInstance());
        } catch (InstanceLifecycleException e) {
            // this is an error with the runtime and not something that is recoverable
            throw new AssertionError(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }

    public LogicalComponentManager getLogicalComponentManager() {
        return logicalComponentManager;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public ClassLoaderRegistry getClassLoaderRegistry() {
        return classLoaderRegistry;
    }

    public MetaDataStore getMetaDataStore() {
        return metaDataStore;
    }

    public ScopeRegistry getScopeRegistry() {
        return scopeRegistry;
    }

    public Repository getRepository() {
        return repository;
    }

    /**
     * Creates a default repository which may be overriden by Runtime subclasses.
     *
     * @return an initialized repository
     * @throws InitializationException if an error is encountered initializing a repository
     */
    protected Repository createRepository() throws InitializationException {
        try {
            RepositoryImpl repository = new RepositoryImpl(hostInfo);
            repository.init();
            return repository;
        } catch (IOException e) {
            throw new InitializationException(e);
        } catch (RepositoryException e) {
            throw new InitializationException(e);
        }
    }

}
