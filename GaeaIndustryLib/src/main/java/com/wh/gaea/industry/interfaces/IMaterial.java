package com.wh.gaea.industry.interfaces;

import java.util.Map;

public interface IMaterial {
	/**
	 * 物料内码，唯一
	 * @return
	 */
	String id();
	
	/**
	 * 物料名称
	 * @return
	 */
	String name();
	
	/**
	 * 物料组名称
	 * @return
	 */
	String group();
	
	/**
	 * 物料组编号
	 * @return
	 */
	String groupCode();
	
	/**
	 * 物料单位
	 * @return
	 */
	String unit();
	
	/**
	 * 物料类型
	 * @return
	 */
	String type();
	
	/**
	 * 物料编码，不同于物料id，此未业务编码，可重复
	 * @return
	 */
	String code();
	
	/**
	 * 物料供应商编码列表
	 * @return
	 */
	String[] supplier();
	
	/**
	 * 物料型号
	 * @return
	 */
	String model();
	
	/**
	 * 物料打包单位
	 * @return
	 */
	String packageUnit();
	
	/**
	 * 物料说明
	 * @return
	 */
	String desc();

	/**
	 * 其他扩展参数
	 * @return
	 */
	Map<String, DynamicParamInfo> paramMap();
}
