package com.wh.gaea.plugin.aps.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.info.ProductionCalendar;
import com.wh.gaea.industry.interfaces.IBom;
import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.plugin.aps.interfaces.IAllotProc;
import com.wh.tools.MapHelper;

public class DeviceTask {

    public static class DeviceOrderTask {
        public IOrder order;
        public IBom bom;
        public AtomicLong counter = new AtomicLong();

        public DeviceOrderTask(IOrder order, IBom bom) {
            this.order = order;
            this.bom = bom;
        }
    }

    public static class AllotInfo {
        /**
         * 起始分配日期
         */
        AtomicReference<Date> start = new AtomicReference<>();

        /**
         * 中止分配日期（含当前日期）
         */
        AtomicReference<Date> end = new AtomicReference<>();

        /**
         * 此任务的设备不同日期的最大工作时间，单位秒
         */
        Map<Date, AtomicLong> maxTimeMap = new ConcurrentHashMap<>();

        public Map<Date, List<DeviceOrderTask>> tasks = new ConcurrentSkipListMap<>();

        public Date next(Date endTime) {
            Date time = new Date(Math.min(endTime.getTime(), end.get().getTime()));
            synchronized (this){
                if (start == null)
                    return null;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start.get());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                start.set(calendar.getTime());
                if (start.get().getTime() > time.getTime()) {
                    return null;
                } else {
                    return start.get();
                }
            }
        }
    }

    /**
     * 关联的设备信息
     */
    DeviceInfo deviceInfo;

    AllotInfo allotInfo = new AllotInfo();

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public DeviceTask(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public AllotInfo getAllotInfo(){
        return allotInfo;
    }

    public void resetAllot() {
        allotInfo.start.getAndSet(deviceInfo.start);
        allotInfo.end.getAndSet(deviceInfo.end);
        allotInfo.maxTimeMap.clear();
    }

    /**
     * 分配到此设备上的任务数量
     */
    public AtomicLong counter = new AtomicLong(0);

    public void add(IOrder order, IBom bom, long count) {
        counter.addAndGet(count);
        DeviceOrderTask task = new DeviceOrderTask(order, bom);
        task.counter.set(count);

        List<DeviceOrderTask> tasks = MapHelper.getMapValue(allotInfo.tasks, order.endTime(), new MapHelper.ICreateMapValue<List<DeviceOrderTask>>() {
            @Override
            public List<DeviceOrderTask> instance() {
                return Collections.synchronizedList(new ArrayList<>());
            }
        });

        tasks.add(task);
    }

    /**
     * 将任务分配到设备上，在满足设备（当前起始时间，如多次执行日期会向后增加但不会超出设备的终止时间）与（BomTask交期或设备）
     * 使用区间较小的区间内
     * @param metaSources 资源
     * @param bomTask 当前要分配的任务对象
     * @param value 分配此对象的任务数量
     * @return 分配结果
     */
    public IAllotProc.AllotResult decMaxTimes(MetaSources metaSources, WorkingInfo.BomTask bomTask, long value) {
        IAllotProc.AllotResult result = decMaxTimes(metaSources, allotInfo.start.get(), value);
        while (result.state == IAllotProc.AllotState.asNoResource) {
            if (allotInfo.next(bomTask.order.endTime()) == null){
            	return result;
			}
			result = decMaxTimes(metaSources, allotInfo.start.get(), value);
        }

        return result;
    }

    /**
     * 将任务按照time指定的日期分配到设备上
     * @param metaSources 资源
     * @param time 设备的运行日期，yyyy-MM-dd
     * @param value 任务数量
     * @return 分配结果
     */
    public IAllotProc.AllotResult decMaxTimes(MetaSources metaSources, Date time, long value) {
        IAllotProc.AllotResult result = new IAllotProc.AllotResult();
        AtomicLong maxTimes = getMaxTimes(metaSources, time);
        long times = 0;
        synchronized (maxTimes){
            times = maxTimes.get();
            if (times <= 0) {//当前设备没有空闲工作时间
                return result;
            }

            times -= value;
            maxTimes.set(times);
        }


        if (times >= 0) {//当前设备的剩余工作时间大于等于要分配的时间，已完成此次分配
            result.state = IAllotProc.AllotState.asEnd;
            result.allotCount = value;
        } else {//当前设备的剩余工作时间小于要分配的时间，仅分配此次分配数量的一部分
            result.state = IAllotProc.AllotState.asAllot;
            result.allotCount = value + times;
        }

        return result;
    }

    public AtomicLong getMaxTimes(MetaSources metaSources, Date time) {
        AtomicLong maxTimes = MapHelper.getMapValue(allotInfo.maxTimeMap, time, new MapHelper.ICreateMapValue<AtomicLong>() {
            @Override
            public AtomicLong instance() {
                ProductionCalendar productionCalendar = metaSources.productionCalendars.datas.get(deviceInfo.id);
                long maxTimes = productionCalendar.getWorkTime(time) / 1000;
                return new AtomicLong(maxTimes);
            }
        });

        return maxTimes;
    }
}