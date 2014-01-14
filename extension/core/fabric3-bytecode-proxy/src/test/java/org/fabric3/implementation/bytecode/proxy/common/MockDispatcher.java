package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public interface MockDispatcher extends ProxyDispatcher {
   
    public void init(boolean[] invoked);
    
    public Object _f3_invoke(int index, Object params) throws Exception;
    
    public boolean [] invoked();
    
    public static class MockDispatcherImpl implements MockDispatcher {
        public boolean[] invoked;

        public void init(boolean[] invoked) {
            this.invoked = invoked;

        }

        public Object _f3_invoke(int index, Object params) throws Exception {
            invoked[index] = true;
            return null;
        }
        
        public boolean[] invoked(){
        	return invoked;
        }
    }
}