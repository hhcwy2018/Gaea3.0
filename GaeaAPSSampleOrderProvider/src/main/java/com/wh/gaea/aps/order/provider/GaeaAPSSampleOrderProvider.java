package com.wh.gaea.aps.order.provider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.info.DefaultProductInfo;
import com.wh.gaea.industry.info.SimpleOrderInfo;
import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;
import com.wh.gaea.plugin.aps.interfaces.IOrderProvider;

public class GaeaAPSSampleOrderProvider implements IOrderProvider {

	IOrder[] orders = new IOrder[] {
			new SimpleOrderInfo("01", "kh_1565071551540_76", getDate("2020-02-28 12"), 0, null, new IProduct[] {
					new DefaultProductInfo(10, "material_1571395542423_54860", null),
					new DefaultProductInfo(10, "material_1566441393858_1", null),
					new DefaultProductInfo(10, "material_1566441393858_72", null),
			}),
			new SimpleOrderInfo("02", "kh_1565071551540_76", getDate("2020-02-29 10"), 0, null, new IProduct[] {
					new DefaultProductInfo(100, "material_1571395542423_54860", null),
					new DefaultProductInfo(15, "material_1566441393858_54", null),
			}),
			new SimpleOrderInfo("03", "kh_1566992399104_37", getDate("2020-03-10 24"), 0, null, new IProduct[] {
					new DefaultProductInfo(50, "material_1570757803097_1", null),
					new DefaultProductInfo(100, "material_1566441393858_54", null),
			}),
	};

	protected static Date getDate(String dateString) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH").parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Map<String, IOrder> orderMap = new ConcurrentHashMap<>();
	
	public GaeaAPSSampleOrderProvider() {
		for (IOrder order : orders) {
			orderMap.put(order.orderId(), order);
		}
	}
	
	@Override
	public IOrder queryOrder(String orderId) {
		return orderMap.get(orderId);
	}

	@Override
	public IProduct convertProduct(IProduct product) {
		return product;
	}

	@Override
	public IOrder convertOrder(IOrder order) {
		return order;
	}

	@Override
	public String toString() {
		return "订单提供者例子";
	}

	@Override
	public IOrder[] queryOrders() {
		return orders;
	}
}
