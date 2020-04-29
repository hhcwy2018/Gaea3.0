package com.wh.gaea.plugin.aps.rule;

public enum RuleExprLogicType {
	ltNone(-1, "无"), ltAnd(0, "and"), ltOr(1, "or"), ltLeftPair(2, "("), ftRightPair(3, ")");

	int code;
	String msg;

	private RuleExprLogicType(int code, String msg) {
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