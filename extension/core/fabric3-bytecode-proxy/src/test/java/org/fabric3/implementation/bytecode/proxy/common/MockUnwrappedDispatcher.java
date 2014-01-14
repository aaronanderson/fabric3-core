package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public interface MockUnwrappedDispatcher extends ProxyDispatcher {
   
    public static class MockUnwrappedDispatcherImpl implements MockUnwrappedDispatcher {

        public Object _f3_invoke(int index, Object param) throws Exception {
            return param;
        }
    }
}