package com.wh.gaea.plugin.aps.rule;

import com.wh.gaea.plugin.aps.interfaces.IRuleBase;

public enum RuleScope implements IRuleBase {
	rsGlobal("全局", 0), rsBOM("仅BOM", 1), rsNode("仅节点", 2);

	private int code;
	private String msg;

	private RuleScope(String msg, int code) {
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

	public static RuleScope fromCode(int code) {
		switch (code) {
		case 1:
			return RuleScope.rsBOM;
		case 2:
			return RuleScope.rsNode;
		case 0:
		default:
			return RuleScope.rsGlobal;
		}
	}
}