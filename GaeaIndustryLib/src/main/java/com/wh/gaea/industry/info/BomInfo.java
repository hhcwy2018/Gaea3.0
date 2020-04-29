package com.wh.gaea.industry.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.interfaces.IBom;

public class BomInfo implements IBom {
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getProcessId() {
		return processId;
	}

	@Override
	public StationInfo[] getStations() {
		return stations == null || stations.size() == 0 ? null : stations.values().toArray(new StationInfo[stations.size()]);
	}

	@Override
	public DeviceInfo[] getDevices() {
		return devices == null || devices.size() == 0 ? null : devices.values().toArray(new DeviceInfo[devices.size()]);
	}

	@Override
	public IBom[] getChilds() {
		return childs == null || childs.size() == 0 ? null : childs.toArray(new IBom[childs.size()]);
	}
	
	public float count;
	public String bomId;
	public String id;
	public String pid;
	public String code;
	public String name;
	public String materialId;
	public String processId;
	public String processCode;
	public String processName;
	public String processFlowId;
	public Map<String, StationInfo> stations = new ConcurrentHashMap<>();
	public Map<String, DeviceInfo> devices = new ConcurrentHashMap<>();
	public List<IBom> childs = new ArrayList<>();
	public Map<String, Float> workhoursMap = new ConcurrentHashMap<>();
	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getMaterialId() {
		return materialId;
	}

	@Override
	public String getBomId() {
		return id;
	}

	@Override
	public float getCount() {
		return count;
	}

	@Override
	public String getProcessFlowId() {
		return processFlowId;
	}

	@Override
	public String getProcessCode() {
		return processCode;
	}

	@Override
	public String getProcessName() {
		return processName;
	}

	@Override
	public String getWorkhoursKey(String deviceId, String mouldId) {
		return (deviceId == null || deviceId.trim().isEmpty() ? "" : deviceId.trim().toLowerCase())+
				(mouldId == null || mouldId.trim().isEmpty() ? "" : mouldId.trim().toLowerCase());
	}

	@Override
	public Map<String, Float> getWorkhours() {
		return workhoursMap;
	}

	@Override
	public String getParentBomId() {
		return pid;
	}

	@Override
	public String getProductBomId() {
		return bomId;
	}

}