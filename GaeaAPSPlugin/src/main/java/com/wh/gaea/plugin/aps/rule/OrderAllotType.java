package com.wh.gaea.plugin.aps.rule;

public enum OrderAllotType{
	/**
	 * 平均分配，使每个设备尽可能平均分配任务
	 */
	otAvg(1, "平均分配"), 
	/**
	 * 贪婪分配，先填充慢一个设备后，再填充下一个
	 */
	otGreed(2, "贪婪分配");
	
	private int code;
	private String msg;

	private OrderAllotType(int code, String msg) {
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

	public static OrderAllotType fromCode(int code) {
		switch (code) {
		case 1:
			return OrderAllotType.otAvg;
		case 2:
		default:
			return OrderAllotType.otGreed;
		}
	}
}