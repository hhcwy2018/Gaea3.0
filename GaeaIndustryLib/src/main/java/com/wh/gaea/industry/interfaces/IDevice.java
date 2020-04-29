package com.wh.gaea.industry.interfaces;

import java.util.Date;

public interface IDevice {

	/**
	 * 空闲中
	 */
	public static final int FREE = 0;
	/**
	 * 使用中
	 */
	public static final int USED = 1;
	/**
	 * 保养中
	 */
	public static final int MAINTAINING = 2;
	/**
	 * 检查中
	 */
	public static final int CHECKING = 3; 
	/**
	 * 维修中
	 */
	public static final int REPAIRING = 4;
	/**
	 * 报警中
	 */
	public static final int ALERTED = 5; 
	/**
	 * 调模中
	 */
	public static final int ADJUSTING = 6; 
	/**
	 * 换模中
	 */
	public static final int EXCHANGING = 7; 
	
	/**
	 * 设备/磨具/道具的编号
	 * @return
	 */
	String getId();

	/**
	 * 名称
	 * @return
	 */
	String getName();

	/**
	 * 设备型号
	 * @return
	 */
	String getModel();

	/**
	 * 设备类型，磨具/道具/机床
	 * @return
	 */
	String getType();

	/**
	 * 设备的代码，一般用于企业内部管理标识
	 * @return
	 */
	String getCode();

	/**
	 * 供应商编码
	 * @return
	 */
	String getSupplier();

	/**
	 * 设备状态，可选值参见列表
	 * @return
	 */
	int getState();
	
	/**
	 * 设备的使用有效期，不是标准，而是实际的有效期
	 * @return
	 */
	float getLife();

	/**
	 * 设备绑定得模具信息
	 * @return
	 */
	IDevice[] getMoulds();
	/**
	 * 设备可以使用的起始时间，要去掉当前还没有完成的任务时间
	 * @return
	 */
	Date getStartTime();
	
	/**
	 * 设备可以使用的终止时间，无终止日期或者由life决定，可以返回null
	 * @return
	 */
	Date getEndTime();
}