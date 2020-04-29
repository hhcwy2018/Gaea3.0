package com.wh.gaea.plugin.aps.rule;

public enum OrderTarget{
	otMinSwitchMould(1, "最少换模"),
	otMinSwitchMaterial(2, "最少换料"),
	otOptimalPower(3, "最优效能");
	
	private int code;
	private String msg;

	private OrderTarget(int code, String msg) {
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

	public static OrderTarget fromCode(int code) {
		switch (code) {
		case 1:
			return OrderTarget.otMinSwitchMould;
		case 2:
			return OrderTarget.otMinSwitchMaterial;
		case 3:
		default:
			return OrderTarget.otOptimalPower;
		}
	}
}