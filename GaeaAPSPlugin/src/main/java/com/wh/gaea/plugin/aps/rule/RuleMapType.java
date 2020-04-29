package com.wh.gaea.plugin.aps.rule;

public enum RuleMapType {
	mtEqual(-1, "相等"), mtLeftInclude(0, "左包含"), mtRightInclude(0, "右包含"), mtInclude(0, "包含"), mtNumber(1, "数学运算");

	int code;
	String msg;

	private RuleMapType(int code, String msg) {
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