package com.wh.gaea.industry.builder;

import java.util.HashMap;
import java.util.Map;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.Customer;
import com.wh.gaea.industry.interfaces.ICustomer;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class CustomerBuilder {

	public interface ISetUserList{
		void addNode(Customer info);
		void onEnd();
	}
	
	public static void builder(String[] customerIds, Map<String, ICustomer> map) throws Exception {
		builder(new ISetUserList() {
			
			@Override
			public void onEnd() {
				
			}
			
			@Override
			public void addNode(Customer info) {
				map.put(info.id, info);
			}
		}, customerIds);
		
	}
	
	public static void builder(CustomerBuilder.ISetUserList setUserList) throws Exception {
		builder(setUserList, null);
	}
	
	public static void builder(CustomerBuilder.ISetUserList setUserList, String[] customerIds) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("kh_Id,kh_Code,kh_Name, bomkhgx_bom_id");
		sqlBuilder.addTable("bomkhgx i left join kh j on i.bomkhgx_kh_id = j.kh_Id");
		sqlBuilder.addWhere("i.deleted", Operation.otEqual, new Object[] { 0 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("i.used", Operation.otEqual, new Object[] { 1 });
		if (customerIds != null && customerIds.length > 0) {
			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
			sqlBuilder.addWhere("kh_Id", Operation.otIn, customerIds);
		}
		IDataset dataset = db.query(sqlBuilder);

		Map<String, Customer> customerMap = new HashMap<>();

		for (IRow row : dataset.getRows()) {
			Customer customer;
			String id = (String) row.getValue("kh_id");
			if (customerMap.containsKey(id))
				customer = customerMap.get(id);
			else {
				customer = new Customer();
				customer.id = (String) row.getValue("kh_id");
				customer.code = (String) row.getValue("kh_code");
				customer.name = (String) row.getValue("kh_name");
				customerMap.put(id, customer);
				setUserList.addNode(customer);
			}
			customer.bomids.add((String) row.getValue("bomkhgx_bom_id"));
		}

		setUserList.onEnd();
	}
}