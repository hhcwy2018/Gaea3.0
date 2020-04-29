package com.wh.tools;

import java.io.File;

public class ClassFileLoader<T> extends ClassLoader {
	File classFile;
	byte[] cLassBytes;
	String className;

	@SuppressWarnings("unchecked")
	@Override
	protected Class<T> findClass(String name) {
		Class<T> clazz = (Class<T>) defineClass(name, cLassBytes, 0, cLassBytes.length);
		return clazz;
	}

	public ClassFileLoader(File classFile, String className) throws Exception {
		cLassBytes = BytesHelp.loadFile(classFile);
		this.className = className;
	}

	@SuppressWarnings("rawtypes")
	public T instance(Object... args) throws Exception {
		if (args == null || args.length == 0)
			return findClass(className).newInstance();
		else {
			Class[] cs = new Class[args.length];
			for (int i = 0; i < cs.length; i++) {
				cs[i] = args[i].getClass();
			}
			
			return findClass(className).getDeclaredConstructor(cs).newInstance(args);
		}
	}
}
