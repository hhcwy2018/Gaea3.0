package com.wh.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.wh.hardware.register.HintType;
import com.wh.hardware.register.ServerCore;
import com.wh.hardware.register.ServerCore.AuthInfo;
import com.wh.hardware.register.test.ToolHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AuthRegisterServerTester extends TestCase {

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AuthRegisterServerTester(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AuthRegisterServerTester.class);
	}

	/**
	 * Rigourous Test :-)
	 * 
	 * @throws Throwable
	 */
	public void testApp() throws Throwable {
		String serverName = "server";
		String clientName = "client";
		ToolHelper.genKeyFile(serverName);
		ToolHelper.genKeyFile(clientName);

		File serverPrivateKeyFile = ToolHelper.getPriavteKeyFile(serverName);
		File clientPublicKeyFile = ToolHelper.getPublicKeyFile(clientName);
		File applyFile = ToolHelper.getFile("config", clientName + ".apy");
		File authFile = ToolHelper.getFile("config", "register.dat");
		File codeFile = ToolHelper.getFile("sample", "CoreTester.java");
		File authCodeFile = ToolHelper.getFile("config", "crypt.code");

		String aesKey = "1223";

		AuthInfo authInfo = new AuthInfo();
		authInfo.aesKey = aesKey;
		authInfo.clientTag = clientName;
		authInfo.days = 10;

		ServerCore serverCore = new ServerCore(serverPrivateKeyFile, clientPublicKeyFile);

		serverCore.genAuthFile(applyFile, authFile, authInfo);
		serverCore.encodeCode(codeFile, authCodeFile, aesKey);

		int ret = -1;
		while (true) {
			try {
				ret = Integer.parseInt(ToolHelper.readLine("请输入生成注册文件类型【1试用10天，2超期，3正式注册】：1"));
				break;
			} catch (Exception e) {
				continue;
			}
		}
		
		switch (ret) {
		case 1:
			// 试用10天
			authInfo.start = new Date();
			authInfo.days = 10;
			authInfo.hintType = HintType.htMsg;
			break;
		case 2:
			// 已经过期
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, -11);
			authInfo.start = calendar.getTime();
			authInfo.days = 5;
			authInfo.hintType = HintType.htMsg;
			break;
		case 3:
			// 正式注册
			authInfo.start = new SimpleDateFormat("yyyy-MM-dd").parse("2020-02-01");
			authInfo.days = -1;
			authInfo.genAuthKey = true;
			serverCore.genAuthFile(applyFile, authFile, authInfo);
			break;

		default:
			return;
		}

		serverCore.genAuthFile(applyFile, authFile, authInfo);
	}
}
