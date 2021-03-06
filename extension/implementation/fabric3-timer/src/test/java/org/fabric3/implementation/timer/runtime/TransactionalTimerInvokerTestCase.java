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
package org.fabric3.implementation.timer.runtime;

import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopeContainer;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class TransactionalTimerInvokerTestCase extends TestCase {
    private InvokerMonitor monitor;
    private TimerInstance instance;

    @SuppressWarnings({"unchecked"})
    public void testInvoke() throws Exception {
        instance.run();

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.commit();

        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        TransactionalTimerInvoker invoker = new TransactionalTimerInvoker(component, tm, monitor);
        invoker.run();
        EasyMock.verify(instance, tm);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testRollbackRuntimeException() throws Exception {
        instance.run();
        EasyMock.expectLastCall().andThrow(new RuntimeException());

        TransactionManager tm = EasyMock.createMock(TransactionManager.class);
        tm.begin();
        tm.rollback();

        TimerComponent component = createComponent(instance);

        EasyMock.replay(instance, tm);

        TransactionalTimerInvoker invoker = new TransactionalTimerInvoker(component, tm, monitor);
        try {
            invoker.run();
            fail();
        } catch (ServiceRuntimeException e) {
            // expected
        }
        EasyMock.verify(instance, tm);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        monitor = EasyMock.createNiceMock(InvokerMonitor.class);

        instance = EasyMock.createMock(TimerInstance.class);
    }

    @SuppressWarnings({"unchecked"})
    private TimerComponent createComponent(TimerInstance instance) throws Exception {
        ScopeContainer container = EasyMock.createMock(ScopeContainer.class);
        EasyMock.expect(container.getScope()).andReturn(Scope.COMPOSITE);
        EasyMock.expect(container.getInstance(EasyMock.isA(TimerComponent.class))).andReturn(instance);
        container.releaseInstance(EasyMock.isA(TimerComponent.class), EasyMock.eq(instance));

        ImplementationManagerFactory factory = EasyMock.createMock(ImplementationManagerFactory.class);
        EasyMock.expect(factory.getImplementationClass()).andReturn((Class) TimerInstance.class);

        EasyMock.replay(container, factory);

        return new TimerComponent(null, null, null, TimerInstance.class, false, factory, container, null, null, null, null, null, true);
    }

    private interface TimerInstance extends Runnable {
        long nextInterval();
    }

}

