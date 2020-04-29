package com.wh.gaea.industry.interfaces;

public enum ModuleState {
	dsFree(0, "空闲"), dsUse(1, "使用"), dsMaintain(2, "保养"), dsCheck(3, "检查"), dsRepair(4, "维修"), dsFault(5, "故障"),
	dsAlert(6, "报警");

	int code;
	String msg;

	private ModuleState(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return msg;
	}
}