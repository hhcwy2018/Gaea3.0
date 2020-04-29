package com.wh.gaea.industry.interfaces;

import java.util.Map;

import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.info.StationInfo;

public interface IBom {
	
	/**
	 * 此BOM的父BOM编号
	 * @return
	 */
	String getParentBomId();
	
	/**
	 * 此BOM的产品BOM编号
	 * @return
	 */
	String getProductBomId();

	/**
	 * BOM编号
	 * @return
	 */
	String getBomId();
	
	/**
	 * 当前节点的编号，唯一
	 * @return
	 */
	String getId();

	/**
	 * 节点的业务代码
	 * @return
	 */
	String getCode();

	/**
	 * 节点显示名称
	 * @return
	 */
	String getName();

	/**
	 * 关联物料的需求数量
	 * @return
	 */
	float getCount();
	
	/**
	 * 关联的物料编号
	 * @return
	 */
	String getMaterialId();
	
	/**
	 * 关联的工序编号
	 * @return
	 */
	String getProcessId();

	/**
	 * 关联的工序业务编码
	 * @return
	 */
	String getProcessCode();

	/**
	 * 关联的工序名称
	 * @return
	 */
	String getProcessName();

	/**
	 * 关联的工艺流程编号
	 * @return
	 */
	String getProcessFlowId();

	/**
	 * 关联的工位信息列表
	 * @return
	 */
	StationInfo[] getStations();

	/**
	 * 关联的设备列表列表
	 * @return
	 */
	DeviceInfo[] getDevices();

	/**
	 * 子节点列表
	 * @return
	 */
	IBom[] getChilds();

	/**
	 * 获取工时得key
	 * @param deviceId 设备id，必须是当前bom节点绑定得设备id
	 * @param mouldId 模具id，必须是当前bom节点绑定得模具id
	 * @return
	 */
	String getWorkhoursKey(String deviceId, String mouldId);

	/**
	 * 获取此节点对应得工时，key为getWorkhoursKey()方法得返回值，value为工时，单位秒
	 * @return
	 */
	Map<String, Float> getWorkhours();
}