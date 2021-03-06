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
package org.fabric3.implementation.spring.api;

import java.util.Collections;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.JpaResolutionException;
import org.fabric3.jpa.api.PersistenceOverrides;

import static org.fabric3.implementation.spring.api.SpringConstants.EMF_RESOLVER;

/**
 * Integrates Fabric3 EntityManagerFactory parsing with Spring. This class can be configured in an end-user Spring application context to make entity
 * manager factories created by Fabric3 available to Spring.
 * <p/>
 * An example configuration is as follows:
 * <pre>
 *          &lt;bean id="EntityManagerFactory" class="org.fabric3.implementation.spring.api.Fabric3EntityManagerFactoryBean"&gt;
 *              &lt;property name="persistenceUnitName" value="loanApplication"/&gt;
 *          &lt;/bean&gt;
 * </pre>
 * The persistence unit name must be specified. Note that the datasource will be introspected from the <code>persistence.xml</code> file and should
 * not be set as a bean property.
 */
public class Fabric3EntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {
    private static final long serialVersionUID = 3488984443450640577L;

    private EntityManagerFactoryResolver resolver;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        // Resolve the service responsible for building and caching EntityManagerFactory instances.
        // This is set in the parent application context owned by the Spring SCA component
        this.resolver = (EntityManagerFactoryResolver) beanFactory.getBean(EMF_RESOLVER);
    }

    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        if (resolver != null) {
            try {
                // note persistence overrides are not supported for Spring
                String unitName = getPersistenceUnitName();
                PersistenceOverrides overrides = new PersistenceOverrides(unitName, Collections.<String, String>emptyMap());
                return resolver.resolve(unitName, overrides, getBeanClassLoader());
            } catch (JpaResolutionException e) {
                throw new PersistenceException(e);
            }
        }
        return super.createNativeEntityManagerFactory();
    }
}
