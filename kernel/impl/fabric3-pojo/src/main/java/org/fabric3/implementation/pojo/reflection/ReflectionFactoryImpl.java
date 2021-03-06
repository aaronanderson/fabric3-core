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
 */
package org.fabric3.implementation.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.InjectorFactory;
import org.fabric3.implementation.pojo.spi.reflection.InstantiatorFactory;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvokerFactory;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ReflectionFactoryImpl implements ReflectionFactory {

    private InstantiatorFactory instantiatorFactory;
    private InjectorFactory injectorFactory;
    private LifecycleInvokerFactory lifecycleInvokerFactory;
    private ServiceInvokerFactory serviceInvokerFactory;
    private ConsumerInvokerFactory consumerInvokerFactory;

    public ReflectionFactoryImpl(@Reference InstantiatorFactory instantiatorFactory,
                                 @Reference InjectorFactory injectorFactory,
                                 @Reference LifecycleInvokerFactory lifecycleInvokerFactory,
                                 @Reference ServiceInvokerFactory serviceInvokerFactory,
                                 @Reference ConsumerInvokerFactory consumerInvokerFactory) {
        this.instantiatorFactory = instantiatorFactory;
        this.injectorFactory = injectorFactory;
        this.lifecycleInvokerFactory = lifecycleInvokerFactory;
        this.serviceInvokerFactory = serviceInvokerFactory;
        this.consumerInvokerFactory = consumerInvokerFactory;
    }

    @Reference(required = false)
    public void setInstantiatorFactories(List<InstantiatorFactory> factories) {
        for (InstantiatorFactory factory : factories) {
            if (!factory.isDefault() || instantiatorFactory == null) {
                instantiatorFactory = factory;

            }
        }
    }

    @Reference(required = false)
    public void setInjectorFactories(List<InjectorFactory> factories) {
        for (InjectorFactory factory : factories) {
            if (!factory.isDefault() || injectorFactory == null) {
                injectorFactory = factory;
            }
        }
    }

    @Reference(required = false)
    public void setLifecycleInvokerFactories(List<LifecycleInvokerFactory> factories) {
        for (LifecycleInvokerFactory factory : factories) {
            if (!factory.isDefault() || lifecycleInvokerFactory == null) {
                lifecycleInvokerFactory = factory;
            }
        }
    }

    @Reference(required = false)
    public void setServiceInvokerFactories(List<ServiceInvokerFactory> factories) {
        for (ServiceInvokerFactory factory : factories) {
            if (!factory.isDefault() || serviceInvokerFactory == null) {
                serviceInvokerFactory = factory;
            }
        }
    }

    @Reference(required = false)
    public void setConsumerInvokerFactories(List<ConsumerInvokerFactory> factories) {
        for (ConsumerInvokerFactory factory : factories) {
            if (!factory.isDefault() || serviceInvokerFactory == null) {
                consumerInvokerFactory = factory;
            }
        }
    }

    public <T> ObjectFactory<T> createInstantiator(Constructor<T> constructor, ObjectFactory<?>[] parameterFactories) {
        return instantiatorFactory.createInstantiator(constructor, parameterFactories);
    }

    public Injector<?> createInjector(Member member, ObjectFactory<?> parameterFactory) {
        return injectorFactory.createInjector(member, parameterFactory);
    }

    public LifecycleInvoker createLifecycleInvoker(Method method) {
        return lifecycleInvokerFactory.createLifecycleInvoker(method);
    }

    public ServiceInvoker createServiceInvoker(Method method) {
        return serviceInvokerFactory.createInvoker(method);
    }

    public ConsumerInvoker createConsumerInvoker(Method method) {
        return consumerInvokerFactory.createInvoker(method);
    }
}
