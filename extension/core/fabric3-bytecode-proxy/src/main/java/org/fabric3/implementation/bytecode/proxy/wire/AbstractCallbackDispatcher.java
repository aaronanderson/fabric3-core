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
package org.fabric3.implementation.bytecode.proxy.wire;

import org.fabric3.implementation.bytecode.proxy.common.ProxyDispatcher;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * Abstract {@link ProxyDispatcher} for handling callback invocations. Concrete classes must implement a strategy for mapping the callback target chain for the
 * invoked callback operation.
 */
public abstract class AbstractCallbackDispatcher implements ProxyDispatcher {

    protected static Object invoke(InvocationChain chain, Object args, WorkContext workContext) throws Throwable {
        // Pop the callback reference as we move back in the request stack. When the invocation is made on the callback target, the same call callback reference
        // state will be present as existed when the initial forward request to this proxy's instance was dispatched to. Consequently,
        // CallbackReference#getForwardCorrelaltionId() will return the correlation id for the callback target.
        CallbackReference callbackReference = workContext.popCallbackReference();

        Interceptor headInterceptor = chain.getHeadInterceptor();

        // send the invocation down the wire
        Message message = MessageCache.getAndResetMessage();
        message.setBody(args);
        message.setWorkContext(workContext);
        try {
            // dispatch the wire down the chain and get the response
            Message response;
            try {
                response = headInterceptor.invoke(message);
            } catch (ServiceUnavailableException e) {
                // simply rethrow ServiceUnavailableExceptions
                throw e;
            } catch (RuntimeException e) {
                // wrap other exceptions raised by the runtime
                throw new ServiceUnavailableException(e);
            }

            // handle response from the application, returning or throwing is as appropriate
            Object body = response.getBody();
            if (response.isFault()) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            message.reset();
            // push the call callbackReference for this component instance back onto the stack
            workContext.addCallbackReference(callbackReference);
        }
    }

}