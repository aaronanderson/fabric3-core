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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.introspection.IntrospectionException;

/**
 * Base class for Exceptions raised during the loading process. Loader implementations should throw a subclass of this to indicate the actual
 * problem.
 */
public class LoaderException extends IntrospectionException {
    public static final int UNDEFINED = -1;
    private static final long serialVersionUID = -7459051598906813461L;
    private final String resourceURI;
    private final int line;
    private final int column;

    public LoaderException(String message, XMLStreamReader reader) {
        super(message);
        Location location = reader.getLocation();
        if (location != null) {
            line = location.getLineNumber();
            column = location.getColumnNumber();
            resourceURI = location.getSystemId();
        } else {
            resourceURI = "system";
            line = -1;
            column = -1;
        }
    }

    public LoaderException(String message, Throwable cause) {
        super(message, cause);
        line = UNDEFINED;
        column = UNDEFINED;
        resourceURI = null;
    }

    /**
     * Returns the location of the resource that was being loaded.
     *
     * @return the location of the resource that was being loaded
     */
    public String getResourceURI() {
        return resourceURI;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" in ");
        builder.append(resourceURI == null ? "unknown" : resourceURI);
        if (line != -1) {
            builder.append(" at ").append(line).append(',').append(column);
        }
        builder.append(": ");
        builder.append(getLocalizedMessage());
        return builder.toString();
    }
}
