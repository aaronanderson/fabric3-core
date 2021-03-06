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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.component.ProducerDefinition;

/**
 * A producer on an instantiated component in the domain.
 */
public class LogicalProducer extends LogicalInvocable {
    private static final long serialVersionUID = 5403855901902189810L;
    private ProducerDefinition definition;
    private List<URI> targets;

    /**
     * Constructor.
     *
     * @param uri        the producer URI
     * @param definition the producer type definition
     * @param parent     the parent component
     */
    public LogicalProducer(URI uri, ProducerDefinition definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
        targets = new ArrayList<URI>();
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
    }

    /**
     * Returns the producer type definition.
     *
     * @return the producer type definition
     */
    public ProducerDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns the configured target channel URIs.
     *
     * @return the configured target channel URIs
     */
    public List<URI> getTargets() {
        return targets;
    }

    /**
     * Adds a configured target channel URI.
     *
     * @param uri the target channel URI
     */
    public void addTarget(URI uri) {
        targets.add(uri);
    }

    /**
     * Adds a configured target channel URIs.
     *
     * @param targets the target channel URIs
     */
    public void addTargets(List<URI> targets) {
        this.targets.addAll(targets);
    }

    public LogicalOperation getStreamOperation() {
        if (operations.size() != 1) {
            throw new IllegalStateException("Invalid number of operations: " + operations.size());
        }
        return operations.get(0);
    }

    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalProducer test = (LogicalProducer) obj;
        return getUri().equals(test.getUri());

    }

    public int hashCode() {
        return getUri().hashCode();
    }

}