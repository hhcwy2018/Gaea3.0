package com.wh.gaea.plugin.aps.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.wh.gaea.industry.interfaces.IBom;
import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;
import com.wh.gaea.plugin.aps.interfaces.IAllotProc;
import com.wh.tools.StringHelper;

public class WorkingInfo {

    /**
     * 每个订单的每个产品的每个工序一个对象
     *
     * @author hhcwy
     */
    public static class BomTask {
        public IOrder order;
        public IBom bom;
        public IBom productBom;
        public AtomicLong counter = new AtomicLong(0);

        public BomTask(IBom bom, IBom productBom, IOrder order) {
            this.bom = bom;
            this.order = order;
            this.productBom = productBom;
        }

        public String toKey() {
            return toKey(bom, order);
        }

        public static final String toKey(IBom bom, IOrder order) {
            return StringHelper.linkString(bom.getId(), order.orderId());
        }

        public void add(long count) {
            counter.addAndGet(count);
        }
    }

    /**
     * 工序id
     */
    public String id;

    public WorkingInfo(String processId) {
        this.id = processId;
    }

    /**
     * 此工序关联的半成品生产数量，key为BomTask.toKey()方法的返回值，value为任务对象
     */
    public Map<String, BomTask> processBomMap = new ConcurrentHashMap<>();

    /**
     * 此工序关联的半成品任务列表，按照订单的交期循序排序，key为包含此bom任务的订单交期，value为任务列表
     */
    public Map<Date, List<BomTask>> processBomCounter = new ConcurrentSkipListMap<>();

    /**
     * 工序的总任务数量
     */
    public AtomicLong counter = new AtomicLong(0);

    /**
     * 分配到此工序的每个订单的产品生产量
     */
    public Map<String, AtomicLong> orderCounter = new ConcurrentHashMap<>();

    /**
     * 获取此工序按照交期排序的任务对象列表，同一交期的不区分先后顺序
     *
     * @return
     */
    public List<BomTask> getCounter() {
        List<BomTask> tasks = new ArrayList<>();
        for (List<BomTask> bomTasks : processBomCounter.values()) {
            tasks.addAll(bomTasks);
        }
        return tasks;
    }

    public void addCounter(IOrder order, IBom productBom, IBom processBom, IProduct product) {
        long taskCount = (long) (product.count() * processBom.getCount());
        counter.addAndGet(taskCount);

        long productCount = product.count();

        AtomicLong counter;
        synchronized (this) {
            counter = orderCounter.get(order.orderId());
            if (counter == null) {
                counter = new AtomicLong(0);
                orderCounter.put(order.orderId(), counter);
            }
        }
        counter.addAndGet(productCount);

        BomTask bomTask;
        synchronized (this) {
            bomTask = processBomMap.get(BomTask.toKey(processBom, order));
            if (bomTask == null) {
                bomTask = new BomTask(processBom, productBom, order);
                processBomMap.put(bomTask.toKey(), bomTask);
                List<BomTask> bomTasks = processBomCounter.get(order.endTime());
                if (bomTasks == null) {
                    synchronized (processBomCounter) {
                        bomTasks = processBomCounter.get(order.endTime());
                        if (bomTasks == null) {
                            bomTasks = Collections.synchronizedList(new ArrayList<>());
                            processBomCounter.put(order.endTime(), bomTasks);
                        }
                    }
                }
                bomTasks.add(bomTask);
            }
        }
        bomTask.add(taskCount);

    }

    public void avgAllot(MetaSources metaSources) throws Exception {
        Collection<String> devices = metaSources.processes.datas.get(id).deviceMap.values();
        long avgCount = counter.get() / devices.size();
        long min = avgCount;

        Map<BomTask, Long> allotCounter = new HashMap<>();
        for (BomTask bomTask : processBomMap.values()) {
            if (bomTask.counter.get() < min) {
                min = bomTask.counter.get();
            }
            allotCounter.put(bomTask, bomTask.counter.get());
        }

        int index = 0;
        for (Map.Entry<BomTask, Long> entry : allotCounter.entrySet()) {
            BomTask bomTask = entry.getKey();
            long count = bomTask.counter.get();
            while (count > 0) {
                long counter = count / avgCount;
                if (counter < 2) {
                    if (count - avgCount >= avgCount / 2) {
                        counter = count / 2;
                    } else
                        counter = count;
                } else {
                    counter = avgCount;
                }

                avgAllotDevice(metaSources, bomTask, counter, index++);
                count -= counter;
            }

        }

    }

    /**
     * 按照bom任务进行分配
     *
     * @param metaSources
     */
    public void greedAllot(MetaSources metaSources) throws Exception {
        for (BomTask bomTask : processBomMap.values()) {

            long count = bomTask.counter.get();
            if (count <= 0)
                continue;

            greedAllotDevice(metaSources, bomTask, count);
        }
    }

    /**
     * 贪婪算法分配
     *
     * @param metaSources
     * @param bomTask
     * @param count
     * @throws Exception
     */
    protected void greedAllotDevice(MetaSources metaSources, BomTask bomTask, long count) throws Exception {
        IAllotProc.AllotResult result = null;
        while (count > 0) {

            deviceLoop:
            for (String deviceId :
                    metaSources.processes.datas.get(id).deviceMap.values()) {
                DeviceTask deviceTask = metaSources.getDeviceTask(deviceId);
                synchronized (bomTask.counter) {
                    result = deviceTask.decMaxTimes(metaSources, bomTask, count);
                    count -= result.allotCount;
                }

                switch (result.state) {
                    case asEnd:
                        deviceTask.add(bomTask.order, bomTask.bom, result.allotCount);
                        return;
                    case asAllot:
                        deviceTask.add(bomTask.order, bomTask.bom, result.allotCount);
                    case asNoResource:
                        break deviceLoop;
                }
            }

            if (result.state == IAllotProc.AllotState.asNoResource) {
                throw new Exception("设备无法满足订单【" + bomTask.order.orderId() + "】中产品【" + bomTask.bom.getName() +
                        "." + bomTask.bom.getCode() + "】交期安排！");
            }
        }
    }

    /**
     * 平均算法分配
     *
     * @param metaSources
     * @param bomTask
     * @param count
     * @throws Exception
     */
    protected void avgAllotDevice(MetaSources metaSources, BomTask bomTask, long count, int deviceIndex) throws Exception {
        IAllotProc.AllotResult result = null;

        int index = 0;

        Collection<String> deviceIds = metaSources.processes.datas.get(id).deviceMap.values();

        deviceIndex = deviceIndex % deviceIds.size();
        for (String deviceId : deviceIds) {

            if (index++ == deviceIndex) {
                DeviceTask deviceTask = metaSources.getDeviceTask(deviceId);
                synchronized (bomTask.counter) {
                    result = deviceTask.decMaxTimes(metaSources, bomTask, count);
                }

                //由于是平均分配，则要求设备每次必须能够添加所需任务
                switch (result.state) {
                    case asEnd:
                        deviceTask.add(bomTask.order, bomTask.bom, result.allotCount);
                        break;
                    case asAllot:
                    case asNoResource:
                        throw new Exception("设备无法满足订单【" + bomTask.order.orderId() + "】中产品【" + bomTask.bom.getName() +
                                "." + bomTask.bom.getCode() + "】交期安排！");
                }

                return;
            }
        }

    }

}