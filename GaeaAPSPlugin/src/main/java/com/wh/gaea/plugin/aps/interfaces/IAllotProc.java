package com.wh.gaea.plugin.aps.interfaces;

import com.wh.gaea.industry.interfaces.IBom;
import com.wh.gaea.industry.interfaces.IOrder;

public interface IAllotProc {
    enum AllotState {
        asEnd,//分配完毕
        asNoResource,//无分配资源
        asAllot,//已分配
    }

    class AllotResult {
        /**
         * 分配状态
         */
        public AllotState state = AllotState.asNoResource;

        /**
         * 实际分配的任务数量:0未分配
         */
        public long allotCount = 0;
    }

    /**
     * 用户实现此函数完成真实分配任务，并返回真实分配的任务数量
     *
     * @param order     分配的订单
     * @param bom       分配的加工节点bom
     * @param taskCount 可分配的任务数量
     * @return 分配结果
     */
    AllotResult allotProc(IOrder order, IBom bom, long taskCount);
}