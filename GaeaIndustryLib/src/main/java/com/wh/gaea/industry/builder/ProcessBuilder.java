package com.wh.gaea.industry.builder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.ProcessInfo;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;

public class ProcessBuilder  {
    public static class MapInfo{
        public String id;
        public String name;
        public MapInfo(String id, String name){
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    /**
     * 工序信息表，key为工序编号，value为工序信息
     */
    Map<String, ProcessInfo> processMap = new ConcurrentHashMap<>();

    public Map<String, ProcessInfo> builder() throws Exception {

        processMap.clear();

        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();

        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("*");
        sqlBuilder.addTable("gw");
        IDataset dataset = db.query(sqlBuilder);
        for (IRow row: dataset.getRows()) {
            ProcessInfo info = new ProcessInfo();
            info.id = (String)row.getValue("gx_id");
            info.name = (String)row.getValue("gx_name");
            info.code = (String)row.getValue("gx_code");
            info.desc = (String)row.getValue("gx_desc");
            info.type = (String)row.getValue("gx_type");
            processMap.put(info.id, info);
        }

        sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("gx.gxgwgx_gx_id gw_id, gw.gw_id,sb.sb_id");
        sqlBuilder.addTable("gwsbgx LEFT JOIN gxgwgx gx on gx.gxgwgx_gw_id = gwsbgx.gwsbgx_gw_id and gx.deleted = 0 and gwsbgx.deleted = 0 LEFT JOIN gw on gwsbgx.gwsbgx_gw_id = gw.gw_id and gw.deleted = 0 LEFT JOIN sb on gwsbgx.gwsbgx_sb_id = sb.sb_id and sb.deleted = 0");
        dataset = db.query(sqlBuilder);

        for (int i = 0; i < dataset.getRowCount(); i++) {
            IRow row = dataset.getRow(i);
            String processId = (String)row.getValue("gw_id");
            ProcessInfo processInfo = processMap.get(processId);
            if (processInfo == null){
                processInfo = new ProcessInfo();
                processMap.put(processId, processInfo);
            }

            processInfo.stationMap.put((String)row.getValue("gw_id"), (String)row.getValue("gw_id"));
            processInfo.deviceMap.put((String)row.getValue("sb_id"), (String)row.getValue("sb_id"));
            processInfo.station_deviceMap.put((String)row.getValue("gw_id"), (String)row.getValue("sb_id"));
        }

        return processMap;
    }

}

