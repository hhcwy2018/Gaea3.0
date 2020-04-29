package com.wh.gaea.industry.builder;

import java.util.ArrayList;
import java.util.List;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.interfaces.DynamicGroupInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.tools.BooleanHelper;
import com.wh.tools.NumberHelper;

import wh.SimpleQuery.Where;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class DynamicParamInfoBuilder {
	
	protected static DynamicParamInfo[] toDynamicParamInfos(IDataset dataset) {
		List<DynamicParamInfo> result = new ArrayList<>();
		for (int i = 0; i < dataset.getRowCount(); i++) {
			result.add(toDynamicParamInfo(dataset.getRow(i)));
		}

		return result.toArray(new DynamicParamInfo[result.size()]);
	}

	protected static DynamicParamInfo toDynamicParamInfo(IRow row) {
		DynamicParamInfo info = new DynamicParamInfo();
		info.id = (String) row.getValue("param_id");
		info.code = (String) row.getValue("param_code");
		info.name = (String) row.getValue("param_name");
		info.dateType = (String) row.getValue("param_type");
		info.precision = NumberHelper.convertToNumber(row.getValue("param_precision"), Integer.class);
		info.size = NumberHelper.convertToNumber(row.getValue("param_length"), Integer.class);
		info.max = NumberHelper.convertToNumber(row.getValue("param_maxvalue"), Float.class);
		info.min = NumberHelper.convertToNumber(row.getValue("param_minvalue"), Float.class);
		info.values = (String) row.getValue("param_values");
		info.valueList = (String) row.getValue("param_valuelist");
		info.must = BooleanHelper.convertToBoolean(row.getValue("param_must"));
		info.defaultValue = (String) row.getValue("param_default");
		return info;
	}
	
	public static DynamicParamInfo[] builder(String configDBName, Where where) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable(configDBName + "..dispost_param");
		if (where != null) {
			sqlBuilder.addWhere(where.field, where.operation, where.value, where.transferred);
			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		}
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		IDataset dataset = db.query(sqlBuilder);

		return toDynamicParamInfos(dataset);
	}

	public enum GroupType {
		gtDevice(0, "设备"), gtMaterial(1, "物料"), gtMaintain(2, "保养"), gtCheck(3, "检查"), gtDeviceCraft(4, "工艺参数"),
		gtCraftFlow(5, "工艺流程");

		private int code;
		private String msg;

		private GroupType(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}

	}

	protected static DynamicGroupInfo[] queryGroups(String configDBName, String key) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("group_id, group_code, group_name");
		sqlBuilder.addTable(configDBName + "..dispost_group");
		sqlBuilder.addWhere("group_type", Operation.otEqual, new Object[] { key });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		IDataset dataset = db.query(sqlBuilder);

		List<DynamicGroupInfo> result = new ArrayList<>();
		for (int i = 0; i < dataset.getRowCount(); i++) {
			IRow row = dataset.getRow(i);
			DynamicGroupInfo info = new DynamicGroupInfo();
			info.id = (String) row.getValue("group_id");
			info.code = (String) row.getValue("group_code");
			info.name = (String) row.getValue("group_name");
			result.add(info);
		}

		return result.toArray(new DynamicGroupInfo[result.size()]);
	}

	public static DynamicGroupInfo[] queryGroups(String configDBName, GroupType groupType) throws Exception {
		switch (groupType) {
		case gtCheck:
			return queryGroups(configDBName, "check");
		case gtCraftFlow:
			return queryGroups(configDBName, "craft");
		case gtDevice:
			return queryGroups(configDBName, "device");
		case gtDeviceCraft:
			return queryGroups(configDBName, "deviceCraft");
		case gtMaintain:
			return queryGroups(configDBName, "maintain");
		case gtMaterial:
			return queryGroups(configDBName, "material");
		}

		throw new Exception("不支持的组类型！");
	}

	public static DynamicParamInfo[] queryGroupParams(String configDBName, String group_id) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("t2.*");
		sqlBuilder.addTable(configDBName + "..dispost_relation t left join " + configDBName
				+ "..dispost_param t2 on t.param_id = t2.param_id");
		sqlBuilder.addWhere("t.group_id", Operation.otEqual, new Object[] { group_id });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("t.deleted", Operation.otEqual, new Object[] { 0 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("t2.deleted", Operation.otEqual, new Object[] { 0 });
		IDataset dataset = db.query(sqlBuilder);

		return toDynamicParamInfos(dataset);
	}

}
