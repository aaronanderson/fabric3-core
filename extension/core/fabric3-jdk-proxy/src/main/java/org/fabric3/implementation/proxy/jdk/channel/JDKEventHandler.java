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
package org.fabric3.implementation.proxy.jdk.channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.component.InstanceInvocationException;

/**
 * Dispatches from a proxy to an {@link EventStream}.
 */
public final class JDKEventHandler implements InvocationHandler {
    private EventStream stream;

    public JDKEventHandler(EventStream stream) {
        this.stream = stream;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null || Object.class.equals(method.getDeclaringClass())) {
            // events have at least one arg
            handleProxyMethod(method);
            return null;
        }
        stream.getHeadHandler().handle(args[0], true);
        return null;
    }

    private Object handleProxyMethod(Method method) throws InstanceInvocationException {
        if (method.getParameterTypes().length == 0 && "toString".equals(method.getName())) {
            return "[Proxy - " + Integer.toHexString(hashCode()) + "]";
        } else if (method.getDeclaringClass().equals(Object.class) && "equals".equals(method.getName())) {
            throw new UnsupportedOperationException();
        } else if (Object.class.equals(method.getDeclaringClass()) && "hashCode".equals(method.getName())) {
            return hashCode();
        }
        String op = method.getName();
        throw new InstanceInvocationException("Operation not configured: " + op);
    }

}