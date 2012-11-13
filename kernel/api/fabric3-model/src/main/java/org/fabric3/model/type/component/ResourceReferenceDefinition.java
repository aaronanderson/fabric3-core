/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.model.type.component;

import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.contract.ServiceContract;

/**
 * A reference to a system resource contained by a component.
 */
public class ResourceReferenceDefinition extends ModelObject {
    private static final long serialVersionUID = 4241666632750146304L;

    private String name;
    private ComponentType parent;
    private boolean optional;
    private ServiceContract serviceContract;

    public ResourceReferenceDefinition(String name, ServiceContract serviceContract, boolean optional) {
        this.name = name;
        this.serviceContract = serviceContract;
        this.optional = optional;
    }

    /**
     * The name of the resource
     *
     * @return the name of the resource
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent component type of this resource reference.
     *
     * @return the parent component type
     */
    public ComponentType getParent() {
        return parent;
    }

    /**
     * Sets the parent component type of this resource reference.
     *
     * @param parent the parent component type
     */
    public void setParent(ComponentType parent) {
        this.parent = parent;
    }

    /**
     * If true, the resource is optional.
     *
     * @return true if the resource is optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Returns the service contract for the resource.
     *
     * @return the service contract
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

}
