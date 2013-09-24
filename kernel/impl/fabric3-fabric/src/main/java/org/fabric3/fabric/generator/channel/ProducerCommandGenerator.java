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
package org.fabric3.fabric.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.fabric.command.AttachChannelConnectionCommand;
import org.fabric3.fabric.command.BuildChannelCommand;
import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.fabric.command.DetachChannelConnectionCommand;
import org.fabric3.fabric.command.DisposeChannelCommand;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.spi.generator.ConnectionGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.spi.generator.ChannelDirection.PRODUCER;

/**
 * Generates a command to establish or remove an event channel connection from a producer. Channel build and dispose commands will be generated for producer
 * targets.
 */
public class ProducerCommandGenerator implements CommandGenerator {
    private ConnectionGenerator connectionGenerator;
    private ChannelCommandGenerator channelGenerator;
    private int order;

    public ProducerCommandGenerator(@Reference ConnectionGenerator connectionGenerator,
                                    @Reference ChannelCommandGenerator channelGenerator,
                                    @Property(name = "order") int order) {
        this.connectionGenerator = connectionGenerator;
        this.channelGenerator = channelGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public ChannelConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }

        ChannelConnectionCommand command = new ChannelConnectionCommand();

        for (LogicalProducer producer : component.getProducers()) {
            generateCommand(producer, command, incremental);
        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateCommand(LogicalProducer producer, ChannelConnectionCommand command, boolean incremental) throws GenerationException {
        LogicalComponent<?> component = producer.getParent();
        QName deployable = producer.getParent().getDeployable();
        if (LogicalState.MARKED == component.getState()) {
            Map<LogicalChannel, ChannelDeliveryType> channels = new HashMap<LogicalChannel, ChannelDeliveryType>();
            for (URI uri : producer.getTargets()) {
                LogicalChannel channel = InvocableGeneratorHelper.getChannelInHierarchy(uri, producer);
                DisposeChannelCommand disposeCommand = channelGenerator.generateDispose(channel, deployable, PRODUCER);
                command.addDisposeChannelCommand(disposeCommand);
                channels.put(channel, disposeCommand.getDefinition().getDeliveryType());
            }
            List<PhysicalChannelConnectionDefinition> definitions = connectionGenerator.generateProducer(producer, channels);
            for (PhysicalChannelConnectionDefinition definition : definitions) {
                DetachChannelConnectionCommand connectionCommand = new DetachChannelConnectionCommand(definition);
                command.add(connectionCommand);
            }
        } else if (LogicalState.NEW == component.getState() || !incremental) {
            Map<LogicalChannel, ChannelDeliveryType> channels = new HashMap<LogicalChannel, ChannelDeliveryType>();
            for (URI uri : producer.getTargets()) {
                LogicalChannel channel = InvocableGeneratorHelper.getChannelInHierarchy(uri, producer);
                BuildChannelCommand buildCommand = channelGenerator.generateBuild(channel, deployable, PRODUCER);
                command.addBuildChannelCommand(buildCommand);
                channels.put(channel, buildCommand.getDefinition().getDeliveryType());
            }
            List<PhysicalChannelConnectionDefinition> definitions = connectionGenerator.generateProducer(producer, channels);
            for (PhysicalChannelConnectionDefinition definition : definitions) {
                AttachChannelConnectionCommand connectionCommand = new AttachChannelConnectionCommand(definition);
                command.add(connectionCommand);
            }
        }
    }

}