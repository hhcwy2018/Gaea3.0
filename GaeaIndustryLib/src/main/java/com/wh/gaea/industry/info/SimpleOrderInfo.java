package com.wh.gaea.industry.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;

public class SimpleOrderInfo implements IOrder {
	String orderId;
	String customerId;
	Date endTime = new Date();
	int workhours = 0;

	Map<String, Object> extendParams = new ConcurrentHashMap<>();

	List<IProduct> products = Collections.synchronizedList(new ArrayList<>());

	public SimpleOrderInfo(String orderId, String customerId, Date endTime, int workhours, Map<String, Object> extendParams,
			IProduct[] products) {
		this.orderId = orderId;
		this.customerId = customerId;
		this.endTime = endTime;
		this.workhours = workhours;
		if (extendParams != null)
			this.extendParams.putAll(extendParams);
		this.products.addAll(Arrays.asList(products));
	}

	@Override
	public String orderId() {
		return orderId;
	}

	@Override
	public String customerId() {
		return customerId;
	}

	@Override
	public String name() {
		return orderId + "[" + customerId + "]";
	}

	@Override
	public Date endTime() {
		return endTime;
	}

	@Override
	public int workhours() {
		return workhours;
	}

	@Override
	public Map<String, Object> extendParams() {
		return new HashMap<String, Object>(extendParams);
	}

	@Override
	public List<IProduct> products() {
		return new ArrayList<>(products);
	}

	@Override
	public String toString() {
		return name();
	}
}
