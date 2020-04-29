package com.wh.test;

import java.io.File;

import com.wh.hardware.register.ClientCore.IDynamicCoreClass;
import com.wh.hardware.register.test.ToolHelper;

public class DynamicCoreFunctionTester implements IDynamicCoreClass{

	public static final File codeFile = ToolHelper.getFile("config", "crypt.code");
	public static final File registerFile = ToolHelper.getFile("config", "register.dat");

	@Override
	public File getRegisterFile() {
		return registerFile;
	}

	@Override
	public File getCryptCodeFile() throws Exception {
		return codeFile;
	}

}
