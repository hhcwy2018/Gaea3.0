package com.wh.test;

import com.wh.hardware.register.ICore;

public class CoreTester implements ICore<String> {

	@Override
	public String call(Object context) {
		return context.toString();
	}

	@Override
	public void hint() {
		System.out.println("unregistered : 你的系统未注册！");
	}

}
