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
package org.fabric3.spi.deployment.generator.channel;

import java.util.List;
import java.util.Map;

import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

/**
 * Generates physical metadata for an event channel connection.
 */
public interface ConnectionGenerator {

    /**
     * Generate event channel connection metadata from a logical producer.
     *
     * @param producer the logical producer
     * @param channels the a map of channels and delivery semantics the consumer is connected to
     * @return the event channel connection metadata
     * @throws GenerationException if a generation error is encountered
     */
    List<PhysicalChannelConnectionDefinition> generateProducer(LogicalProducer producer, Map<LogicalChannel, ChannelDeliveryType> channels)
            throws GenerationException;

    /**
     * Generate event channel connection metadata from a logical consumer.
     *
     * @param consumer the logical consumer
     * @param channels the a map of channels and delivery semantics the consumer is connected to
     * @return the event channel connection metadata
     * @throws GenerationException if a generation error is encountered
     */
    List<PhysicalChannelConnectionDefinition> generateConsumer(LogicalConsumer consumer, Map<LogicalChannel, ChannelDeliveryType> channels)
            throws GenerationException;
}