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
package org.fabric3.fabric.deployment.executor;

import org.fabric3.fabric.deployment.command.BuildChannelCommand;
import org.fabric3.fabric.deployment.command.DisposeChannelCommand;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.deployment.command.AttachChannelConnectionCommand;
import org.fabric3.fabric.deployment.command.ChannelConnectionCommand;
import org.fabric3.fabric.deployment.command.DetachChannelConnectionCommand;
import org.fabric3.spi.command.CommandExecutor;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;

/**
 * Establishes and removes event channel connections.
 */
@EagerInit
public class ChannelConnectionCommandExecutor implements CommandExecutor<ChannelConnectionCommand> {
    private CommandExecutorRegistry executorRegistry;

    public ChannelConnectionCommandExecutor(@Reference CommandExecutorRegistry executorRegistry) {
        this.executorRegistry = executorRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(ChannelConnectionCommand.class, this);
    }

    public void execute(ChannelConnectionCommand command) throws ExecutionException {

        // detach must be executed first so attachers can drop connections prior to adding new ones
        for (DetachChannelConnectionCommand detachCommand : command.getDetachCommands()) {
            executorRegistry.execute(detachCommand);
        }

        for (DisposeChannelCommand disposeCommand : command.getDisposeChannelCommands()) {
            executorRegistry.execute(disposeCommand);
        }
        if (command.getDisposeChannelCommands() != null) {
        }

        for (BuildChannelCommand buildCommand : command.getBuildChannelCommands()) {
            executorRegistry.execute(buildCommand);
        }

        for (AttachChannelConnectionCommand attachCommand : command.getAttachCommands()) {
            executorRegistry.execute(attachCommand);
        }

    }
}