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
package org.fabric3.binding.ws.metro.runtime.security;

import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.jaxws.impl.SecurityClientTube;

/**
 * Overrides the standard WSIT security tube behavior for the following:
 * <ul>
 * <li> Introduces a for workaround as described in {@link F3SecurityTubeFactory}.
 * <li> Overrides the default SecurityEnvironment with a Fabric3 implementation
 * </ul>
 */
public class F3SecurityClientTube extends SecurityClientTube {

    public F3SecurityClientTube(ClientTubelineAssemblyContext context, Tube nextTube) {
        super(context, nextTube);
        // override the default security environment with a Fabric3 system service
        secEnv = context.getContainer().getSPI(SecurityEnvironment.class);
    }

    protected F3SecurityClientTube(SecurityClientTube that, TubeCloner cloner) {
        super(that, cloner);
    }

    @Override
    protected void collectPolicies() {
        // workaround for NPE when policies only attached at the WSDL operation level
        spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;
        super.collectPolicies();
    }
}
