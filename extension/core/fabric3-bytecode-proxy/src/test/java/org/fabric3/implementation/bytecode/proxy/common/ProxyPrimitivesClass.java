package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public class ProxyPrimitivesClass {

    public boolean getBooleanPrimitive(boolean param){
    	throw new AssertionError("base method should not be invoked");
    }

    public byte getBytePrimitive(byte param){
    	throw new AssertionError("base method should not be invoked");
    }

    public short getShortPrimitive(short param){
    	throw new AssertionError("base method should not be invoked");
    }

    public int getIntPrimitive(int param){
    	throw new AssertionError("base method should not be invoked");
    }

    public long getLongPrimitive(long param){
    	throw new AssertionError("base method should not be invoked");
    }

    public float getFloatPrimitive(float param){
    	throw new AssertionError("base method should not be invoked");
    }

    public double getDoublePrimitive(double param){
    	throw new AssertionError("base method should not be invoked");
    }

    public int[] getIntArray(int[] params){
    	throw new AssertionError("base method should not be invoked");
    }

}
