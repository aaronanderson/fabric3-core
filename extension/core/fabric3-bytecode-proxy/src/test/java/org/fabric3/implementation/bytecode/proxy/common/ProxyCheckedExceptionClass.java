package org.fabric3.implementation.bytecode.proxy.common;

import java.io.IOException;

/**
 *
 */
public class ProxyCheckedExceptionClass {

    public String handle(String event) throws IOException{
    	throw new AssertionError("base method should not be invoked");
    }


}
