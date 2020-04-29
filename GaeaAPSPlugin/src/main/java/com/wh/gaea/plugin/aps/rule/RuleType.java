package com.wh.gaea.plugin.aps.rule;

import com.wh.gaea.plugin.aps.interfaces.IRuleBase;

public enum RuleType implements IRuleBase {
	rtGroup("分组规则", 1), rtSort("排序规则", 2), rtFilter("过滤规则", 3), rtTransition("间隔规则", 4),
	rtResorted("重排规则", 5);

	private int code;
	private String msg;

	private RuleType(String msg, int code) {
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

	public static RuleType fromCode(int code) {
		switch (code) {
		case 1:
			return RuleType.rtGroup;
		case 2:
			return RuleType.rtSort;
		case 3:
			return RuleType.rtFilter;
		case 4:
			return RuleType.rtTransition;
		case 5:
		default:
			return RuleType.rtResorted;
		}
	}
}