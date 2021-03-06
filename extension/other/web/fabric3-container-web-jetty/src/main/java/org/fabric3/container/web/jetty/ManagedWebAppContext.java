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
package org.fabric3.container.web.jetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.webapp.WebAppContext;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 *
 */
@Management
public class ManagedWebAppContext extends WebAppContext {
    public ManagedWebAppContext(String webAppDir, String contextPath) {
        super(webAppDir, contextPath);
    }

    @ManagementOperation(description = "The web app name")
    public String getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    @ManagementOperation(description = "The web app context path")
    public String getContextPath() {
        return super.getContextPath();
    }

    @Override
    @ManagementOperation(description = "If web app is available")
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    @ManagementOperation(description = "The web app state")
    public String getState() {
        return super.getState();
    }

    @ManagementOperation(description = "Start the web app")
    public void startWebApp() throws Exception {
        super.start();
    }

    @ManagementOperation(description = "Stop the web app")
    public void stopWebApp() throws Exception {
        super.stop();
    }

    @Override
    public String[] getVirtualHosts() {
        return super.getVirtualHosts();
    }

    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        try {
            WebRequestTunnel.setRequest(request);
            super.doHandle(target, baseRequest, request, response);
        } finally {
            WebRequestTunnel.setRequest(null);
            workContext.reset();
        }
    }
}