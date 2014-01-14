package org.fabric3.implementation.bytecode.proxy.common;

//Extracted to separate file to test inlining outer class template 
public class MockEchoDispatcherImpl implements MockEchoDispatcher {

    public Object _f3_invoke(int index, Object args) throws Throwable {
        return ((Object[]) args)[0];

    }

}