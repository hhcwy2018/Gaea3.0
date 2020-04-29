package com.wh.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class StringClassLoader extends ClassLoader {

	// 存放编译之后的字节码(key:类全名,value:编译之后输出的字节码)
	private Map<String, ByteJavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();
	// 存放编译过程中输出的信息
	private DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();

	/**
	 * 自定义一个字符串的源码对象
	 */
	private class StringJavaFileObject extends SimpleJavaFileObject {
		// 等待编译的源码字段
		private String contents;

		// java源代码 => StringJavaFileObject对象 的时候使用
		public StringJavaFileObject(String className, String contents) {
			super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
			this.contents = contents;
		}

		// 字符串源码会调用该方法
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return contents;
		}

	}

	/**
	 * 自定义一个编译之后的字节码对象
	 */
	private class ByteJavaFileObject extends SimpleJavaFileObject {
		// 存放编译后的字节码
		private ByteArrayOutputStream outPutStream;

		public ByteJavaFileObject(String className, Kind kind) {
			super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), kind);
		}

		// StringJavaFileManage 编译之后的字节码输出会调用该方法（把字节码输出到outputStream）
		@Override
		public OutputStream openOutputStream() {
			outPutStream = new ByteArrayOutputStream();
			return outPutStream;
		}

		// 在类加载器加载的时候需要用到
		public byte[] getCompiledBytes() {
			return outPutStream.toByteArray();
		}
	}

	/**
	 * 自定义一个JavaFileManage来控制编译之后字节码的输出位置
	 */
	@SuppressWarnings("rawtypes")
	private class StringJavaFileManage extends ForwardingJavaFileManager {
		@SuppressWarnings("unchecked")
		StringJavaFileManage(JavaFileManager fileManager) {
			super(fileManager);
		}

		// 获取输出的文件对象，它表示给定位置处指定类型的指定类。
		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
				FileObject sibling) throws IOException {
			ByteJavaFileObject javaFileObject = new ByteJavaFileObject(className, kind);
			javaFileObjectMap.put(className, javaFileObject);
			return javaFileObject;
		}
	}

	/**
	 * 获取类的全名称
	 *
	 * @param sourceCode 源码
	 * @return 类的全名称
	 */
	public String getFullClassName(String sourceCode) {
		String className = "";
		Pattern pattern = Pattern.compile("package(.+);");
		Matcher matcher = pattern.matcher(sourceCode);
		if (matcher.find()) {
			className = matcher.group().replaceFirst("package", "").replace(";", "").trim() + ".";
		}

		pattern = Pattern.compile("class(.+)implements|extends|\\{");
		matcher = pattern.matcher(sourceCode);
		if (matcher.find()) {
			className += matcher.group(1).trim();
		}
		return className;
	}

	String fullClassName;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean compiler(String sourceCode, Object... args) {

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		// 标准的内容管理器,更换成自己的实现，覆盖部分方法
		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
		JavaFileManager javaFileManager = new StringJavaFileManage(standardFileManager);
		// 构造源代码对象
		JavaFileObject javaFileObject = new StringJavaFileObject(fullClassName, sourceCode);
		
		Iterable options = args == null || args.length == 0 ? null : Arrays.asList(args);
		// 获取一个编译任务
		JavaCompiler.CompilationTask task = compiler.getTask(null, javaFileManager, diagnosticsCollector, options, null,
				Arrays.asList(javaFileObject));
		return task.call();
	}

	public void compilerClass(String sourceCode, File javaFile, String... args) throws Exception {
		TextStreamHelp.saveToFile(javaFile, sourceCode);
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		List<String> options = new ArrayList<>();
		options.add(javaFile.getAbsolutePath());
		if (args != null && args.length > 0)
			for (String string : args) {
				options.add(string);
			}
		// 获取一个编译任务
		int ret = compiler.run(null, null, null, options.toArray(new String[options.size()]));
		if (ret != 0)
			throw new Exception(getCompilerMessage());
	}

	public String getCompilerMessage() {
		StringBuilder sb = new StringBuilder();
		List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
		for (Diagnostic<?> diagnostic : diagnostics) {
			sb.append(diagnostic.toString()).append("\r\n");
		}
		return sb.toString();
	}

	public StringClassLoader() {
		
	}
	
	public StringClassLoader(String classCode) throws Throwable {
		super();
		fullClassName = getFullClassName(classCode);
		if (!compiler(classCode))
			throw new Throwable(getCompilerMessage());
	}

	@SuppressWarnings("unchecked")
	public <T> T instance() throws Throwable {
		return (T) findClass(fullClassName).newInstance();

	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		ByteJavaFileObject fileObject = javaFileObjectMap.get(name);
		if (fileObject != null) {
			byte[] bytes = fileObject.getCompiledBytes();
			return defineClass(name, bytes, 0, bytes.length);
		}
		try {
			return ClassLoader.getSystemClassLoader().loadClass(name);
		} catch (Exception e) {
			return super.findClass(name);
		}
	}

}