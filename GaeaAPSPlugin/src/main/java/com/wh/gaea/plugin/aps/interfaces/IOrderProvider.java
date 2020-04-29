package com.wh.gaea.plugin.aps.interfaces;

import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;

public interface IOrderProvider {
	IOrder[] queryOrders();
	IOrder queryOrder(String orderId);
	IProduct convertProduct(IProduct product);
	IOrder convertOrder(IOrder order);
}
