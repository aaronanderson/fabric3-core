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
package org.fabric3.fabric.deployment.command;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.fabric3.spi.command.CompensatableCommand;

/**
 * Used to establish or dispose a channel connection. This may include provisioning the channel (or disposing it) to a runtime where the producer or consumer is
 * hosted.
 */
public class ChannelConnectionCommand implements CompensatableCommand {
    private static final long serialVersionUID = 8746788639966402901L;

    private List<BuildChannelCommand> buildCommands;
    private List<DisposeChannelCommand> disposeCommands;

    private List<AttachChannelConnectionCommand> attachCommands;
    private List<DetachChannelConnectionCommand> detachCommands;

    public ChannelConnectionCommand() {
        attachCommands = new ArrayList<AttachChannelConnectionCommand>();
        detachCommands = new ArrayList<DetachChannelConnectionCommand>();
        buildCommands = new ArrayList<BuildChannelCommand>();
        disposeCommands = new ArrayList<DisposeChannelCommand>();
    }

    public ChannelConnectionCommand getCompensatingCommand() {
        // return the commands in reverse order
        ChannelConnectionCommand compensating = new ChannelConnectionCommand();

        for (BuildChannelCommand command : buildCommands) {
            compensating.addDisposeChannelCommand(command.getCompensatingCommand());
        }
        for (DisposeChannelCommand command : disposeCommands) {
            compensating.addBuildChannelCommand(command.getCompensatingCommand());
        }
        if (!attachCommands.isEmpty()) {
            ListIterator<AttachChannelConnectionCommand> iterator = attachCommands.listIterator(attachCommands.size());
            while (iterator.hasPrevious()) {
                AttachChannelConnectionCommand command = iterator.previous();
                DetachChannelConnectionCommand compensatingCommand = command.getCompensatingCommand();
                compensating.add(compensatingCommand);
            }
        }
        if (!detachCommands.isEmpty()) {
            ListIterator<DetachChannelConnectionCommand> iterator = detachCommands.listIterator(detachCommands.size());
            while (iterator.hasPrevious()) {
                DetachChannelConnectionCommand command = iterator.previous();
                AttachChannelConnectionCommand compensatingCommand = command.getCompensatingCommand();
                compensating.add(compensatingCommand);
            }
        }
        return compensating;
    }

    public List<BuildChannelCommand> getBuildChannelCommands() {
        return buildCommands;
    }

    public void addBuildChannelCommand(BuildChannelCommand command) {
        this.buildCommands.add(command);
    }

    public List<DisposeChannelCommand> getDisposeChannelCommands() {
        return disposeCommands;
    }

    public void addDisposeChannelCommand(DisposeChannelCommand command) {
        this.disposeCommands.add(command);
    }

    public List<AttachChannelConnectionCommand> getAttachCommands() {
        return attachCommands;
    }

    public void add(AttachChannelConnectionCommand command) {
        attachCommands.add(command);
    }

    public List<DetachChannelConnectionCommand> getDetachCommands() {
        return detachCommands;
    }

    public void add(DetachChannelConnectionCommand command) {
        detachCommands.add(command);
    }
}
