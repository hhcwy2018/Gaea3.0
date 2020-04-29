package com.wh.gaea.plugin.aps.interfaces;

import com.wh.gaea.plugin.aps.info.MetaSources;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;

public interface IEngineFilter {
	
	/**
	 * 过滤排程的装载阶段，
	 * @param structInfo 规则架构
	 * @param metaSources 要过滤的数据
	 */
	void load(RuleStructInfo structInfo, MetaSources metaSources) throws Exception;
	
	/**
	 * 订单产品预排序阶段，
	 * @param metaSources 要过滤的数据
	 */
	void orderProduction(MetaSources metaSources) throws Exception;

	/**
	 * 展开工序阶段
	 * @param metaSources 要过滤的数据
	 */
	void expandProcess(MetaSources metaSources) throws Exception;

	/**
	 * 展开设备阶段
	 * @param structInfo 规则架构
	 * @param metaSources 要过滤的数据
	 */
	void expandMachine(RuleStructInfo structInfo, MetaSources metaSources) throws Exception;

	/**
	 * 排序阶段
	 * @param structInfo 规则架构
	 * @param metaSources 要过滤的数据
	 */
	void sort(RuleStructInfo structInfo, MetaSources metaSources) throws Exception;

	/**
	 * 班组及设备能力分配阶段
	 * @param structInfo 规则架构
	 * @param metaSources 要过滤的数据
	 */
	void allotCapacity(RuleStructInfo structInfo, MetaSources metaSources) throws Exception;

	/**
	 * 排程结果分发阶段
	 * @param structInfo 规则架构
	 * @param metaSources 要过滤的数据
	 */
	void publish(RuleStructInfo structInfo, MetaSources metaSources) throws Exception;
}
