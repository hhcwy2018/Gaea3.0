package com.wh.gaea.plugin.aps.rule;

public enum RuleExprOperation {
	foEqual(0, "="), foUnequal(1, "<>"), foIn(2, "包含"), foNotIn(3, "不包含"), foGreate(4, ">"), foLess(5, "<"),
	foGreateEqual(6, ">="), foLessEqual(7, "<="), foTypeEqual(8, "相同"), foTypeUnEqual(9, "不相同"), foSort(9, "排序");

	int code;
	String msg;

	private RuleExprOperation(int code, String msg) {
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