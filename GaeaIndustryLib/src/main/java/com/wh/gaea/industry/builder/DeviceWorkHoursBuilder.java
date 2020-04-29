package com.wh.gaea.industry.builder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.GlobalInstance;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.ISqlBuilder;

public class DeviceWorkHoursBuilder {

    /**
     * 设备工时，key为设备id，value为此设备下产品与工时的映射关系（key为物料编码，value为工时，单位秒）
     */
    public Map<String, Map<String, Float>> device_workhourMap = new ConcurrentHashMap<>();

    public void fillInfo(IDataset.IRow row) throws Exception {
        String deviceId = (String) row.getValue("bzgsjl_sb_id");
        String bommxId = (String) row.getValue("bommx_Id");
        String tmp = (String)row.getValue("bzgsjl_gs");
        if (tmp == null || tmp.trim().isEmpty())
            return;

        float workhours = Float.parseFloat(tmp);

        Map<String, Float> map = device_workhourMap.get(deviceId);
        if (map == null){
            map = new ConcurrentHashMap<>();
            device_workhourMap.put(deviceId, map);
        }

        map.put(bommxId, workhours);
    }

    public Map<String, Map<String, Float>> builder(String[] deviceIds) throws Exception {
        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("bzgsjl_sb_id, t3.material_id,t3.material_code,t2.bommx_Id,t.bzgsjl_gs");
        sqlBuilder.addTable("bzgsjl t left join bommx t2 on t.bzgsjl_bommx_id = t2.bommx_Id left join material t3 on t2.bommx_wl_Id=t3.material_id");
        sqlBuilder.addWhere("t.used", ISqlBuilder.Operation.otEqual, new Object[] { 1 });
        sqlBuilder.addLogicalOperation(ISqlBuilder.LogicalOperation.otAnd);
        sqlBuilder.addWhere("t.deleted", ISqlBuilder.Operation.otEqual, new Object[] { 0 });
        if (deviceIds != null && deviceIds.length > 0) {
            sqlBuilder.addLogicalOperation(ISqlBuilder.LogicalOperation.otAnd);
            sqlBuilder.addWhere("bzgsjl_sb_id", ISqlBuilder.Operation.otIn, deviceIds);
        }

        IDataset dataset = db.query(sqlBuilder);

        for (int i = 0; i < dataset.getRowCount(); i++) {
            fillInfo(dataset.getRow(i));
        }

        return device_workhourMap;

    }
}
