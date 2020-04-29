package com.wh.gaea.plugin.authorization;

import java.io.File;
import java.io.IOException;

import com.wh.encrypt.Asymmetric;
import com.wh.gaea.GlobalInstance;
import com.wh.hardware.register.ClientCore;
import com.wh.hardware.register.ClientCore.IDynamicCoreClass;
import com.wh.hardware.register.ServerCore.AuthInfo;
import com.wh.swing.tools.MsgHelper;

public abstract class AuthHelper {
	public static final int GENAUTH_COL_INDEX = 0;
	public static final int ID_COL_INDEX = 1;
	public static final int NAME_COL_INDEX = 2;
	public static final int START_COL_INDEX = 3;
	public static final int DAY_COL_INDEX = 4;
	public static final int CONTACT_COL_INDEX = 5;
	public static final int TEL_COL_INDEX = 6;
	public static final int HINT_COL_INDEX = 7;
	public static final int AES_COL_INDEX = 8;

	public static final String SERVER_AUTH_NAME_PREX = "server";
	public static final String CLIENT_AUTH_NAME_PREX = "client";
	
	public static final String Auth_Dir_Name = "auth";
	public static File getRootPath(String id) throws IOException {
		File path = GlobalInstance.instance().getProjectFile(AuthHelper.Auth_Dir_Name, id);
		if (!path.exists())
			if (!path.mkdirs())
				throw new IOException("建立目录【" + path.getAbsolutePath() + "】失败！");

		return path;
	}

	public static File getKeyFile(String name, String id) throws IOException {
		File file = new File(getRootPath(id), name);
		File path = file.getParentFile();
		if (!path.exists())
			if (!path.mkdirs())
				throw new IOException("建立目录【" + path.getAbsolutePath() + "】失败！");

		return file;
	}

	public static File getPublicKeyFile(String name, String id) throws IOException {
		return getKeyFile(name + "_pub.key", id);
	}

	public static File getPrivateKeyFile(String name, String id) throws IOException {
		return getKeyFile(name + "_pri.key", id);
	}

	public static void genKeyFile(String name, String pathName) throws Exception {
		File publicKeyFile = getPublicKeyFile(name, pathName);
		File privateKeyFile = getPrivateKeyFile(name, pathName);
		Asymmetric.getBase64KeyPairs(publicKeyFile, privateKeyFile);
	}

	public static File getAuthFile(String id) throws IOException {
		return new File(getRootPath(id), "authorization.reg");
	}

	public static File getEncryptCodeFile(String id) throws IOException {
		return new File(getRootPath(id), "core.wh");
	}

	public static File getAuthRequestFile(String id) throws IOException {
		return new File(AuthHelper.getRootPath(id), "client.apy");
	}

	public static File getSourceCodeFile(String id) throws IOException {
		return new File(AuthHelper.getRootPath(id), "source.java");
	}

	public static File getDataFile() throws IOException {
		File file = GlobalInstance.instance().getProjectFile(AuthHelper.Auth_Dir_Name, "regs.dat");
		File path = file.getParentFile();
		if (!path.exists())
			if (!path.mkdirs())
				throw new IOException("建立目录【" + path.getAbsolutePath() + "】失败！");

		return file;
	}

	public static void testAuth(String id, AuthInfo authInfo) {		
		try {
			File serverPublicKeyFile = getPublicKeyFile(SERVER_AUTH_NAME_PREX, id);
			File clientPrivateKeyFile = getPrivateKeyFile(CLIENT_AUTH_NAME_PREX, id);;
			File authFile = getAuthFile(id);
			File codeFile = getEncryptCodeFile(id);

			ClientCore clientCore = new ClientCore(clientPrivateKeyFile,  
					serverPublicKeyFile, "com.wh.test.CoreTester", new IDynamicCoreClass() {
						
						@Override
						public File getRegisterFile() {
							return authFile;
						}
						
						@Override
						public File getCryptCodeFile() throws Exception {
							return codeFile;
						}
					});
			
			clientCore.setRegisterFileNoGetClass();
			clientCore.testNoCall("测试在注册时间内:");
			MsgHelper.showMessage("成功注册");
		} catch (Exception e) {
			MsgHelper.showException(e);;
		} catch (Throwable e) {
			MsgHelper.showMessage("注册已经过期！");
		}

		

	}
	
}
