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
package org.fabric3.binding.ws.metro.runtime.wire;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.ws.metro.provision.MetroTargetDefinition;
import org.fabric3.binding.ws.metro.runtime.core.CallbackTargetHandler;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.ReferenceCallbackAddressHandler;
import org.fabric3.binding.ws.metro.runtime.core.SOAPMessageHandlerAdapter;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * Base {@link TargetWireAttacher} functionality for web services.
 */
public abstract class AbstractMetroTargetWireAttacher<T extends PhysicalTargetDefinition> implements TargetWireAttacher<T> {
    private BindingHandlerRegistry handlerRegistry;
    private EndpointService endpointService;

    public AbstractMetroTargetWireAttacher(BindingHandlerRegistry handlerRegistry, EndpointService endpointService) {
        this.handlerRegistry = handlerRegistry;
        this.endpointService = endpointService;
    }

    protected List<Handler> createHandlers(MetroTargetDefinition target) {
        if (target.getHandlers().isEmpty() && !target.isBidirectional() && !target.isCallback()) {
            return null;
        }
        List<Handler> handlers = new ArrayList<Handler>();

        if (target.isBidirectional()) {
            ReferenceCallbackAddressHandler callbackHandler = new ReferenceCallbackAddressHandler(target.getCallbackUri(), endpointService);
            handlers.add(callbackHandler);
        }  else if (target.isCallback()) {
            CallbackTargetHandler handler = new CallbackTargetHandler();
            handlers.add(handler);
        }

        for (PhysicalBindingHandlerDefinition handlerDefinition : target.getHandlers()) {
            BindingHandler<SOAPMessage> handler = handlerRegistry.createHandler(SOAPMessage.class, handlerDefinition);
            SOAPMessageHandlerAdapter adaptor = new SOAPMessageHandlerAdapter(handler);
            handlers.add(adaptor);
        }
        return handlers;
    }

}