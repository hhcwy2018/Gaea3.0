package com.wh.gaea.plugin.aps.rule;

import com.wh.gaea.plugin.aps.interfaces.IRuleBase;

public enum RulePart implements IRuleBase {
	rpLoad("任务装载", 0), rpAllotProduction("订单产品预排序", 1), rpAllotProcess("工序展开", 2), rpAllotMachine("设备展开", 3),
	rpSort("任务排序", 4), rpAllotCapacity("生产能力分配", 5);

	private int code;
	private String msg;

	private RulePart(String msg, int code) {
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

	public static RulePart fromCode(int code) {
		switch (code) {
		case 0:
			return RulePart.rpLoad;
		case 1:
			return RulePart.rpAllotProduction;
		case 2:
			return RulePart.rpAllotProcess;
		case 3:
			return RulePart.rpAllotMachine;
		case 4:
			return RulePart.rpSort;
		case 5:
		default:
			return RulePart.rpAllotCapacity;
		}
	}
}