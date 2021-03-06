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
package org.fabric3.spi.model.physical;

import javax.xml.namespace.QName;
import java.io.Serializable;

import org.w3c.dom.Document;

/**
 * A property and its resolved values.
 */
public class PhysicalPropertyDefinition implements Serializable {
    private static final long serialVersionUID = -9068366603932114615L;
    private String name;
    private Document value;
    private Object instanceValue;
    private boolean many;
    private QName type;

    public PhysicalPropertyDefinition(String name, Document value, boolean many) {
        this(name, value, many, null);
    }

    public PhysicalPropertyDefinition(String name, Document value, boolean many, QName type) {
        this.name = name;
        this.value = value;
        this.many = many;
        this.type = type;
    }

    public PhysicalPropertyDefinition(String name, Object value, boolean many) {
        this.name = name;
        this.instanceValue = value;
        this.many = many;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property value. Properties may be single-valued or multi-valued. Values are stored as child nodes of the root.
     *
     * @return the property value
     */
    public Document getValue() {
        return value;
    }

    /**
     * Returns the property value as an instance.
     *
     * @return the property value
     */
    public Object getInstanceValue() {
        return instanceValue;
    }

    /**
     * Returns true if the property is multi-valued.
     *
     * @return true if the property is multi-valued
     */
    public boolean isMany() {
        return many;
    }

    /**
     * Returns the optional type.
     *
     * @return the optional type
     */
    public QName getType() {
        return type;
    }
}
