package com.wh.gaea.industry.builder;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.BomInfo;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class BomOper {
	public interface ISetBomList{
		void addNode(BomInfo info);
		void onBomList();
	}
	
	public void initBomList(String[] bomid, BomOper.ISetBomList setBomList) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("bom");
		if (bomid != null && bomid.length > 0) {
			sqlBuilder.addWhere("bom_id", Operation.otIn, bomid);
			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		}
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("used", Operation.otEqual, new Object[] { 1 });
		IDataset dataset = db.query(sqlBuilder);

		if (dataset.getRowCount() > 0)
			for (IRow row : dataset.getRows()) {
				BomInfo info = new BomInfo();
				info.id = row.getValue("bom_id").toString();
				info.code = row.getValue("bom_code").toString();
				info.name = row.getValue("bom_name").toString();
				setBomList.addNode(info);
			}
		setBomList.onBomList();
	}

	public String getBomId(String bomcode) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("bom");
		sqlBuilder.addWhere("bom_code", Operation.otIn, new Object[] { bomcode });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("used", Operation.otEqual, new Object[] { 1 });
		IDataset dataset = db.query(sqlBuilder);

		if (dataset.getRowCount() == 0)
			return null;

		return dataset.getRow(0).getValue("bom_id").toString();
	}

}