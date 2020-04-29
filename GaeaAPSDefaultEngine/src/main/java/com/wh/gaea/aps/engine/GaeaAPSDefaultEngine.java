package com.wh.gaea.aps.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.wh.gaea.industry.builder.BOMBuilder;
import com.wh.gaea.industry.builder.CustomerBuilder;
import com.wh.gaea.industry.builder.DeviceBuilder;
import com.wh.gaea.industry.builder.DeviceWorkHoursBuilder;
import com.wh.gaea.industry.builder.MaterialRuntimeBuilder;
import com.wh.gaea.industry.builder.ProcessBuilder;
import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.info.MaterialRuntimeInfo;
import com.wh.gaea.industry.interfaces.IBom;
import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;
import com.wh.gaea.industry.interfaces.IScheduleTask;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.info.DeviceTask;
import com.wh.gaea.plugin.aps.info.MetaSources;
import com.wh.gaea.plugin.aps.info.WorkingInfo;
import com.wh.gaea.plugin.aps.interfaces.IEngine;
import com.wh.gaea.plugin.aps.interfaces.IEngineFilter;
import com.wh.gaea.plugin.aps.rule.OrderAllotType;
import com.wh.gaea.plugin.aps.rule.OrderTarget;
import com.wh.gaea.plugin.aps.rule.Rule;
import com.wh.gaea.plugin.aps.rule.RulePart;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;
import com.wh.gaea.plugin.aps.rule.RuleStructRunner;
import com.wh.gaea.plugin.aps.rule.RuleType;
import com.wh.gaea.plugin.aps.rule.connector.RuleTarget;
import com.wh.parallel.computing.execute.ParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;

public class GaeaAPSDefaultEngine implements IEngine {

    RuleStructRunner runner;

    @Override
    public Result checkStruct(RuleStructInfo structInfo) {
        Result result = new Result();
        if (structInfo == null)
            return new NulledResult();

        result.ret = Result.RET_SUCC;

        return result;
    }

    @Override
    public Result checkRule(Rule rule) {
        CheckRuleResult result = new CheckRuleResult();
        if (rule == null)
            return new NulledResult();

        result.ret = Result.RET_SUCC;

        return result;
    }

    @Override
    public MetaSources load(OrderTarget orderTarget, OrderAllotType orderType, RuleStructInfo structInfo,
                            IOrder[] orders, IEngineFilter filter) throws Throwable {
        runner = new RuleStructRunner(structInfo);

        MetaSources metaSources = new MetaSources(orderType, orderTarget);

        Map<String, String> materialIds = new HashMap<>();
        Map<String, String> customerIds = new HashMap<>();
        for (IOrder iOrder : orders) {
            customerIds.put(iOrder.customerId(), iOrder.customerId());
            for (IProduct product : iOrder.products()) {
                materialIds.put(product.materalId(), product.materalId());
            }
        }

        setProductionCalender(metaSources);
        setProcessRelation(metaSources);
        setMaterials(metaSources, materialIds.values());
        String[] bommxIds = materialIdToBomId(materialIds.values().toArray(new String[materialIds.size()]));
        setBoms(metaSources, bommxIds);
        String[] deviceIds = getDeviceIds(metaSources);
        setDevice(metaSources, deviceIds);
        setDeviceWorkHours(metaSources, deviceIds);
        setCustomers(metaSources, customerIds.keySet().toArray(new String[customerIds.size()]));
        setRules(structInfo, metaSources);

        if (filter != null) {
            filter.load(structInfo, metaSources);
        }
        return metaSources;
    }

    @Override
    public void allotProduction(IOrder[] orders, MetaSources metaSources, IEngineFilter filter) throws Exception {
        Map<Date, IOrder> timeOrders = new TreeMap<>();
        for (IOrder iOrder : orders) {
            timeOrders.put(iOrder.endTime(), iOrder);
        }

        metaSources.orders.addAll(timeOrders.values());

        if (filter != null)
            filter.orderProduction(metaSources);
    }

    protected <T> void parallelComputing(Collection<T> datas, ISimpleActionComputer<T> simpleActionComputer)
            throws InterruptedException, ExecutionException {
        parallelComputing(datas, simpleActionComputer, null);
    }

    protected <T> void parallelComputing(Collection<T> datas, ISimpleActionComputer<T> simpleActionComputer,
                                         Integer threshold) throws InterruptedException, ExecutionException {
        if (threshold == null)
            threshold = datas.size() > 8 ? datas.size() / 8 : datas.size() / 2;
        ParallelComputingExecutor<T> executor = new ParallelComputingExecutor<>(datas, threshold);
        executor.execute(simpleActionComputer);
    }

    protected void setProcess(IBom productionBom, IBom bom, MetaSources metaSources, IOrder order, IProduct product) {
        if (bom.getChilds() != null && bom.getChilds().length > 0) {
            WorkingInfo workingInfo = metaSources.getProcessTask(bom);
            if (workingInfo == null)
                return;

            workingInfo.addCounter(order, productionBom, bom, product);
            for (IBom child : bom.getChilds()) {
                setProcess(productionBom, child, metaSources, order, product);
            }
        }
    }

    protected void getDeviceIds(IBom bom, Map<String, String> deviceMap) {
        DeviceInfo[] deviceInfos = bom.getDevices();
        if (deviceInfos != null){
            for (DeviceInfo info: deviceInfos) {
                deviceMap.put(info.id, info.id);
            }
        }
        IBom[] childs = bom.getChilds();

        if (childs != null){
            for (IBom child: childs) {
                getDeviceIds(child, deviceMap);
            }
        }
    }

    protected String[] getDeviceIds(MetaSources metaSources) {
        Map<String, String> deviceMap = new HashMap<>();
        for (IBom bom : metaSources.boms.datas.values()) {
            getDeviceIds(bom, deviceMap);
        }

        return deviceMap.values().toArray(new String[deviceMap.size()]);
    }

    protected void setProcessRelation(MetaSources metaSources) throws Exception {
        metaSources.processes.datas.putAll(new ProcessBuilder().builder());
    }

    protected void setDevice(MetaSources metaSources, String[] deviceIds) throws Exception {
        metaSources.devices.datas.putAll(new DeviceBuilder().queryEquipmentMap());
    }

    protected void setDeviceWorkHours(MetaSources metaSources, String[] deviceIds) throws Exception {
        metaSources.deviceStandardTimes.datas.putAll(new DeviceWorkHoursBuilder().builder(deviceIds));
    }

    @Override
    public void allotProcess(MetaSources metaSources, IEngineFilter filter) throws Throwable {
        parallelComputing(metaSources.orders, new ISimpleActionComputer<IOrder>() {

            @Override
            public void compute(IOrder t1) throws Exception {
                for (IProduct product : t1.products()) {
                    List<IBom> boms = metaSources.productionBoms.datas.get(product.materalId());
                    for (IBom bom : boms) {
                        setProcess(bom, bom, metaSources, t1, product);
                    }
                }
            }

        });

        if (filter != null)
            filter.expandProcess(metaSources);

    }

    /**
     * 当前仅按照订单的交期计算设备排程，但实际则必须考虑每个工序的先后时间，需要计算生产一个产品的各个工序的交期，而不是订单总交期
     * 即将每个参与工序都设置上一个交期，以满足最后一个工序的交期小于等于订单交期
     *
     */
    protected void groupBaseMachine(List<Rule> rules, MetaSources metaSources,
                                                             WorkingInfo workingInfo) throws Throwable {

        switch (metaSources.getorOrderType()) {

            case otAvg:
                workingInfo.avgAllot(metaSources);
                break;
            case otGreed:
                workingInfo.greedAllot(metaSources);
                break;
        }

    }

    //未改完，目前未验证分组后的结果存放对象是否正确
    protected Map<String, List<DeviceInfo>> groupSwitchMinMachine(List<Rule> rules, MetaSources metaSources,
                                                                  WorkingInfo workingInfo) throws Throwable {
        Map<String, List<DeviceInfo>> groupDevices = new ConcurrentHashMap<>();
        for (DeviceInfo deviceInfo : metaSources.devices.datas.values()) {
            DeviceTask deviceTask = metaSources.getDeviceTask(deviceInfo.id);
            for (List<DeviceTask.DeviceOrderTask> tasks: deviceTask.getAllotInfo().tasks.values()) {
                for (DeviceTask.DeviceOrderTask task: tasks) {
                    MaterialRuntimeInfo info = metaSources.materials.datas.get(task.bom.getMaterialId());
                    for (Rule rule : rules) {
                        String group = rule.compute(new Rule.IComputeProc() {
                            @Override
                            public void getUserParameter(String key, String value) {

                            }

                            @Override
                            public Object getSystemParameter(RuleTarget target, String groupName, String attrName) {
                                switch (target) {
                                    case ftMaterial:
                                        if (!info.group.equalsIgnoreCase(groupName))
                                            return null;

                                        return info.paramMap.get(attrName);
                                    default:
                                        break;
                                }
                                return null;
                            }
                        });
                        if (group == null)
                            continue;
                        List<DeviceInfo> devices = groupDevices.get(group);
                        if (devices == null) {
                            devices = new ArrayList<>();
                            groupDevices.put(group, devices);
                        }

                        devices.addAll(Arrays.asList(task.bom.getDevices()));
                    }

                }
            }

        }

        return groupDevices;
    }

    /**
     * 使用规则分组设备，返回每个分组的设备id列表；此方法不会将任务分配到设备上，仅分组
     *
     * @param rules       规则列表
     * @param metaSources 元数据
     * @param workingInfo 要分组的工序任务
     * @return key为分组key，value为设备id列表
     * @throws Throwable
     */
    protected void groupMachine(List<Rule> rules, MetaSources metaSources,
                                                         WorkingInfo workingInfo) throws Throwable {
        groupBaseMachine(rules, metaSources, workingInfo);
        groupSwitchMinMachine(rules, metaSources, workingInfo);
    }

    @Override
    public void allotMachine(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter)
            throws Throwable {
        List<Rule> rules = runner.getRules(RulePart.rpAllotMachine, RuleType.rtGroup);
        parallelComputing(metaSources.processTasks.datas.values(), new ISimpleActionComputer<WorkingInfo>() {

            @Override
            public void compute(WorkingInfo workingInfo) throws Throwable {
                groupMachine(rules, metaSources, workingInfo);
            }

        }, 1);

        if (filter != null)
            filter.expandMachine(structInfo, metaSources);

    }

    @Override
    public void sort(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter) throws Exception {
        if (filter != null)
            filter.sort(structInfo, metaSources);
    }

    @Override
    public void allotCapacity(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter)
            throws Exception {
        if (filter != null)
            filter.allotCapacity(structInfo, metaSources);
    }

    @Override
    public ScheduleResult publishTask(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter)
            throws Exception, Throwable {
        ScheduleResult result = new ScheduleResult(metaSources);

        result.ret = ScheduleResult.RET_SUCC;

        if (filter != null)
            filter.publish(structInfo, metaSources);

        return result;
    }

    BOMBuilder bomBuilder = new BOMBuilder();

    protected void setCustomers(MetaSources metaSources, String[] customerIds) throws Exception {
        CustomerBuilder.builder(customerIds, metaSources.customers.datas);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setMaterials(MetaSources metaSources, Collection materialIds) throws Exception {
        Map<String, MaterialRuntimeInfo> infos = new MaterialRuntimeBuilder().builder(APSConfigure.getConfigDBName(), materialIds, true);
        metaSources.materials.datas.putAll(infos);
    }

    protected void setBoms(MetaSources metaSources, String[] bomids) throws Exception {
        bomBuilder.builder(bomids, metaSources.boms.datas);
        for (IBom bom : metaSources.boms.datas.values()) {
            List<IBom> boms = metaSources.productionBoms.datas.get(bom.getMaterialId());
            if (boms == null) {
                boms = Collections.synchronizedList(new ArrayList<>());
                metaSources.productionBoms.datas.put(bom.getMaterialId(), boms);
            }
            boms.add(bom);
        }
    }

    protected String[] materialIdToBomId(String[] materialIds) throws Exception {
        return BOMBuilder.getBomIds(materialIds);
    }

    protected void setProductionCalender(MetaSources metaSources) throws Exception {

    }

    protected void setRules(RuleStructInfo structInfo, MetaSources metaSources) throws Throwable {
        List<Rule> rules = structInfo.queryRules(null, null, null);

        List<Rule> globalRules = new ArrayList<>();
        Map<String, Rule> bomRules = new HashMap<>();
        Map<String, Rule> nodeRules = new HashMap<>();
        for (Rule rule : rules) {
            switch (rule.scope) {
                case rsBOM:
                    bomRules.put(rule.id, rule);
                    break;
                case rsGlobal:
                    globalRules.add(rule);
                    break;
                case rsNode:
                    nodeRules.put(rule.id, rule);
                    break;
            }
        }

    }

    @Override
    public Result save(List<IScheduleTask> scheduleTasks, MetaSources metaSources) {
        return null;
    }

    @Override
    public String toString() {
        return "系统排程引擎";
    }

}
