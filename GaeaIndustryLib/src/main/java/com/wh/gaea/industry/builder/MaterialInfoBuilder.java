package com.wh.gaea.industry.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.MaterialInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.tools.MapHelper;

import wh.SimpleQuery;
import wh.SimpleQuery.Where;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class MaterialInfoBuilder {

	Map<String, MaterialInfo> materialMap = new ConcurrentHashMap<>();
	Map<String, DynamicParamInfo[]> materialParamMap = new ConcurrentHashMap<>();

	/**
	 * 查询指定物料信息
	 * @param ids 要查询的物料id列表
	 * @param include true查询ids指定的物料信息，false查询不包含ids指定的物料信息
	 * @return 物料信息列表
	 * @throws Exception
	 */
	public MaterialInfo[] builder(String configDBName, Collection<Object> ids, boolean include) throws Exception {
		builderMap(configDBName);
		Collection<MaterialInfo> result = MapHelper.queryMap(materialMap, ids, include);
		return result.toArray(new MaterialInfo[result.size()]);
	}

	void builderMap(String configDBName) throws Exception {
		if (materialMap.size() == 0) {

			IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
			ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addField("*");
			sqlBuilder.addTable("material");
			sqlBuilder.addWhere("used", Operation.otEqual, new Object[] { 1 });
			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
			sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
//		if (materialId != null && !materialId.isEmpty()) {
//			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
//			sqlBuilder.addWhere("material_id", Operation.otEqual, new Object[] { materialId });
//		}

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

			for (int i = 0; i < dataset.getRowCount(); i++) {
				IRow row = dataset.getRow(i);
				MaterialInfo info = new MaterialInfo();
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
				Map<String, DynamicParamInfo> map = materialParamMap.get(info.group);
				if (map != null)
					info.paramMap.putAll(map);

				materialMap.put(info.id, info);
			}
		}
	}

	/**
	 * 查询所有物料信息
	 * @return 物料信息列表
	 * @throws Exception
	 */
	public MaterialInfo[] builder(String configDBName) throws Exception {
		builderMap(configDBName);
		return materialMap.values().toArray(new MaterialInfo[materialMap.size()]);
	}

	/**
	 * 查询指定物料的自定义属性信息
	 * @param materialId 要查询的物料编号
	 * @param configDBName 配置数据库名称
	 * @return 自定义属性列表
	 * @throws Exception
	 */
	public DynamicParamInfo[] builder(String materialId, String configDBName) throws Exception {
		if (materialParamMap.containsKey(materialId))
			return materialParamMap.get(materialId);
		
		Where where = new SimpleQuery.Where("param_id", Operation.otIn, new Object[] { "select param_id from "
				+ configDBName
				+ "..dispost_relation where group_id = (select material_group from material where material_id = '"
				+ materialId + "')" }, false);
		DynamicParamInfo[] result = DynamicParamInfoBuilder.builder(configDBName, where);
		if (result != null) {
			materialParamMap.put(materialId, result);
		}
		
		return result;
	}

}
