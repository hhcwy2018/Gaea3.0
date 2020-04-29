package com.wh.gaea.industry.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.industry.info.BomInfo;
import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.info.MouldInfo;
import com.wh.gaea.industry.info.StationInfo;
import com.wh.gaea.industry.interfaces.IBom;
import com.wh.gaea.industry.interfaces.IDevice;
import com.wh.parallel.computing.execute.ParallelComputingCaller;
import com.wh.parallel.computing.execute.ParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;
import com.wh.parallel.computing.interfaces.ISimpleCallComputer;
import com.wh.swing.tools.tree.TreeHelp;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class BOMBuilder {
    public static class TreeInfo {
        public Object name;
        public String id;
        public String pid;
        public String bom_id;
        public String bommx_id;
        public String materialId;

        @Override
        public String toString() {
            return name.toString();
        }
    }

    Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
    Map<String, List<IRow>> childMap = new HashMap<>();

    protected DefaultMutableTreeNode createTreeNode(IRow row, DefaultTreeModel model) {
        BOMBuilder.TreeInfo info = new TreeInfo();
        info.id = row.getValue("bommx_id").toString();
        info.pid = row.getValue("bommx_fid") == null ? null : row.getValue("bommx_fid").toString();
        info.name = row.getValue("bommx_name").toString();
        info.bommx_id = info.id;
        info.bom_id = row.getValue("bommx_bom_id") != null ? row.getValue("bommx_bom_id").toString() : null;
        info.materialId = row.getValue("bommx_wl_id").toString();

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
        nodeMap.put(info.id, node);
        DefaultMutableTreeNode parent = nodeMap.get(info.pid);
        if (parent == null)
            parent = (DefaultMutableTreeNode) model.getRoot();
        model.insertNodeInto(node, parent, model.getChildCount(parent));

        return node;
    }

    protected IBom createBom(IRow row) {
        BomInfo info = new BomInfo();
        info.id = row.getValue("bommx_id").toString();
        info.pid = row.getValue("bommx_fid") == null ? null : row.getValue("bommx_fid").toString();
        info.name = row.getValue("bommx_name").toString();
        info.bomId = row.getValue("bommx_bom_id") != null ? row.getValue("bommx_bom_id").toString() : null;
        info.materialId = row.getValue("bommx_wl_id").toString();
        info.processFlowId = row.getValue("gylc_id") == null ? null : row.getValue("gylc_id").toString();
        info.processId = row.getValue("gx_id") == null ? null : row.getValue("gx_id").toString();
        info.processCode = row.getValue("gx_code") == null ? null : row.getValue("gx_code").toString();
        info.processName = row.getValue("gx_name") == null ? null : row.getValue("gx_name").toString();
        info.count = Float.parseFloat(row.getValue("bommx_m_count").toString());

        return info;
    }

    protected void createTree(IRow root, DefaultTreeModel model) {
        DefaultMutableTreeNode node = createTreeNode(root, model);
        String id = root.getValue("bommx_id").toString();
        nodeMap.put(id, node);
        if (childMap.containsKey(id))
            for (IRow row : childMap.get(id)) {
                createTree(row, model);
            }
    }

    protected IBom createTree(IRow row, IBom parent) {
        IBom bom = createBom(row);

        if (parent != null) {
            BomInfo info = (BomInfo) parent;
            info.childs.add(bom);
        }

        List<IRow> childs = childMap.get(bom.getId());
        if (childs != null && childs.size() > 0) {
            for (IRow iRow : childs) {
                createTree(iRow, bom);
            }
        }

        return bom;
    }

    public void builder(String bomid, JTree bomTree) throws Exception {
        builder(bomid, bomTree, true);
    }

    protected IRow buildMap(List<IRow> dataset) {
        IRow root = null;

        if (dataset.size() > 0) {

            for (IRow row : dataset) {
                Object obj = row.getValue("bommx_fid");
                String fid = "";
                if (obj != null)
                    fid = obj.toString().trim();

                if (fid == null || fid.isEmpty())
                    root = row;

                List<IRow> childs;

                if (childMap.containsKey(fid)) {
                    childs = childMap.get(fid);
                } else {
                    childs = new ArrayList<>();
                    childMap.put(fid, childs);
                }
                childs.add(row);
            }
        }

        return root;
    }

    public void builder(List<IRow> dataset, DefaultTreeModel model) throws Exception {

        IRow root = buildMap(dataset);
        if (root != null) {
            createTree(root, model);
        }

    }

    public IBom builder(List<IRow> dataset) throws Exception {

        IRow root = buildMap(dataset);
        if (root != null) {
            return createTree(root, (IBom) null);
        }

        return null;
    }

    public void builder(List<IRow> dataset, JTree bomTree, boolean clear) throws Exception {

        DefaultTreeModel model = (DefaultTreeModel) bomTree.getModel();
        if (clear || model == null)
            model = new DefaultTreeModel(new DefaultMutableTreeNode());

        builder(dataset, model);

        bomTree.setModel(model);
        TreeHelp.expandOrCollapse(bomTree, (DefaultMutableTreeNode) model.getRoot(), true);
    }

    public void builder(String bomid, JTree bomTree, boolean clear) throws Exception {
        DefaultTreeModel model = (DefaultTreeModel) bomTree.getModel();
        if (clear || model == null)
            model = new DefaultTreeModel(new DefaultMutableTreeNode());

        builder(bomid, model);

        bomTree.setModel(model);
    }

    public void builder(String bomid, DefaultTreeModel model) throws Exception {
        Map<String, DefaultTreeModel> models = builder(new String[]{bomid});
        if (models.size() > 0) {
            model.setRoot((TreeNode) models.get(models.keySet().iterator().next()).getRoot());
        }
    }

    public Map<String, DefaultTreeModel> builder(String[] bomids) throws Exception {

        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("*");
        sqlBuilder.addTable("bommx");
        sqlBuilder.addWhere("bommx_bom_id", Operation.otIn, bomids);
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("used", Operation.otEqual, new Object[]{1});
        IDataset dataset = db.query(sqlBuilder);

        Map<String, List<IRow>> bomTrees = new HashMap<>();
        for (IRow row : dataset.getRows()) {
            String bomid = (String) row.getValue("bommx_bom_id");
            List<IRow> rows = bomTrees.get(bomid);
            if (rows == null) {
                rows = new ArrayList<>();
                bomTrees.put(bomid, rows);
            }

            rows.add(row);
        }

        Map<String, DefaultTreeModel> models = new ConcurrentHashMap<>();

        ParallelComputingCaller<List<IRow>> caller = new ParallelComputingCaller<>(new ArrayList<>(bomTrees.values()), 1);

        return caller.submit(new ISimpleCallComputer<List<IRow>, DefaultTreeModel, Map<String, DefaultTreeModel>>() {

            @Override
            public DefaultTreeModel compute(List<IRow> rows) throws Exception {
                if (rows.size() == 0)
                    return null;

                DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
                builder(rows, model);
                String bomid = (String) rows.get(0).getValue("bommx_bom_id");
                models.put(bomid, model);
                return model;
            }

            @Override
            public Map<String, DefaultTreeModel> get() {
                return models;
            }
        });

    }

    protected void buildBOMBaseInfo(String[] bomids, Map<String, IBom> boms) throws Exception {
        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("i.*,j.gx_code,j.gx_name");
        sqlBuilder.addTable("bommx i left join gx j on i.gx_id = j.gx_id");
        sqlBuilder.addWhere("bommx_bom_id", Operation.otIn, bomids);
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.used", Operation.otEqual, new Object[]{1});
        IDataset dataset = db.query(sqlBuilder);
        if (dataset.getRowCount() == 0)
            return;

        Map<String, List<IRow>> bomTrees = new HashMap<>();
        for (IRow row : dataset.getRows()) {
            String bomid = (String) row.getValue("bommx_bom_id");
            List<IRow> rows = bomTrees.get(bomid);
            if (rows == null) {
                rows = new ArrayList<>();
                bomTrees.put(bomid, rows);
            }

            rows.add(row);
        }

        ParallelComputingExecutor<List<IRow>> executor = new ParallelComputingExecutor<>(
                new ArrayList<>(bomTrees.values()), 1);
        executor.execute(new ISimpleActionComputer<List<IRow>>() {

            @Override
            public void compute(List<IRow> rows) throws Exception {
                if (rows.size() == 0)
                    return;

                String bomid = (String) rows.get(0).getValue("bommx_bom_id");

                boms.put(bomid, builder(rows));
            }
        });

    }

    protected void getBOMNotLeafNodeIds(IBom[] boms, Map<String, IBom> resultNodes) throws Exception {
        if (boms == null || boms.length == 0)
            return;

        for (IBom iBom : boms) {
            if (iBom.getChilds() != null && iBom.getChilds().length > 0) {
                resultNodes.put(iBom.getId(), iBom);
                getBOMNotLeafNodeIds(iBom.getChilds(), resultNodes);
            }
        }
    }

    protected void buildBOMWorkhours(String[] bomids, Map<String, IBom> boms) throws Exception {
        Map<String, IBom> notLeafBomMap = new HashMap<>();
        getBOMNotLeafNodeIds(boms.values().toArray(new IBom[boms.size()]), notLeafBomMap);

        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("*");
        sqlBuilder.addTable("bzgsjl");
        sqlBuilder.addWhere("bzgsjl_bommx_id", Operation.otIn,
                notLeafBomMap.keySet().toArray(new String[notLeafBomMap.size()]));
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("used", Operation.otEqual, new Object[]{1});
        IDataset dataset = db.query(sqlBuilder);
        if (dataset.getRowCount() == 0)
            return;

        for (IRow row : dataset.getRows()) {
            String bomid = (String) row.getValue("bzgsjl_bommx_id");
            String deviceid = (String) row.getValue("bzgsjl_sb_id");
            String mouldid = (String) row.getValue("bzgsjl_mj_id");
            BomInfo bom = (BomInfo) notLeafBomMap.get(bomid);
            bom.workhoursMap.put(bom.getWorkhoursKey(deviceid, mouldid), (float) row.getValue("bzgsjl_gs"));
        }
    }

    protected void buildStationAndDevice(String[] bomids, Map<String, IBom> boms) throws Exception {
        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("i.bommx_id,i.bommx_bom_id,k.gw_id,k.gw_code,k.gw_name,"
                + "m.sb_id,m.sb_name,m.sb_code,m.sb_xh,m.sb_status,m.sb_type,m.sb_cssm-m.sb_ysycs+m.sb_rbcs life");
        sqlBuilder.addTable(
                "bommx i left join gxgwgx j on i.gx_id = j.gxgwgx_gx_id left join gw k on j.gxgwgx_gw_id=k.gw_id left join gwsbgx l on k.gw_id=l.gwsbgx_gw_id left join sb m on l.gwsbgx_sb_id=m.sb_id");
        sqlBuilder.addWhere("bommx_bom_Id", Operation.otIn, bomids);
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.used", Operation.otEqual, new Object[]{1});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("k.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("k.used", Operation.otEqual, new Object[]{1});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("m.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("m.used", Operation.otEqual, new Object[]{1});
        IDataset dataset = db.query(sqlBuilder);

        if (dataset.getRowCount() == 0)
            return;

        for (IRow row : dataset.getRows()) {
            String stationId = row.getValue("gw_id") == null ? null : row.getValue("gw_id").toString();
            BomInfo bom = (BomInfo) boms.get(row.getValue("bommx_bom_id").toString());
            if (stationId != null && stationId.isEmpty()) {
                StationInfo stationInfo = new StationInfo();
                stationInfo.id = stationId;
                stationInfo.code = row.getValue("gx_code") == null ? null : row.getValue("gx_code").toString();
                stationInfo.name = row.getValue("gw_name") == null ? null : row.getValue("gw_name").toString();
                bom.stations.put(stationInfo.id, stationInfo);
            }

            DeviceInfo deviceInfo = DeviceBuilder.fillDeviceInfo(row);
            if (deviceInfo.id != null && deviceInfo.id.isEmpty())
                bom.devices.put(deviceInfo.id, deviceInfo);

        }
    }

    protected void getDeviceIds(IBom[] boms, Map<String, IDevice> devices) {
        if (boms == null || boms.length == 0)
            return;

        for (IBom bom : boms) {
            DeviceInfo[] deviceInfos = bom.getDevices();
            if (deviceInfos == null)
                continue;

            for (IDevice device : deviceInfos) {
                String id = device.getId();
                if (id == null || id.isEmpty())
                    continue;
                devices.put(id, device);
            }
            getDeviceIds(bom.getChilds(), devices);
        }
    }

    protected void buildBOMMoulds(Map<String, IBom> boms) throws Exception {
        Map<String, IDevice> devices = new HashMap<>();
        getDeviceIds(boms.values().toArray(new IBom[boms.size()]), devices);

        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("i.*,j.*");
        sqlBuilder.addTable("sbbdb i left join sb j on i.bd_bsb_id = j.sb_id");
        sqlBuilder.addWhere("bd_msb_id", Operation.otIn, devices.keySet().toArray(new String[devices.size()]));
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.used", Operation.otEqual, new Object[]{1});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("j.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("j.used", Operation.otEqual, new Object[]{1});
        IDataset dataset = db.query(sqlBuilder);
        if (dataset.getRowCount() == 0)
            return;

        for (IRow row : dataset.getRows()) {
            String deviceId = (String) row.getValue("bd_msb_id");
            MouldInfo info = DeviceBuilder.fillMouldInfo(row);
            DeviceInfo deviceInfo = (DeviceInfo) devices.get(deviceId);
            deviceInfo.moulds.put(info.id(), info);
        }
    }

    public void builder(String[] bomids, Map<String, IBom> boms) throws Exception {
        buildBOMBaseInfo(bomids, boms);//boms保存本次排程使用订单包含的产品对应的制造bom信息及节点的工序信息
        ParallelComputingExecutor<Integer> executor = new ParallelComputingExecutor<>(new Integer[]{0, 1}, 1);
        executor.execute(new ISimpleActionComputer<Integer>() {

            @Override
            public void compute(Integer t1) throws Exception {
                switch (t1) {
                    case 0:
                        buildStationAndDevice(bomids, boms);
                        buildBOMMoulds(boms);
                        break;
                    case 1:
                        buildBOMWorkhours(bomids, boms);
                        break;
                }
            }
        });
    }

    public static String[] getBomIds(String[] materialIds) throws Exception {

        IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
        ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
        sqlBuilder.addField("distinct bommx_bom_id");
        sqlBuilder.addTable("bom i left join bommx j on i.bom_root = j.bommx_id");
        sqlBuilder.addWhere("bommx_wl_id", Operation.otIn, materialIds);
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.deleted", Operation.otEqual, new Object[]{0});
        sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
        sqlBuilder.addWhere("i.used", Operation.otEqual, new Object[]{1});
        IDataset dataset = db.query(sqlBuilder);
        if (dataset.getRowCount() == 0)
            return null;

        String[] result = new String[dataset.getRowCount()];
        int index = 0;
        for (IRow row : dataset.getRows()) {
            result[index++] = (String) row.getValue("bommx_bom_id");
        }

        return result;
    }

}