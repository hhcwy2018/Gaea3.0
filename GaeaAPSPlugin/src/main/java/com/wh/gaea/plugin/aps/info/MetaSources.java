package com.wh.gaea.plugin.aps.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.info.MaterialRuntimeInfo;
import com.wh.gaea.industry.info.ProcessInfo;
import com.wh.gaea.industry.info.ProductionCalendar;
import com.wh.gaea.industry.interfaces.IBom;
import com.wh.gaea.industry.interfaces.ICustomer;
import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IScheduleTask;
import com.wh.gaea.industry.interfaces.IWorker;
import com.wh.gaea.plugin.aps.rule.OrderAllotType;
import com.wh.gaea.plugin.aps.rule.OrderTarget;

public class MetaSources {

    OrderTarget orderTarget = OrderTarget.otMinSwitchMould;
    OrderAllotType orderType = OrderAllotType.otAvg;

    public OrderTarget getOrderTarget() {
        return orderTarget;
    }

    public OrderAllotType getorOrderType() {
        return orderType;
    }

    public MetaSources(OrderAllotType orderType, OrderTarget orderTarget) {
        this.orderTarget = orderTarget;
        this.orderType = orderType;
    }

    /**
     * 工序信息以及工序与工位、设备的绑定信息，key为工序编码，value工序信息
     */
    public MetaSource<String, ProcessInfo> processes = new MetaSource<>();

    /**
     * 设备信息、key为设备编码，value设备信息
     */
    public MetaSource<String, DeviceInfo> devices = new MetaSource<>();

    /**
     * 设备标准工时信息、key为设备编码，value此设备的产品工时（key为物料编码，value为工时单位秒）
     */
    public MetaSource<String, Map<String, Float>> deviceStandardTimes = new MetaSource<>();

    /**
     * 所有需要用到的物料信息，key为物料编码，value值为物料的运行信息
     */
    public MetaSource<String, MaterialRuntimeInfo> materials = new MetaSource<>();

    /**
     * 所有需要用到的操作者信息，key为人员编码，value值为人员信息
     */
    public MetaSource<String, IWorker> workers = new MetaSource<>();

    /**
     * 所有需要用到的客户信息，key为客户编码，value值为客户信息
     */
    public MetaSource<String, ICustomer> customers = new MetaSource<>();

    /**
     * 所有需要用到的产品BOM信息，key为BOM编码，value值为产品BOM信息
     */
    public MetaSource<String, IBom> boms = new MetaSource<>();

    /**
     * 以物料编码为索引的产品BOM信息列表，key为物料编码，value值为产品BOM信息列表
     */
    public MetaSource<String, List<IBom>> productionBoms = new MetaSource<>();

    /**
     * 设备生产日历信息，key为设备id，value为此设备的生产时间安排
     */
    public MetaSource<String, ProductionCalendar> productionCalendars = new MetaSource<>();

    /**
     * 已经按照交期排序的订单列表
     */
    public List<IOrder> orders = Collections.synchronizedList(new ArrayList<>());

    /**
     * 所有工序的生产任务信息， key为ProcessTask的processBom域的工序id，value为工序生产任务信息
     */
    public MetaSource<String, WorkingInfo> processTasks = new MetaSource<>();

    /**
     * 所有设备的生产任务信息， key为DeviceTask的deviceInfo域的id，value为设备生产任务信息
     */
    public MetaSource<String, DeviceTask> deviceTasks = new MetaSource<>();

    /**
     * 排序后的任务列表
     */
    public List<IScheduleTask> tasks = Collections.synchronizedList(new ArrayList<>());

    /**
     * 根据输入的产品节点bom获取对应的工序任务对象，如果不存在则创建
     *
     * @param bom 产品节点bom
     * @return 已经存在或者新建立的工序任务对象
     */
    public WorkingInfo getProcessTask(IBom bom) {
        String processId = bom.getProcessId();
        if (processId == null || processId.isEmpty())
            return null;

        WorkingInfo workingInfo = processTasks.datas.get(processId);
        if (workingInfo == null) {
            synchronized (processTasks) {
                workingInfo = processTasks.datas.get(processId);
                if (workingInfo == null) {
                    workingInfo = new WorkingInfo(processId);
                    processTasks.datas.put(processId, workingInfo);
                }
            }
        }

        return workingInfo;
    }

    public DeviceTask getDeviceTask(String deviceId) {
        DeviceTask task = deviceTasks.datas.get(deviceId);
        if (task == null) {
            synchronized (deviceTasks) {
                task = deviceTasks.datas.get(deviceId);
                if (task == null) {
                    task = new DeviceTask(devices.datas.get(deviceId));

                    deviceTasks.datas.put(deviceId, task);
                }
            }
        }

        return task;
    }

}