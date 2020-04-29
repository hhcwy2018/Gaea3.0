package com.wh.gaea.plugin.aps.rule.connector;

public enum RuleTarget {
	ftDevice(0, "设备"), ftModule(1, "模具"), ftMaterial(2, "物料");

	int code;
	String msg;

	private RuleTarget(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return msg;
	}

	public int getCode() {
		return code;
	}
}