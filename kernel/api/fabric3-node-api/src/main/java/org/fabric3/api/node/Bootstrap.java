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
package org.fabric3.api.node;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * Bootstraps an interface to a service fabric.
 */
public class Bootstrap {
    private static final String FABRIC_CLASS = "org.fabric3.node.DefaultFabric";

    /**
     * Bootstraps the fabric interface using the default configuration.
     *
     * @throws FabricException if an exception occurs during bootstrap
     */
    public static Fabric initialize() throws FabricException {
        return initialize(null);
    }

    /**
     * Bootstraps the fabric interface using the provided configuration.
     *
     * @param url the system configuration
     * @throws FabricException if an exception occurs during bootstrap
     */
    public static Fabric initialize(URL url) throws FabricException {
        try {
            ClassLoader bootstrapClassLoader = Bootstrap.class.getClassLoader();
            // instantiate the Fabric API implementation class
            Class<?> implClass = Class.forName(FABRIC_CLASS, true, bootstrapClassLoader);
            return (Fabric) implClass.getConstructor(URL.class).newInstance(url);
        } catch (ClassNotFoundException e) {
            // programming error
            throw new FabricException(e);
        } catch (IllegalAccessException e) {
            // programming error
            throw new FabricException(e);
        } catch (InstantiationException e) {
            // programming error
            throw new FabricException(e);
        } catch (NoSuchMethodException e) {
            // programming error
            throw new FabricException(e);
        } catch (InvocationTargetException e) {
            // programming error
            throw new FabricException(e.getTargetException());
        } catch (Exception e) {
            throw new FabricException(e);
        }
    }


}
