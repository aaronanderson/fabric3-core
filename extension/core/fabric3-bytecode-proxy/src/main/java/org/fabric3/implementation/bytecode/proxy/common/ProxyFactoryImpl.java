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
 *
 */
package org.fabric3.implementation.bytecode.proxy.common;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.classloader.BytecodeClassLoader;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.builder.classloader.ClassLoaderListener;
import org.oasisopen.sca.annotation.Reference;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Implementation that uses ASM for bytecode generation.
 */
public class ProxyFactoryImpl implements ProxyFactory, ClassLoaderListener {

	public static final Set<Method> OBJECT_METHODS = Collections.unmodifiableSet(new HashSet<Method>(Arrays.asList(java.lang.Object.class.getMethods()))); 
	
	private ClassLoaderRegistry classLoaderRegistry;

	private Map<URI, BytecodeClassLoader> classLoaderCache = new HashMap<URI, BytecodeClassLoader>();

	public ProxyFactoryImpl(@Reference ClassLoaderRegistry classLoaderRegistry) {
		this.classLoaderRegistry = classLoaderRegistry;
	}

	public <T, D extends ProxyDispatcher> T createProxy(URI classLoaderKey, Class<T> type, Method[] methods, Class<D> dispatcherInt, Class<? extends D> dispatcherTmpl, boolean wrapped) throws ProxyException {

		String className = type.getName() + "_Proxy_" + dispatcherInt.getSimpleName(); // ensure multiple dispatchers can be defined for the same interface

		// check if the proxy class has already been created
		BytecodeClassLoader generationLoader = getClassLoader(classLoaderKey);
		try {
			Class<T> proxyClass = (Class<T>) generationLoader.loadClass(className);
			return proxyClass.newInstance();
		} catch (ClassNotFoundException e) {
			// ignore
		} catch (InstantiationException e) {
			throw new ProxyException(e);
		} catch (IllegalAccessException e) {
			throw new ProxyException(e);
		}

		verifyTemplate(dispatcherTmpl);

		String classNameInternal = Type.getInternalName(type) + "_Proxy_" + dispatcherInt.getSimpleName();
		String thisDescriptor = "L" + classNameInternal + ";";
		String dispatcherIntName = Type.getInternalName(dispatcherInt);

		//Important to use class version of template class that will be copied. If class compiled with JDK 1.5 but copied 
		//to class version 1.7 there will be errors since 1.7 enforces frame stackmaps which were not present in 1.5

		int version = getClassVersion(generationLoader, dispatcherTmpl);
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		if (type.isInterface()) {
			String interfazeName = Type.getInternalName(type);
			cw.visit(version, ACC_PUBLIC + ACC_SUPER, classNameInternal, null, "java/lang/Object", new String[] { dispatcherIntName, interfazeName });
			cw.visitSource(type.getName() + "Proxy.java", null);
			String baseName = Type.getInternalName(Object.class);
			// write the ctor
			writeConstructor(baseName, thisDescriptor, cw);
		} else {
			verifyBaseClass(type, methods);
			String baseTypeName = Type.getInternalName(type);
			cw.visit(version, ACC_PUBLIC + ACC_SUPER, classNameInternal, null, baseTypeName, new String[] { dispatcherIntName });

			cw.visitSource(type.getName() + "Proxy.java", null);
			String baseName = Type.getInternalName(type);
			// write the ctor
			writeConstructor(baseName, thisDescriptor, cw);
		}

		copyTemplate(generationLoader, classNameInternal, dispatcherTmpl, cw);

		// write the methods
		int methodIndex = 0;
		for (Method method : methods) {
			//if the method is not overridable do not generate a bytecode method for it. This means any invocation of the class will directly act upon the 
			//the base class or proxy class but since these methods should not be visible anyway it shouldn't matter. The exception could be equals/hashcode/toString/clone 
			if (!isOverridableMethod(method)) {
				methodIndex++;
				continue;
			}
			String methodSignature = Type.getMethodDescriptor(method);
			String[] exceptions = new String[method.getExceptionTypes().length];
			for (int i = 0; i < exceptions.length; i++) {
				exceptions[i] = Type.getInternalName(method.getExceptionTypes()[i]);
			}
			int visibility = Modifier.isPublic(method.getModifiers()) ? ACC_PUBLIC : Modifier.isProtected(method.getModifiers()) ? ACC_PROTECTED : 0;
			mv = cw.visitMethod(visibility, method.getName(), methodSignature, null, exceptions);
			mv.visitCode();

			List<Label> exceptionLabels = new ArrayList<Label>();
			Label label2 = new Label();
			Label label3 = new Label();

			for (String exception : exceptions) {
				Label endLabel = new Label();
				exceptionLabels.add(endLabel);
				mv.visitTryCatchBlock(label2, label3, endLabel, exception);

			}

			mv.visitLabel(label2);
			mv.visitVarInsn(ALOAD, 0);

			// set the method index used to dispatch on
			if (methodIndex >= 0 && methodIndex <= 5) {
				// use an integer constant if within range
				mv.visitInsn(Opcodes.ICONST_0 + methodIndex);
			} else {
				mv.visitIntInsn(Opcodes.BIPUSH, methodIndex);
			}
			methodIndex++;

			int[] index = new int[1];
			index[0] = 0;
			int[] stack = new int[1];
			stack[0] = 1;

			if (method.getParameterTypes().length == 0) {
				// no params, load null
				mv.visitInsn(Opcodes.ACONST_NULL);
			} else {
				if (wrapped) {
					emitWrappedParameters(method, mv, index, stack);
				} else {
					emitUnWrappedParameters(method, mv, index, stack);
				}
			}

			mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, "_f3_invoke", "(ILjava/lang/Object;)Ljava/lang/Object;");

			// handle return values
			writeReturn(method, label3, mv);

			// implement catch blocks
			index[0] = 0;
			for (String exception : exceptions) {
				Label endLabel = exceptionLabels.get(index[0]);
				mv.visitLabel(endLabel);
				mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { exception });
				mv.visitVarInsn(ASTORE, wrapped ? stack[0] : 1);
				Label label6 = new Label();
				mv.visitLabel(label6);
				mv.visitVarInsn(ALOAD, wrapped ? stack[0] : 1);
				mv.visitInsn(ATHROW);
				index[0]++;
			}

			Label label7 = new Label();
			mv.visitLabel(label7);
			mv.visitMaxs(7, 5);
			mv.visitEnd();

		}
		cw.visitEnd();

		byte[] data = cw.toByteArray();
		//ClassReader classReader = new ClassReader(data);
		//classReader.accept(new org.objectweb.asm.util.TraceClassVisitor(null, new org.objectweb.asm.util.ASMifier(), new java.io.PrintWriter(System.out)), 0);
		Class<?> proxyClass = generationLoader.defineClass(className, data);
		try {
			return (T) proxyClass.newInstance();
		} catch (InstantiationException e) {
			throw new ProxyException(e);
		} catch (IllegalAccessException e) {
			throw new ProxyException(e);
		}

	}

	public void onDeploy(ClassLoader classLoader) {
		// no-op
	}

	public void onUndeploy(ClassLoader classLoader) {
		if (!(classLoader instanceof MultiParentClassLoader)) {
			return;
		}
		// remove cached classloader for the contribution on undeploy
		classLoaderCache.remove(((MultiParentClassLoader) classLoader).getName());
	}

	protected void verifyTemplate(Class<? extends ProxyDispatcher> dispatcherTmpl) throws ProxyException {
		if (dispatcherTmpl.isInterface() || Modifier.isAbstract(dispatcherTmpl.getModifiers())) {
			throw new ProxyException("Dispatcher Template " + dispatcherTmpl + "  must be a non-abstract class");
		}

		try {
			Constructor<?> c = dispatcherTmpl.getConstructor(new Class[0]);
			if (!Modifier.isPublic(c.getModifiers())) {
				throw new ProxyException("Dispatcher Template class " + dispatcherTmpl + " must have public no argument constructor");
			}
		} catch (NoSuchMethodException e) {
			throw new ProxyException("Dispatcher Template class " + dispatcherTmpl + " must have no argument constructor");
		}

		if (!Object.class.equals(dispatcherTmpl.getSuperclass())) {
			throw new ProxyException("Dispatcher Template class " + dispatcherTmpl + " must not be a subclass");
		}
	}

	protected void verifyBaseClass(Class<?> type, Method[] methods) throws ProxyException {
		if (Modifier.isFinal(type.getModifiers())) {
			throw new ProxyException("Proxy class " + type + " is declared final");
		}
		if (Modifier.isAbstract(type.getModifiers())) {
			throw new ProxyException("Proxy class " + type + "  is declared abstract");
		}

		try {
			Constructor<?> c = type.getConstructor(new Class[0]);
			if (!Modifier.isPublic(c.getModifiers())) {
				throw new ProxyException("Proxy class " + type + " must have public no argument constructor");
			}
		} catch (NoSuchMethodException e) {
			throw new ProxyException("Proxy class " + type + " must have no argument constructor");
		}
	}
	
	public static InputStream readClass(ClassLoader classLoader, Class<?> clazz) throws ProxyException{
		String className = Type.getInternalName(clazz) + ".class";
		InputStream is = classLoader.getResourceAsStream(className);
		if (is == null) {
			throw new ProxyException("Unable to locate class " + className);
		}
		return is;
	}
	
	public static int getClassVersion(ClassLoader classLoader, Class<?> clazz) throws ProxyException {//instead of using ASM just read class header to get version 
		try {			
			DataInputStream in = new DataInputStream(readClass(classLoader, clazz));
			int magicNumber = in.readInt();
			if (magicNumber != 0xcafebabe) {
				throw new ProxyException("Invalid class");
			}
			int minorVersion = in.readUnsignedShort();
			int majorVersion = in.readUnsignedShort();
			in.close();
			return majorVersion;
		} catch (IOException e) {
			throw new ProxyException(e);
		}
	}

	protected boolean isOverridableMethod(Method method) {
		if (OBJECT_METHODS.contains(method)) {
			return false;
		}

		if (Modifier.isStatic(method.getModifiers())) {
			return false;
		}

		if (Modifier.isFinal(method.getModifiers())) {
			return false;
		}

		if (Modifier.isPrivate(method.getModifiers())) {
			return false;
		}
		
		if (Modifier.isPublic(method.getModifiers())) {
			return true;
		}

		if (Modifier.isProtected(method.getModifiers())) {
			return true;
		}
		return false;
	}

	public <D extends ProxyDispatcher> void copyTemplate(ClassLoader classLoader, String newClassNameInternal, Class<? extends D> dispatcherTmpl, ClassWriter cw) throws ProxyException {
		try {
			ClassReader cr = new ClassReader(readClass(classLoader, dispatcherTmpl));
			cr.accept(new TemplateCopyVisitor(dispatcherTmpl, new RemappingClassAdapter(cw, new TemplateRemapper(Type.getInternalName(dispatcherTmpl), newClassNameInternal))), 0);
		} catch (IOException ie) {
			throw new ProxyException(ie);
		}
	}

	public static class TemplateCopyVisitor extends ClassVisitor {
		boolean isInnerClass;
		String className;

		public TemplateCopyVisitor(Class<?> dispatcherTmpl, ClassVisitor cv) {
			super(Opcodes.ASM4, cv);
			this.isInnerClass = dispatcherTmpl.getEnclosingClass() != null;
			this.className = Type.getInternalName(dispatcherTmpl);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (!"<init>".equals(name)) {//ignore all constructors. Since template must not be a subclass and proxy will only be instanciated by ProxyFactoryImpl this should not be a problem
				return super.visitMethod(access, name, desc, signature, exceptions);
			} else {
				return null;
			}
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			if (!isInnerClass) { //if the template is not an inner class include all innerclasses
				super.visitInnerClass(name, outerName, innerName, access);
			} else if (!name.equals(className)) { //if it is an inner class include all other inner classes. Do not ommit an inner class definition for this inner class so all contents get promoted to top level proxy class
				super.visitInnerClass(name, outerName, innerName, access);
			}
		}

		@Override
		public void visitSource(String source, String debug) {

		}

		@Override
		public void visitEnd() {

		}

	}

	public static class TemplateRemapper extends Remapper {
		String oldType;
		String newType;

		public TemplateRemapper(String oldType, String newType) {
			this.oldType = oldType;
			this.newType = newType;
		}

		@Override
		public String mapType(String type) { //field owners
			return type.replace(oldType, newType);
		}

		@Override
		public String mapDesc(String desc) { //variable descriptions
			return desc.replace(oldType, newType);
		}

	}

	@SuppressWarnings("unchecked")
	public void emitWrappedParameters(Method method, MethodVisitor mv, int[] index, int[] stack) {
		int numberOfParameters = method.getParameterTypes().length;

		// create the Object[] used to pass the parameters to _f3_invoke and push it on the stack
		if (numberOfParameters >= 0 && numberOfParameters <= 5) {
			// use an integer constant if within range
			mv.visitInsn(Opcodes.ICONST_0 + numberOfParameters);
		} else {
			mv.visitIntInsn(Opcodes.BIPUSH, numberOfParameters);
		}
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		mv.visitInsn(DUP);

		for (Class<?> param : method.getParameterTypes()) {
			if (Integer.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.ILOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
			} else if (Float.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.FLOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
			} else if (Boolean.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.ILOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
			} else if (Short.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.ILOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
			} else if (Byte.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.ILOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
			} else if (Double.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.DLOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
				stack[0]++; // double occupies two positions

			} else if (Long.TYPE.equals(param)) {
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(Opcodes.LLOAD, stack[0]);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
				stack[0]++; // long occupies two positions
			} else {
				// object type
				mv.visitInsn(Opcodes.ICONST_0 + index[0]);
				mv.visitVarInsn(ALOAD, stack[0]);
				mv.visitInsn(AASTORE);
				if (index[0] < numberOfParameters - 1) {
					mv.visitInsn(DUP);
				}
			}
			index[0]++;
		}
		// TODO other primitive types
		stack[0]++;

	}

	@SuppressWarnings("unchecked")
	public void emitUnWrappedParameters(Method method, MethodVisitor mv, int[] index, int[] stack) {
		int numberOfParameters = method.getParameterTypes().length;
		if (numberOfParameters > 1) {
			// FIXME
			throw new AssertionError("Not supported");
		}

		Class<?> param = method.getParameterTypes()[0];
		if (Integer.TYPE.equals(param)) {
			mv.visitVarInsn(ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		} else if (Float.TYPE.equals(param)) {
			mv.visitVarInsn(Opcodes.FLOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
		} else if (Boolean.TYPE.equals(param)) {
			mv.visitVarInsn(Opcodes.ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
		} else if (Short.TYPE.equals(param)) {
			mv.visitVarInsn(Opcodes.ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
		} else if (Byte.TYPE.equals(param)) {
			mv.visitVarInsn(Opcodes.ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
		} else if (Double.TYPE.equals(param)) {
			mv.visitVarInsn(Opcodes.DLOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
		} else if (Long.TYPE.equals(param)) {
			mv.visitVarInsn(Opcodes.LLOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
		} else {
			// object type
			mv.visitVarInsn(ALOAD, 1);
		}

	}

	private void writeConstructor(String baseName, String thisDescriptor, ClassWriter cw) {
		MethodVisitor mv;
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, baseName, "<init>", "()V");
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", thisDescriptor, null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void writeReturn(Method method, Label endLabel, MethodVisitor mv) {
		Class<?> returnType = method.getReturnType();

		if (Void.TYPE.equals(returnType)) {
			mv.visitInsn(Opcodes.POP);
			mv.visitLabel(endLabel);
			mv.visitInsn(RETURN);
		} else if (returnType.isPrimitive()) {
			if (Double.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.DRETURN);
			} else if (Long.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.LRETURN);
			} else if (Integer.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.IRETURN);
			} else if (Float.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.FRETURN);
			} else if (Short.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.IRETURN);
			} else if (Byte.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.IRETURN);
			} else if (Boolean.TYPE.equals(returnType)) {
				mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
				mv.visitLabel(endLabel);
				mv.visitInsn(Opcodes.IRETURN);
			}
		} else {
			String internalTypeName = Type.getInternalName(returnType);
			mv.visitTypeInsn(CHECKCAST, internalTypeName);
			mv.visitLabel(endLabel);
			mv.visitInsn(ARETURN);
		}
	}

	/**
	 * Returns a classloader for loading the proxy class, creating one if necessary.
	 *
	 * @param classLoaderKey the key of the contribution classloader the proxy is being loaded for; set as the parent
	 * @return the classloader
	 */
	private BytecodeClassLoader getClassLoader(URI classLoaderKey) {
		ClassLoader parent = classLoaderRegistry.getClassLoader(classLoaderKey);
		BytecodeClassLoader generationClassLoader = classLoaderCache.get(classLoaderKey);
		if (generationClassLoader == null) {
			generationClassLoader = new BytecodeClassLoader(classLoaderKey, parent);
			generationClassLoader.addParent(getClass().getClassLoader()); // proxy classes need to be visible as well
			classLoaderCache.put(classLoaderKey, generationClassLoader);
		}
		return generationClassLoader;
	}

}
