/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.provision;

import java.net.URI;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * Generated metadata used for attaching a reference to a ZeroMQ Socket.
 */
public class ZeroMQTargetDefinition extends PhysicalTargetDefinition {
    private static final long serialVersionUID = 2273519605739325350L;
    private ZeroMQMetadata metadata;
    private URI callbackUri;

    /**
     * Constructor for a reference binding.
     *
     * @param uri      the target URI
     * @param metadata the ZeroMQ metadata to configure the underlying socket
     */
    public ZeroMQTargetDefinition(URI uri, ZeroMQMetadata metadata) {
        this.metadata = metadata;
        setUri(uri);
    }

    /**
     * Constructor for a bidirectional service
     *
     * @param uri         the target URI
     * @param callbackUri the callback URI
     * @param metadata    the ZeroMQ metadata to configure the underlying socket
     */
    public ZeroMQTargetDefinition(URI uri, URI callbackUri, ZeroMQMetadata metadata) {
        this.metadata = metadata;
        setUri(uri);
        this.callbackUri = callbackUri;
    }

    public ZeroMQMetadata getMetadata() {
        return metadata;
    }

    public URI getCallbackUri() {
        return callbackUri;
    }
}
