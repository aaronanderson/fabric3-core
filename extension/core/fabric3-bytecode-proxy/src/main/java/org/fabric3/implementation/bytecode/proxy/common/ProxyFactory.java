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
package org.fabric3.implementation.bytecode.proxy.common;

import java.lang.reflect.Method;
import java.net.URI;

/**
 * Creates a bytecode generated proxy that dispatches to a target.
 * <p/>
 * Bytecode proxies are designed to be more performant than traditional JDK proxies as they dispatch based on a method index.
 */
public interface ProxyFactory {

    /**
     * Creates a proxy.
     *
     * @param classLoaderKey the key of the classloader the proxy is to be created for
     * @param type      	 the interface or class to be proxied
     * @param methods        the sorted list of proxy methods. If multiple proxies are created for a classloader, method order must be the same as proxy
     *                       bytecode is cached.
     * @param dispatcherInt  the dispatcher interface the proxy implements
     * @param dispatcherTmpl the dispatcher class that contains the implemented interface methods that should be copy to the proxy
     * @param wrapped        true if parameters should be wrapped in an array as JDK proxy invocations are
     * @return the proxy instance, which extends the provided dispatcher class
     * @throws ProxyException if there is an error creating the proxy
     */
    <T,D extends ProxyDispatcher> T createProxy(URI classLoaderKey, Class<T> type, Method[] methods,  Class<D> dispatcherInt, Class<? extends D> dispatcherTmpl, boolean wrapped)
            throws ProxyException;

}
