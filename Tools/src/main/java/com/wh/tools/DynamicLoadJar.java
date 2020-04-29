package com.wh.tools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DynamicLoadJar {

	public static class ArgInfo<V> {
		public Class<?> type;
		public V value;

		public ArgInfo(Class<?> type, V value) {
			this.type = type;
			this.value = value;
		}
	}

	public static class AppendableUrlClassLoader extends URLClassLoader {

		public AppendableUrlClassLoader(URL[] urls) {
			super(urls);
		}

		public AppendableUrlClassLoader(URL[] array, ClassLoader contextClassLoader) {
			super(array, contextClassLoader);
		}

		public void addURL(URL url) {
			super.addURL(url);
		}
	}

	static AppendableUrlClassLoader urlClassLoader;

	public static AppendableUrlClassLoader getClassLoader() throws Exception {
		return getClassLoader(null);
	}

	protected static void addUrls(File[] dirs) throws Exception {
		if (dirs == null || dirs.length == 0)
			return;

		AppendableUrlClassLoader loader = getClassLoader();

		if (dirs != null && dirs.length > 0) {
			for (File dir : dirs) {
				for (File file : dir.listFiles()) {
					addClassLoaderFile(loader, file);
				}
			}
		}
	}

	public static AppendableUrlClassLoader getClassLoader(File[] paths) throws Exception {
		if (urlClassLoader == null)
			urlClassLoader = createExtendClassLoader(paths);
		else {
			addUrls(paths);
		}
		return urlClassLoader;
	}

	protected static void addClassLoaderFile(AppendableUrlClassLoader classLoader, File file) throws Exception {
		String name = file.getName();
		if (name.equalsIgnoreCase(".") || name.equalsIgnoreCase(".."))
			return;

		if (file.isDirectory())
			return;

		if (!file.getName().trim().toLowerCase().endsWith("jar"))
			return;

		classLoader.addURL(file.toURI().toURL());		
	}
	
	public static void addClassLoaderFile(File file) throws Exception {
		addClassLoaderFile(getClassLoader(), file);

	}

	public static void addClassLoaderUrl(File dir) throws Exception {
		addClassLoaderUrl(new File[] { dir });
	}

	public static void addClassLoaderUrl(File[] dirs) throws Exception {
		getClassLoader(dirs);
	}

	public static AppendableUrlClassLoader createExtendClassLoader(File[] dirs) throws Exception {
		List<URL> urls = new ArrayList<URL>();
		if (dirs != null && dirs.length > 0) {
			for (File dir : dirs) {

				for (File file : dir.listFiles()) {
					String name = file.getName();
					if (name.equalsIgnoreCase(".") || name.equalsIgnoreCase(".."))
						continue;

					if (file.isDirectory())
						continue;

					if (!file.getName().trim().toLowerCase().endsWith("jar"))
						continue;

					// loadAdapterJar(file);
					urls.add(file.toURI().toURL());
				}
			}
			return new AppendableUrlClassLoader(urls.toArray(new URL[urls.size()]),
					Thread.currentThread().getContextClassLoader());
		} else {
			return new AppendableUrlClassLoader(null, Thread.currentThread().getContextClassLoader());
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> T instance(String className, ArgInfo<Object>... args) throws Exception {
		return instance(className, getClassLoader(), args);
	}

	@SuppressWarnings("unchecked")
	public static <T> T instance(String className, ClassLoader classLoader, ArgInfo<Object>... args) throws Exception {
		Class<?> c = classLoader.loadClass(className);
		if (args != null && args.length > 0) {
			Class<?>[] cs = new Class<?>[args.length];
			Object[] values = new Object[args.length];

			int index = 0;
			for (ArgInfo<Object> argInfo : args) {
				cs[index] = argInfo.type;
				values[index++] = argInfo.value;
			}
			return (T) c.getConstructor(cs).newInstance(values);
		} else {
			return (T) c.newInstance();
		}

	}

}
