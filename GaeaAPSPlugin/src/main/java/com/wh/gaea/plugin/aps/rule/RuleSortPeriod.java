package com.wh.gaea.plugin.aps.rule;

public enum RuleSortPeriod {
	rtOne("第一次循环", 1), rtTwo("第二次循环", 2);

	private int code;
	private String msg;

	private RuleSortPeriod(String msg, int code) {
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

	public static RuleSortPeriod fromCode(int code) {
		switch (code) {
		case 1:
			return rtOne;
		case 2:
		default:
			return rtTwo;
		}
	}

}
