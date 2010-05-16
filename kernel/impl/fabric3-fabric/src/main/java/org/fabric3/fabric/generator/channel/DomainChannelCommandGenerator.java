/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.fabric.generator.channel;

import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalChannel;

/**
 * Creates commands to build and unbuild domain-level channels. Domain-level channels are special-cased as they are not part of a deployed composite.
 * That is, they are contained by the domain composite, which is virtual.
 *
 * @version $Rev$ $Date$
 */
public interface DomainChannelCommandGenerator {

    /**
     * Generates a build command.
     *
     * @param channel     the channel to build
     * @param incremental true if an incremental deployment is being performed
     * @return the command
     * @throws GenerationException if a generation error is encountered
     */
    CompensatableCommand generateBuild(LogicalChannel channel, boolean incremental) throws GenerationException;

    /**
     * Generates an unbuild command.
     *
     * @param channel     the channel to remove
     * @param incremental true if an incremental deployment is being performed
     * @return the command
     * @throws GenerationException if a generation error is encountered
     */
    CompensatableCommand generateUnBuild(LogicalChannel channel, boolean incremental) throws GenerationException;

    /**
     * Generates attach and detach commands for a channel bound to a transport.
     *
     * @param channel     the channel
     * @param incremental true if an incremental deployment is being performed
     * @return the command
     * @throws GenerationException if a generation error is encountered
     */
    ChannelConnectionCommand generateAttachDetach(LogicalChannel channel, boolean incremental) throws GenerationException;

}