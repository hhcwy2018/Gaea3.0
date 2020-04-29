package com.wh.gaea.industry.builder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.info.MouldInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.gaea.industry.interfaces.IDevice;
import com.wh.tools.MapHelper;

import wh.SimpleQuery;
import wh.SimpleQuery.Where;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class DeviceBuilder {
	Map<String, IDevice> deviceMap = new ConcurrentHashMap<>();
	Map<String, DynamicParamInfo[]> deviceParaMap = new ConcurrentHashMap<>();

	Map<String, IDevice> moduleMap = new ConcurrentHashMap<>();
	Map<String, DynamicParamInfo[]> moduleParaMap = new ConcurrentHashMap<>();
	
	protected void initMap(DeviceInfo[] infos, Map<String, IDevice> map) {
		for (DeviceInfo deviceInfo : infos) {
			map.put(deviceInfo.id, deviceInfo);
		}
	}
	
	public IDevice[] queryEquipments(Collection<Object> ids, boolean include) throws Exception {
		Collection<IDevice> result = MapHelper.queryMap(deviceMap, ids, include);
		return result.toArray(new DeviceInfo[result.size()]);
	}
	
	public IDevice[] queryModules(Collection<Object> ids, boolean include) throws Exception {
		Collection<IDevice> result = MapHelper.queryMap(moduleMap, ids, include);
		return result.toArray(new DeviceInfo[result.size()]);
	}
	
	public void queryEquipments(String[] ids, Map<String, IDevice> map) throws Exception {
		DeviceInfo[] result = queryDevices(ids, "mjsb", false);
		initMap(result, map);
	}
	
	public IDevice[] queryEquipments() throws Exception {
		DeviceInfo[] result = queryDevices(null, "mjsb", false);
		initMap(result, deviceMap);
		return result;
	}

	public Map<String, DeviceInfo> queryEquipmentMap() throws Exception {
		DeviceInfo[] devices = queryDevices(null, "mjsb", false);

		Map<String, DeviceInfo> result = new HashMap<>();
		for (DeviceInfo info: devices) {
			result.put(info.id, info);
		}
		return result;
	}

	public void queryMoulds(String[] ids, Map<String, IDevice> map) throws Exception {
		DeviceInfo[] result = queryDevices(ids, "mjsb", true);
		initMap(result, map);
	}
	
	public IDevice[] queryMoulds() throws Exception {
		DeviceInfo[] result = queryDevices(null, "mjsb", true);
		initMap(result, moduleMap);
		return result;
	}

	public static MouldInfo fillMouldInfo(IRow row) throws Exception {
		MouldInfo info = fillDeviceInfo(MouldInfo.class, row);
		info._adjustTimes = (float)row.getValue("bd_tm_date");
		info._switchTimes = (float)row.getValue("bd_hm_date");
		return info;
	}
	
	public static DeviceInfo fillDeviceInfo(IRow row) throws Exception {
		return fillDeviceInfo(DeviceInfo.class, row);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends DeviceInfo> T fillDeviceInfo(Class<T> c, IRow row) throws Exception {
		DeviceInfo info = c.newInstance();
		info.id = (String) row.getValue("sb_id");
		info.name = (String) row.getValue("sb_name");
//		info.group = (String) row.getValue("sb_group");
		info.model = (String) row.getValue("sb_xh");
		info.type = (String) row.getValue("sb_type");
		info.code = (String) row.getValue("sb_code");
		info.supplier = (String) row.getValue("sb_gys");
		info.state = Integer.parseInt(row.getValue("sb_status").toString());
		info.start = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(info.start);
		calendar.add(Calendar.YEAR, 1);
		info.end = calendar.getTime();
		return (T) info;
	}
	
	public static DeviceInfo[] queryDevices(String[] deviceIds, String type, boolean include) throws Exception {
		
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("sb");
		sqlBuilder.addWhere("used", Operation.otEqual, new Object[] { 1 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		if (deviceIds != null && deviceIds.length > 0) {
			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
			sqlBuilder.addWhere("sb_id", Operation.otIn, deviceIds);
		}

		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		if (include) {
			sqlBuilder.addWhere("sb_type", Operation.otEqual, new Object[] { type });
		} else {
			sqlBuilder.addWhere("sb_type", Operation.otUnequal, new Object[] { type });
		}

		IDataset dataset = db.query(sqlBuilder);

		List<DeviceInfo> result = new ArrayList<>();
		for (int i = 0; i < dataset.getRowCount(); i++) {
			result.add(fillDeviceInfo(dataset.getRow(i)));
		}

		return result.toArray(new DeviceInfo[result.size()]);
	}

	public static DynamicParamInfo[] queryDynamicParams(String deviceId, String configDBName, Map<String, DynamicParamInfo[]> map) throws Exception {
		if (map.containsKey(deviceId))
			return map.get(deviceId);
		
		Where where = new SimpleQuery.Where("param_id", Operation.otIn,
				new Object[] { "select param_id from " + configDBName
						+ "..dispost_relation where group_id = (select sb_group from sb where sb_id = '" + deviceId
						+ "')" },
				false);
		DynamicParamInfo[] result = DynamicParamInfoBuilder.builder(configDBName, where);
		map.put(deviceId, result);
		return result;
	}

	public DynamicParamInfo[] queryDeviceParams(String deviceId, String configDBName) throws Exception {
		return queryDynamicParams(deviceId, configDBName, deviceParaMap);
	}
	
	public DynamicParamInfo[] queryModuleParams(String moduleId, String configDBName) throws Exception {
		return queryDynamicParams(moduleId, configDBName, moduleParaMap);
	}

}
