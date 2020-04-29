package com.wh.gaea.industry.info;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.interfaces.IDInfo;
import com.wh.gaea.industry.interfaces.IDevice;
import com.wh.gaea.industry.interfaces.IMould;

public class DeviceInfo implements IDInfo, IDevice{
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getModel() {
		return model;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getSupplier() {
		return supplier;
	}

	@Override
	public int getState() {
		return state;
	}

	public String id;
	public String name;
	public String model;
	public String type;
	public String code;
	public String supplier;
	public int state;
	public float life;
	public Map<String, IMould> moulds = new ConcurrentHashMap<>();
	public Date start;
	public Date end;

	@Override
	public String toString() {
		return name + "[" + code + "]";
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public float getLife() {
		return life;
	}

	@Override
	public Date getStartTime() {
		return start;
	}

	@Override
	public Date getEndTime() {
		return end;
	}

	@Override
	public IDevice[] getMoulds() {
		return moulds.values().toArray(new IDevice[moulds.size()]);
	}
}