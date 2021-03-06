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
package org.fabric3.spi.model.physical;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.net.URI;

/**
 * Used to attach the source side of a channel connection. The source may be a producer, channel binding or channel.
 */
public class PhysicalConnectionSourceDefinition implements Serializable {
    private static final long serialVersionUID = 3395589699751449558L;

    public static final int NO_SEQUENCE = 0;

    private URI uri;
    private int sequence = NO_SEQUENCE;
    private QName deployable;
    private URI classLoaderId;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the sequence a consumer should be passed events, if supported by the channel type.
     *
     * @return the sequence a consumer should be passed events, if supported by the channel type
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence a consumer should be passed events, if supported by the channel type.
     *
     * @param sequence the sequence a consumer should be passed events, if supported by the channel type.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }


    public QName getDeployable() {
        return deployable;
    }

    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

}