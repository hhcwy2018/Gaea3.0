package com.wh.gaea.industry.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IOrder {
	/**
	 * 订单编号
	 * @return
	 */
	String orderId();
	
	/**
	 * 订单的客户编号
	 * @return
	 */
	String customerId();
	
	/**
	 * 订单名字，可以为null
	 * @return
	 */
	String name();
	
	/**
	 * 订单交付时间
	 * @return
	 */
	Date endTime();
	
	/**
	 * 订单工时
	 * @return
	 */
	int workhours();
	
	/**
	 * 订单的扩展参数列表
	 * @return
	 */
	Map<String, Object> extendParams();
	
	/**
	 * 订单包括的产品信息
	 * @return
	 */
	List<IProduct> products();
}
