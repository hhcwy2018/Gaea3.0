package com.wh.gaea.plugin.aps.rule;

import com.wh.gaea.plugin.aps.interfaces.IRuleBase;

public enum RulePeriod implements IRuleBase {
	rsAll("全部任务", 0), rsNotApply("仅未应用同类规则的任务", 1), rsApply("仅已应用同类规则的任务", 2);

	private int code;
	private String msg;

	private RulePeriod(String msg, int code) {
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

	public static RulePeriod fromCode(int code) {
		switch (code) {
		case 1:
			return RulePeriod.rsNotApply;
		case 2:
			return RulePeriod.rsApply;
		case 0:
		default:
			return RulePeriod.rsAll;
		}
	}
}