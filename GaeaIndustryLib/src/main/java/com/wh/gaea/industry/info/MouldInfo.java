package com.wh.gaea.industry.info;

import com.wh.gaea.industry.interfaces.IMould;

public class MouldInfo extends DeviceInfo implements IMould{
	public float _adjustTimes;
	public float _switchTimes;
	
	@Override
	public float adjustTimes() {
		return _adjustTimes;
	}

	@Override
	public float switchTimes() {
		return _switchTimes;
	}

}
