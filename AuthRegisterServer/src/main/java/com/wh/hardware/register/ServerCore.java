package com.wh.hardware.register;

import java.io.File;
import java.util.Date;

import org.json.JSONObject;

import com.wh.encrypt.AES;
import com.wh.encrypt.Asymmetric;

public final class ServerCore extends Core{
	public static String CHARSET = "utf8";
	
	/***
	 * 授权信息，禁用与服务端生成授权文件函数
	 * @author wy
	 *
	 */
	public static class AuthInfo{
		
		public HintType hintType = HintType.htHalf;
		
		/***
		 * 不能为null，每个发布程序一个标识不可重复，并且程序应该通过程序引用此标识
		 */
		public String clientTag = null;
		/***
		 * 加密代码
		 */
		protected String code = null;
		/***
		 * 授权的起始时间，可以为null
		 */
		public Date start = new Date();
		/***
		 * 授权天数，可以为-1，表示无期限
		 */
		public int days = -1;
		/***
		 * 用于加解密核心代码的aes密码
		 */
		public String aesKey = null;
		/***
		 * 授权id，可以为null
		 */
		public String authKey = null;
		
		/**
		 * 是否生成注册key，true生成，其他不生成
		 */
		public boolean genAuthKey = false;
	}
	
	String serverPrivateKey, clientPublicKey;
	public ServerCore(String serverPrivateKey, String clientPublicKey) {
		this.serverPrivateKey = serverPrivateKey;
		this.clientPublicKey = clientPublicKey;
	}
	
	/**
	 * 仅用于查看注册申请文件
	 * @param serverPrivateFile
	 * @throws Exception
	 */
	public ServerCore(File serverPrivateFile) throws Exception {
		this(BytesHelp.loadTextFile(serverPrivateFile), null);
	}
	
	public ServerCore(File serverPrivateFile, File clientPublicFile) throws Exception {
		this(BytesHelp.loadTextFile(serverPrivateFile), BytesHelp.loadTextFile(clientPublicFile));
	}

	/**
	 * 保存注册信息到注册文件
	 * @param authFile 生成的注册文件
	 * @param authInfo 要保存的注册信息
	 * @throws Exception
	 */
	final void saveAuthFile(File authFile, AuthInfo authInfo) throws Exception {
		JSONObject data = new JSONObject();
		data.put("start", authInfo.start == null ? new Date().getTime() : authInfo.start.getTime());
		data.put("use", authInfo.days);
		data.put("tag", authInfo.clientTag);
		data.put("hint", authInfo.hintType);
		if (!authInfo.genAuthKey) {
			data.put("key", "x");
		}else
			data.put("key", authInfo.code);
		data.put("aes", authInfo.aesKey);
		data.put("sign", Asymmetric.signatureBase64Data(getRegisterSignCode(data), serverPrivateKey));
		String registerText = Asymmetric.encrypt(data.toString(), clientPublicKey);
		BytesHelp.saveFile(registerText.getBytes(CHARSET), authFile);
		
	}
	
	/**
	 * 从文件读取用户提交的用于生成注册key的特征码信息
	 * @param file 包含用户机器特征码的文件，用客户端生成
	 * @param serverPrivateKey 对应这个用户的服务端密钥对的私钥
	 * @return 用户提交的注册信息（特征码）
	 * @throws Exception
	 */
	final String readApplyFile(File file, String serverPrivateKey) throws Exception {
		String text = new String(BytesHelp.loadFile(file), CHARSET);
		String code = Asymmetric.decrypt(text, serverPrivateKey);
		return code;
	}
	
	/**
	 * 从文件读取用户提交的用于生成注册key的特征码信息
	 * @param file 包含用户机器特征码的文件，用客户端生成
	 * @return 用户提交的注册信息（特征码）
	 * @throws Exception
	 */
	public String readApplyFile(File file) throws Exception {
		return readApplyFile(file, serverPrivateKey);
	}
	
	/**
	 * 通过用户提交的注册信息文件生成对应的授权文件
	 * @param applyFile 用户提交的注册文件
	 * @param authFile 生成的用户授权文件
	 * @param authInfo 此用户的授权信息
	 * @throws Exception
	 */
	public void genAuthFile(File applyFile, File authFile, AuthInfo authInfo) throws Exception {
		String code = readApplyFile(applyFile, serverPrivateKey);
		
		authInfo.code = Asymmetric.signatureBase64Data(code, serverPrivateKey);
		
		saveAuthFile(authFile, authInfo);
		
	}
	
	/**
	 * 将一个代码文件加密，此文件用于客户端验证及执行核心业务任务，必须实现ICore接口，并且call方法返回以及处理过程必须和具体程序的业务功能相关。
	 * @param codeFile 包含实现ICore接口的代码的文件，文件的编码必须为utf8
	 * @param encodeCodeFile 生成的用于客户端的加密代码文件
	 * @param aesKey 用于加密代码的aes密钥
	 * @throws Exception
	 */
	public void encodeCode(File codeFile, File encodeCodeFile, String aesKey) throws Exception {
		AES.encryptFile(aesKey, codeFile.getAbsolutePath(), encodeCodeFile.getAbsolutePath());
	}
}
