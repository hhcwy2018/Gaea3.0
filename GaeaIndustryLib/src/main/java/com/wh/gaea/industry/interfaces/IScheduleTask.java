package com.wh.gaea.industry.interfaces;

import java.util.Date;

public interface IScheduleTask {
	/**
	 * 订单编号列表，此计划任务可能来源于多个订单，多个订单按交期先后顺序排列
	 * @return
	 */
	String[] orderId();
	/**
	 * 物料编号
	 * @return
	 */
	String materialId();
	/**
	 * 设备编号
	 * @return
	 */
	String deviceId();
	/**
	 * 工位编号
	 * @return
	 */
	String stationId();
	/**
	 * 任务编号，
	 * @return
	 */
	String taskId();
	/**
	 * 操作者编号，如果排程的目标同时还有人的要求
	 * @return
	 */
	String workerId();
	/**
	 * 任务数量
	 * @return
	 */
	float count();
	/**
	 * 任务起始时间
	 * @return
	 */
	Date startTime();
	/**
	 * 任务结束时间，包括非工作时间
	 * @return
	 */
	Date endTime();
	/**
	 * 工时，单位秒
	 * @return
	 */
	float workhours();
}
