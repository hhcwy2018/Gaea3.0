package com.wh.hardware.register;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import com.wh.encrypt.AES;
import com.wh.encrypt.Asymmetric;
import com.wh.tools.HardwareInformation;
import com.wh.tools.StringClassLoader;

public final class ClientCore extends Core {

	/**
	 * 此接口用于获取密过程必要的信息
	 * @author HP
	 *
	 */
	public interface IDynamicCoreClass {

		/***
		 * 获取客户端被加密的代码端，此代码应该和核心的代码有直接作用，并且应该适度调用
		 * 
		 * @return 加密代码文本，此文本必须通过授权文件的aesKey加密
		 * @throws Exception
		 */
		File getCryptCodeFile() throws Exception;

		/***
		 * 返回包含试用或者正式使用授权的时间信息的文件绝对路径
		 * 
		 * 文件内容格式为：
		 * {start:100,use:count,aes:"加密代码的aes密码",key:"123",sign:"vk",hint:"htMsg",
		 * msg:"未注册"} start:试用的起始日期 use:试用的时间，单位为天。 aes:用来加解密加密代码的aes密钥 key:授权码
		 * 
		 * @return 授权文件，此文件全文必须经过Asymmetric。encode加密，并且使用的公钥必须与getKey的返回值为一对密钥
		 */
		File getRegisterFile();
	}

	/***
	 * 获取客户端的私钥，必须为Asymmetric.toBase64String函数的返回值
	 */
	String clientPrivateKey;

	/***
	 * 服务端的公钥，必须为Asymmetric.toBase64String函数的返回值
	 */
	String serverPublicKey;

	IDynamicCoreClass dynamicCoreClass;

	@SuppressWarnings("rawtypes")
	ICore core;

	String className;
	
	JSONObject data;
	
	int div = 0;

	/**
	 * 仅用于生成注册申请文件
	 * @param serverPublicFile 服务端公钥文件
	 * @throws Exception
	 */
	public ClientCore(File serverPublicFile) throws Exception {
		this(null,
				BytesHelp.loadTextFile(serverPublicFile), null, null);
	}

	/**
	 * 构造方法
	 * @param clientPrivateFile 包含客户端的RSA密钥对的私钥的文件，内容必须为key的base64编码形式
	 * @param clientPublicFile 包含客户端的RSA密钥对的公钥的文件，内容必须为key的base64编码形式
	 * @param serverPublicFile 包含服务端的RSA密钥对的公钥的文件，内容必须为key的base64编码形式
	 * @param className 动态代码的类名称
	 * @param dynamicCoreClass 参见IDynamicCoreClass接口说明
	 * @throws Exception
	 */
	public ClientCore(File clientPrivateFile, File serverPublicFile, String className,
			IDynamicCoreClass dynamicCoreClass) throws Exception {
		this(BytesHelp.loadTextFile(clientPrivateFile),
				BytesHelp.loadTextFile(serverPublicFile), className, dynamicCoreClass);
	}

	/**
	 * 构造方法
	 * @param clientPrivateKey 客户端的RSA密钥对的私钥，必须为key的base64编码形式
	 * @param clientPublicKey 客户端的RSA密钥对的公钥，必须为key的base64编码形式
	 * @param serverPublicKey 服务端的RSA密钥对的公钥，必须为key的base64编码形式
	 * @param className 动态代码的类名称
	 * @param dynamicCoreClass 参见IDynamicCoreClass接口说明
	 * @throws Exception
	 */
	public ClientCore(String clientPrivateKey, String serverPublicKey, String className,
			IDynamicCoreClass dynamicCoreClass) {
		this.clientPrivateKey = clientPrivateKey;

		this.serverPublicKey = serverPublicKey;
		
		this.dynamicCoreClass = dynamicCoreClass;
		this.className = className;
	}

	public void setRegisterFile() throws Throwable {
		data = readAuthFile(dynamicCoreClass.getRegisterFile());
		core = getClass(className, decodeCode(dynamicCoreClass.getCryptCodeFile()));
	}

	public void setRegisterFileNoGetClass() throws Throwable {
		data = readAuthFile(dynamicCoreClass.getRegisterFile());
		decodeCode(dynamicCoreClass.getCryptCodeFile());
	}

	@SuppressWarnings({ "rawtypes" })
	final <T extends ICore> T getClass(String className, String code) throws Throwable {
		try {
			return new StringClassLoader(code).instance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int x = 1 / 0;
		System.out.print(x);

		return null;
	}

	/***
	 * 读取注册文件
	 * 
	 * @param file 包含注册信息的文件
	 * @return 注册信息
	 * @throws Exception
	 */
	JSONObject readAuthFile(File file) throws Exception {
		String text = new String(BytesHelp.loadFile(file), CHARSET);
		String code = Asymmetric.decrypt(text, clientPrivateKey);
		JSONObject data = new JSONObject(code);
		return data;
	}

	/**
	 * 生成客户端的注册申请文件，并在后续用以生成客户的注册文件
	 * 
	 * @param file 要保存的注册申请文件
	 * @throws Exception
	 */
	public void genApplyFile(File file) throws Exception {
		String code = new HardwareInformation().getFeatureMD5();
		String text = Asymmetric.encrypt(code, serverPublicKey);
		BytesHelp.saveFile(text.getBytes(CHARSET), file);

	}

	/**
	 * 测试用户的注册文件是否有效
	 * 
	 * @param data 注册信息
	 * @return 无意义
	 * @throws Exception
	 */
	final void testKey(JSONObject data) throws Exception {
		String scode = new HardwareInformation().getFeatureMD5();
		Asymmetric.checkSignature(serverPublicKey, data.getString("key"), scode);
	}

	/**
	 * 测试用户的试用时间是否超期
	 * 
	 * @param <T>
	 * @param data 注册信息
	 * @param core 客户端实现的核心函数接口
	 * @throws Throwable
	 */
	final <T> void testDate(ICore<T> core) throws Throwable {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(data.getLong("start")));
		calendar.add(Calendar.DAY_OF_YEAR, data.getInt("use"));
		
		
		long p1 = calendar.getTime().getTime();
		long start = new Date().getTime() - p1;

		int stat = 0;
		try {
			stat = (int) (1 / (start + Math.abs(start)));
		} catch (Throwable e) {
			return;
		}
		switch (HintType.valueOf(data.getString("hint"))) {
		case htHalf:
			throw new Throwable("b" + stat);
		case htMsg:
			core.hint();
			break;
		}
	}

	/**
	 * 测试注册文件是否有效，如果无效根据注册文件的设置，提示或者退出程序，如果是提示则回正常执行core接口
	 * 
	 * @param <T>     core接口的模板参数类型
	 * @param context 用户执行core接口call方法的上下文参数
	 * @return 返回本次core接口call方法的返回值
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public final <T> T test(Object context) throws Throwable {

		Asymmetric.checkSignature(serverPublicKey, data.getString("sign"), getRegisterSignCode(data));

		try {
			testKey(data);
		} catch (Throwable e) {
			try {
				testDate(core);
			} catch (Exception e2) {
			}
		}

		return (T) core.call(context);
	}

	/**
	 * 测试注册文件是否有效，如果无效根据注册文件的设置，提示或者退出程序，如果是提示则回正常执行core接口
	 * 
	 * @param <T>     core接口的模板参数类型
	 * @param context 用户执行core接口call方法的上下文参数
	 * @return 返回本次core接口call方法的返回值
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public final void testNoCall(Object context) throws Throwable {

		Asymmetric.checkSignature(serverPublicKey, data.getString("sign"), getRegisterSignCode(data));

		try {
			testKey(data);
		} catch (Throwable e) {
			try {
				testDate(core);
			} catch (Exception e2) {
			}
		}
	}

	/**
	 * 解码加密代码文件，返回解密后的代码文本
	 * @param encodeCodeFile 包含加密的代码文本的文件，此文件必须由ServerCore。encodeCode函数生成
	 * @return 解密后的代码文本
	 * @throws Exception
	 */
	public String decodeCode(File encodeCodeFile) throws Exception {
		try (FileInputStream inputStream = new FileInputStream(encodeCodeFile);
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();) {
			AES.decryptStream(data.getString("aes"), inputStream, buffer);
			return new String(buffer.toByteArray(), "utf8");
		}
	}

}
