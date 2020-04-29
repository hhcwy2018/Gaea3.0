package com.wh.test;

import java.io.File;

import com.wh.hardware.register.ClientCore;
import com.wh.hardware.register.test.ToolHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AuthRegisterClientTester extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AuthRegisterClientTester(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AuthRegisterClientTester.class);
	}

	/**
	 * Rigourous Test :-)
	 * 
	 * @throws Throwable
	 */
	public void testApp() throws Throwable {
		String serverName = "server";
		String clientName = "client";

		File serverPublicKeyFile = ToolHelper.getPublicKeyFile(serverName);
		File clientPrivateKeyFile = ToolHelper.getPriavteKeyFile(clientName);
		
		File applyFile = ToolHelper.getFile("config", clientName + ".apy");

		ClientCore clientCore = new ClientCore(clientPrivateKeyFile, serverPublicKeyFile, "com.wh.test.CoreTester",
				new DynamicCoreFunctionTester());

		int ret = -1;
		while (true) {
			try {
				ret = Integer.parseInt(ToolHelper.readLine("请输入要执行的操作【1生成注册申请文件，2检查注册文件】"));
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		switch (ret) {
		case 1:
			clientCore.genApplyFile(applyFile);
			break;
		default:
			clientCore.setRegisterFile();
			//此处的try仅应用于本测试，正式客户端代码不可以加
			try {
				String result = clientCore.test("检查注册文件");
				ToolHelper.print("注册有效", result);
			} catch (Throwable e) {
				ToolHelper.print("注册文件无效！", "未注册");
			}
			break;
		}

	}
}
