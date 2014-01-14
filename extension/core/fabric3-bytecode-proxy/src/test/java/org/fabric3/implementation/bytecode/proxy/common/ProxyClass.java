package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public class ProxyClass {

    public void handle(String event){
    	throw new AssertionError("base method should not be invoked");
    }

    protected void handle(Double event){
    	throw new AssertionError("base method should not be invoked");    	
    }
    
    public void handle(Object event){
    	throw new AssertionError("base method should not be invoked");
    }    

}
