package org.fabric3.implementation.bytecode.proxy.common;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fabric3.implementation.bytecode.proxy.common.MockCheckedExceptionDispatcher.MockCheckedExceptionDispatcherImpl;
import org.fabric3.implementation.bytecode.proxy.common.MockDispatcher.MockDispatcherImpl;
import org.fabric3.implementation.bytecode.proxy.common.MockReturningDispatcher.MockReturningDispatcherImpl;
import org.fabric3.implementation.bytecode.proxy.common.MockRuntimeExceptionDispatcher.MockRuntimeExceptionDispatcherImpl;
import org.fabric3.implementation.bytecode.proxy.common.MockUnwrappedDispatcher.MockUnwrappedDispatcherImpl;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class ProxyFactoryImplTestCase extends TestCase {
    public static final URI URI = java.net.URI.create("test");
    private ProxyFactoryImpl factory;

    public void testInterfaceProxyDispatch() throws Exception {
        Method[] methods = ProxyInterface.class.getMethods();

        boolean[] invoked = new boolean[]{false, false, false};
        ProxyInterface proxy = factory.createProxy(URI, ProxyInterface.class, methods, MockDispatcher.class, MockDispatcherImpl.class, true);
        ((MockDispatcher) proxy).init(invoked);

        proxy.handle("event");
        proxy.handle(1d);
        proxy.handle(new Object());

        MockDispatcher handler = (MockDispatcher) proxy;
        assertTrue(handler.invoked()[0]);
        assertTrue(handler.invoked()[1]);
        assertTrue(handler.invoked()[2]);
    }
    
    
    public void testClassProxyDispatch() throws Exception {
        Method[] methods = ProxyClass.class.getDeclaredMethods();

        boolean[] invoked = new boolean[]{false, false, false};
        ProxyClass proxy = factory.createProxy(URI, ProxyClass.class, methods, MockDispatcher.class, MockDispatcherImpl.class, true);
        ((MockDispatcher) proxy).init(invoked);

        proxy.handle("event");
        proxy.handle(1d);
        proxy.handle(new Object());

        MockDispatcher handler = (MockDispatcher) proxy;
        assertTrue(handler.invoked()[0]);
        assertTrue(handler.invoked()[1]);
        assertTrue(handler.invoked()[2]);
    }

    
    public void testInterfaceReturnDispatch() throws Exception {
        Method[] methods = ProxyReturnInterface.class.getMethods();

        ProxyReturnInterface proxy = factory.createProxy(URI, ProxyReturnInterface.class, methods, MockReturningDispatcher.class, MockReturningDispatcherImpl.class, true);
        assertEquals("test", proxy.handle("test"));
    }
    
    public void testClassReturnDispatch() throws Exception {
        Method[] methods = ProxyReturnClass.class.getMethods();

        ProxyReturnClass proxy = factory.createProxy(URI, ProxyReturnClass.class, methods, MockReturningDispatcher.class, MockReturningDispatcherImpl.class, true);
        assertEquals("test", proxy.handle("test"));
    }

    public void testInterfaceCheckedExceptionDispatch() throws Exception {
        Method[] methods = ProxyCheckedExceptionInterface.class.getMethods();

        ProxyCheckedExceptionInterface proxy = factory.createProxy(URI,
                                                                   ProxyCheckedExceptionInterface.class,
                                                                   methods,
                                                                   MockCheckedExceptionDispatcher.class,
                                                                   MockCheckedExceptionDispatcherImpl.class,
                                                                   true);
        try {
            proxy.handle("test");
            fail();
        } catch (IOException e) {
            // expected
        }
    }
    
    public void testClassCheckedExceptionDispatch() throws Exception {
        Method[] methods = ProxyCheckedExceptionClass.class.getMethods();

        ProxyCheckedExceptionClass proxy = factory.createProxy(URI,
                                                                   ProxyCheckedExceptionClass.class,
                                                                   methods,
                                                                   MockCheckedExceptionDispatcher.class,
                                                                   MockCheckedExceptionDispatcherImpl.class,
                                                                   true);
        try {
            proxy.handle("test");
            fail();
        } catch (IOException e) {
            // expected
        }
    }

    public void testInterfaceCheckedRuntimeExceptionDispatch() throws Exception {
        Method[] methods = ProxyCheckedExceptionInterface.class.getMethods();

        ProxyRuntimeExceptionInterface proxy = factory.createProxy(URI,
                                                                   ProxyRuntimeExceptionInterface.class,
                                                                   methods,
                                                                   MockRuntimeExceptionDispatcher.class,
                                                                   MockRuntimeExceptionDispatcherImpl.class,
                                                                   true);
        try {
            proxy.handle("test");
            fail();
        } catch (ServiceRuntimeException e) {
            // expected
        }
    }
    
    public void testClassCheckedRuntimeExceptionDispatch() throws Exception {
        Method[] methods = ProxyCheckedExceptionClass.class.getMethods();

        ProxyCheckedExceptionClass proxy = factory.createProxy(URI,
        														   ProxyCheckedExceptionClass.class,
                                                                   methods,
                                                                   MockRuntimeExceptionDispatcher.class,
                                                                   MockRuntimeExceptionDispatcherImpl.class,
                                                                   true);
        try {
            proxy.handle("test");
            fail();
        } catch (ServiceRuntimeException e) {
            // expected
        }
    }

    public void testInterfaceNoParamDispatch() throws Exception {
        Method[] methods = ProxyNoParamInterface.class.getMethods();

        ProxyNoParamInterface proxy = factory.createProxy(URI, ProxyNoParamInterface.class, methods, MockReturningDispatcher.class, MockReturningDispatcherImpl.class, true);
        assertEquals("test", proxy.get());
    }
    
    public void testClassNoParamDispatch() throws Exception {
        Method[] methods = ProxyNoParamClass.class.getMethods();

        ProxyNoParamClass proxy = factory.createProxy(URI, ProxyNoParamClass.class, methods, MockReturningDispatcher.class, MockReturningDispatcherImpl.class, true);
        assertEquals("test", proxy.get());
    }

    public void testIntefacePrimitivesReturnDispatch() throws Exception {
        Method[] methods = ProxyPrimitivesInterface.class.getMethods();

        ProxyPrimitivesInterface proxy = factory.createProxy(URI, ProxyPrimitivesInterface.class, methods, MockEchoDispatcher.class, MockEchoDispatcherImpl.class, true);

        assertEquals(Double.MAX_VALUE, proxy.getDoublePrimitive(Double.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, proxy.getIntPrimitive(Integer.MAX_VALUE));
        assertEquals(Short.MAX_VALUE, proxy.getShortPrimitive(Short.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, proxy.getLongPrimitive(Long.MAX_VALUE));
        assertEquals(Float.MAX_VALUE, proxy.getFloatPrimitive(Float.MAX_VALUE));
        assertEquals(Byte.MAX_VALUE, proxy.getBytePrimitive(Byte.MAX_VALUE));
        assertTrue(proxy.getBooleanPrimitive(true));
    }
    
    public void testClassPrimitivesReturnDispatch() throws Exception {
        Method[] methods = ProxyPrimitivesClass.class.getMethods();

        ProxyPrimitivesClass proxy = factory.createProxy(URI, ProxyPrimitivesClass.class, methods, MockEchoDispatcher.class, MockEchoDispatcherImpl.class, true);

        assertEquals(Double.MAX_VALUE, proxy.getDoublePrimitive(Double.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, proxy.getIntPrimitive(Integer.MAX_VALUE));
        assertEquals(Short.MAX_VALUE, proxy.getShortPrimitive(Short.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, proxy.getLongPrimitive(Long.MAX_VALUE));
        assertEquals(Float.MAX_VALUE, proxy.getFloatPrimitive(Float.MAX_VALUE));
        assertEquals(Byte.MAX_VALUE, proxy.getBytePrimitive(Byte.MAX_VALUE));
        assertTrue(proxy.getBooleanPrimitive(true));
    }


    public void testInterfaceUnwrappedDispatch() throws Exception {
        Method[] methods = ProxyReturnInterface.class.getMethods();
        ProxyReturnInterface proxy = factory.createProxy(URI, ProxyReturnInterface.class, methods, MockUnwrappedDispatcher.class, MockUnwrappedDispatcherImpl.class, false);

        assertEquals("event", proxy.handle("event"));
    }
    
    public void testClassUnwrappedDispatch() throws Exception {
        Method[] methods = ProxyReturnClass.class.getMethods();
        ProxyReturnClass proxy = factory.createProxy(URI, ProxyReturnClass.class, methods, MockUnwrappedDispatcher.class, MockUnwrappedDispatcherImpl.class, false);

        assertEquals("event", proxy.handle("event"));
    }
    
    
    public void setUp() throws Exception {
        super.setUp();

        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader());
        EasyMock.replay(registry);

        factory = new ProxyFactoryImpl(registry);
    }

}

