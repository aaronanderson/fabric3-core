package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public class ProxyReturnClass {

    public String handle(String event){
    	throw new AssertionError("base method should not be invoked");
    }


}
