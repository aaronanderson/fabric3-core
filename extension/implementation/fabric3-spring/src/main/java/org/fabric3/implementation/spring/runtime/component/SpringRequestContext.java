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
package org.fabric3.implementation.spring.runtime.component;

import javax.security.auth.Subject;

import org.fabric3.api.Fabric3RequestContext;
import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.ServiceReference;

/**
 *
 */
public class SpringRequestContext implements Fabric3RequestContext {
    private String name;

    public SpringRequestContext(String name) {
        this.name = name;
    }

    public Subject getSecuritySubject() {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        return workContext.getSubject().getJaasSubject();
    }

    public SecuritySubject getCurrentSubject() {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        return workContext.getSubject();
    }

    public String getServiceName() {
        return name;
    }

    public <B> ServiceReference<B> getServiceReference() {
        throw new UnsupportedOperationException();
    }

    public <CB> CB getCallback() {
        throw new UnsupportedOperationException();
    }

    public <CB> ServiceReference<CB> getCallbackReference() {
        throw new UnsupportedOperationException();
    }

    public <T> T getHeader(Class<T> type, String name) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        return workContext.getHeader(type, name);
    }

    public void setHeader(String name, Object value) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext.setHeader(name, value);
    }

    public void removeHeader(String name) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext.removeHeader(name);
    }
}
