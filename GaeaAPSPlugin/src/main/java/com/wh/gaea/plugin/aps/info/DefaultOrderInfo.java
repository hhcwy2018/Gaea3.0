package com.wh.gaea.plugin.aps.info;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;
import com.wh.gaea.plugin.aps.interfaces.IOrderProvider;

public class DefaultOrderInfo implements IOrder{
	public IOrder order;
	public Map<String, Object> extendParams = new ConcurrentHashMap<>();
	public Map<String, IProduct> products = new ConcurrentHashMap<>();
	
	protected void setProducts(IOrderProvider provider, String orderId) {
		order = provider.queryOrder(orderId);
	}
	
	public DefaultOrderInfo(IOrderProvider provider, String orderId) {
		setProducts(provider, orderId);
	}
	
	@Override
	public String toString() {
		return name();
	}

	@Override
	public String orderId() {
		return order.orderId();
	}

	@Override
	public String customerId() {
		return order.customerId();
	}

	@Override
	public String name() {
		return orderId() + "[" + customerId() + "]";
	}

	@Override
	public Map<String, Object> extendParams() {
		return extendParams;
	}

	@Override
	public List<IProduct> products() {
		return new ArrayList<IProduct>(products.values());
	}

	@Override
	public Date endTime() {
		return order.endTime();
	}

	@Override
	public int workhours() {
		return order.workhours();
	}
}