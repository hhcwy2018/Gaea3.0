package com.wh.gaea.industry.info;

import java.util.ArrayList;
import java.util.List;

import com.wh.gaea.industry.interfaces.ICustomer;

public class Customer implements ICustomer {
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getBomids() {
		return bomids;
	}

	public String id;
	public String code;
	public String name;
	public List<String> bomids = new ArrayList<>();

	@Override
	public String toString() {
		return name + "【" + code + "】";
	}
}