package com.wh.gaea.industry.interfaces;

import java.util.Date;
import java.util.List;

public interface IWorker {
	
	/**
	 * 表示一个人一个工作周期，start-end可以跨天、年、月
	 * @author wy
	 *
	 */
	public static class WorkTime{
		/**
		 * 工作起始时间（包含），精确到分钟
		 */
		public Date start;
		/**
		 * 工作终止时间（不包含），精确到分钟
		 */
		public Date end;
		
		public WorkTime(Date start, Date end) {
			this.start = start;
			this.end = end;
		}
	}
	
	/**
	 * 人员编号
	 * @return
	 */
	String id();
	
	/**
	 * 人员名称
	 * @return
	 */
	String name();
	
	/**
	 * 所属部门
	 * @return
	 */
	String part();
	
	/**
	 * 职位
	 * @return
	 */
	String post();
	
	/**
	 * 可以使用的设备编号列表
	 * @return
	 */
	List<String> deviceIds();
	
	/**
	 * 所属的工位编号
	 * @return
	 */
	List<String> stationIds();
	
	/**
	 * 在排程区间可以工作的时间列表，每个日期
	 * @return
	 */
	List<WorkTime> dates();
}
