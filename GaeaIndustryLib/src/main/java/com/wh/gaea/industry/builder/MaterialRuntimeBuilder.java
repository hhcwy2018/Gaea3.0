package com.wh.gaea.industry.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.MaterialRuntimeInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.parallel.computing.execute.ParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleBatchActionComputer;

import wh.SimpleQuery;
import wh.SimpleQuery.Where;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class MaterialRuntimeBuilder {

    Map<String, MaterialRuntimeInfo> materialMap = new ConcurrentHashMap<>();

    /**
     * 查询指定物料信息
     *
     * @param ids     要查询的物料id列表
     * @param include true查询ids指定的物料信息，false查询不包含ids指定的物料信息
     * @return 物料信息列表
     * @throws Exception
     */
    public Map<String, MaterialRuntimeInfo> builder(String configDBName, Collection<Object> ids, boolean include) throws Exception {
        if (materialMap.size() > 0)
            return materialMap;

        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("*");
        sqlBuilder.addTable("material");
        sqlBuilder.addWhere("used", Operation.otEqual, new Object[]{1});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[]{0});
        if (ids != null && ids.size() > 0) {
            sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
            sqlBuilder.addWhere("material_id", Operation.otIn, ids.toArray());
        }

        IDataset dataset = db.query(sqlBuilder);

        Where where = new SimpleQuery.Where("param_id", Operation.otIn, new Object[] { "select param_id from "
                + configDBName
                + "..dispost_relation where group_id in (select material_group from material)" }, false);
        DynamicParamInfo[] result = DynamicParamInfoBuilder.builder(configDBName, where);
        Map<String, Map<String, DynamicParamInfo>> materialParamMap = new HashMap<>();

        if (result != null) {
            for (DynamicParamInfo info: result) {
                String id = info.id;
                String group = info.group;
                Map<String, DynamicParamInfo> map = materialParamMap.get(group);
                if (map == null){
                    map = new HashMap<>();
                    materialParamMap.put(group, map);
                }
                map.put(id, info);
            }
        }

        Map<String, String> queryMaterialInfoSqls = new ConcurrentHashMap<>();

        for (int i = 0; i < dataset.getRowCount(); i++) {
            IRow row = dataset.getRow(i);
            MaterialRuntimeInfo info = new MaterialRuntimeInfo();
            info.id = (String) row.getValue("material_id");
            info.name = (String) row.getValue("material_name");
            info.group = (String) row.getValue("material_group");
            info.groupCode = (String) row.getValue("group_code");
            info.unit = (String) row.getValue("material_unit");
            info.code = (String) row.getValue("material_code");
            info.type = (String) row.getValue("material_type");
            info.packageUnit = (String) row.getValue("material_volume");
            info.desc = (String) row.getValue("material_desc");
            info.model = (String) row.getValue("material_specification");

            Map<String, DynamicParamInfo> params = materialParamMap.get(info.id);
            if (params != null)
                info.paramDefines.putAll(params);

            materialMap.put(info.id, info);

            queryMaterialInfoSqls.put(info.id, "select * from mt_" + info.group + " where material_id = '" + info.id + "'");
        }

        ParallelComputingExecutor<Map.Entry<String, String>> executor = new ParallelComputingExecutor<>(queryMaterialInfoSqls.entrySet(), 5);
        executor.execute(new ISimpleBatchActionComputer<Map.Entry<String, String>>() {
            @Override
            public void compute(Map.Entry<String, String> t1) throws Throwable {

            }

            @Override
            public void computeBatch(List<Map.Entry<String, String>> sqlMap) throws Throwable {
                String sqls = null;
                for (Map.Entry<String, String> entry : sqlMap) {
                    if (sqls == null)
                        sqls = entry.getValue();
                    else
                        sqls += " union " + entry.getValue();
                }

                IDataset dataset = db.query(sqls, null);
                for (IRow row: dataset.getRows()) {
                    MaterialRuntimeInfo info = materialMap.get(row.getValue("material_id"));
                    if (info == null){}
                    for (IDataset.IColumn column: dataset.getColumns()) {
                        if (column.getName().toLowerCase().contains("param_")){
                            info.paramMap.put(column.getName(), row.getValue(column.getName()));
                        }
                    }
                }
            }

        });

        return materialMap;
    }

}
