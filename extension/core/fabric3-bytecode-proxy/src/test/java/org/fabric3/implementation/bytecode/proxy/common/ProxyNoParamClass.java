package org.fabric3.implementation.bytecode.proxy.common;

public class ProxyNoParamClass {
	
    public String get(){
    	throw new AssertionError("base method should not be invoked");
    }
}
