package com.wh.gaea.plugin.aps.interfaces;

import java.util.List;

import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IScheduleTask;
import com.wh.gaea.plugin.aps.info.MetaSources;
import com.wh.gaea.plugin.aps.rule.OrderTarget;
import com.wh.gaea.plugin.aps.rule.OrderAllotType;
import com.wh.gaea.plugin.aps.rule.Rule;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;

public interface IEngine {
	
	/**
	 * 排程结果
	 * @author wy
	 *
	 */
	public static class ScheduleResult extends Result{
		public MetaSources metaSources;
		public ScheduleResult(MetaSources metaSources) {
			this.metaSources = metaSources;
		}
	}
	
	public static class SuccResult extends Result{
		public SuccResult() {
			ret = RET_SUCC;
		}
	}
	
	/**
	 * 排程结果
	 * @author wy
	 *
	 */
	public static class Result{
		
		public static final int RET_SUCC = 0;
		public static final int RET_FAIL = -1;
		public static final int RET_RESOURCE_NOTOPEN = -2;
		public static final int RET_RULE_INVALID = -3;
		public static final int RET_DATA_INVALID = -4;
		
		/**
		 * 排程结果状态，0成功，-1排程失败，-2资源无法打开，-3排程规则无效，-4数据无效
		 */
		public int ret = -1;
		/**
		 * 错误消息，可能为null
		 */
		public String errMsg;
		/**
		 * 错误对象，可能为null
		 */
		public Exception exception;
	}
	
	public static class NulledResult extends Result{
		public NulledResult() {
			exception = new NullPointerException();
		}
	}
	
	/**
	 * 规则检查结果
	 * @author wy
	 *
	 */
	public static class CheckRuleResult extends Result{
		/**
		 * 错误发生的规则编号
		 */
		public int lineNum = -1;
	}
		
	/**
	 * 检查排程架构所包含的规则是否合法
	 * @param structInfo 要检查的架构信息
	 * @return 检查结果信息
	 */
	Result checkStruct(RuleStructInfo structInfo);
	
	/**
	 * 检查规则是否合法
	 * @param Rule 要检查的规则信息
	 * @return 检查结果信息
	 */
	Result checkRule(Rule rule);

	/**
	 * 执行订单数据预装载，完成订单包含的单据信息、设备信息、物料信息等元数据的装载及清洗工作，此函数应尽量保证数据在后续工作中的高效使用。
	 * 此方法执行后应填充返回对象的元数据属性
	 * @param OrderTarget 排程的优化目标
	 * @param OrderAllotType 排程的分配规则
	 * @param structInfo 排程使用的规则架构
	 * @param orders 排程的任务单
	 * @param filter 排程的过滤器
	 * @return 排程结果信息
	 * @throws Throwable 
	 */
	MetaSources load(OrderTarget orderTarget, OrderAllotType orderType, RuleStructInfo structInfo, IOrder[] orders,
			IEngineFilter filter) throws Throwable;
	
	/**
	 * 将订单内的产品，按照产品bom进行分组，并按照交期排序
	 * 此方法执行后应填充metaSources对象的processTasks属性
	 * @param metaSources 元数据对象
	 * @param orders 排程的任务单
	 * @param filter 排程的过滤器
	 * @throws Exception
	 */
	void allotProduction(IOrder[] orders, MetaSources metaSources, IEngineFilter filter) throws Exception;
	
	/**
	 * 将订单内的产品，按照bom中的工序进行分组，并按照交期排序
	 * 此方法执行后应填充metaSources对象的processTasks属性
	 * @param metaSources 元数据对象
	 * @param filter 排程的过滤器
	 * @throws Exception
	 */
	void allotProcess(MetaSources metaSources, IEngineFilter filter) throws Exception, Throwable;
	
	/**
	 * 将allotProcess的任务按照分组规则分配到每组设备，此时仅确定分组关系，并不计算产能
	 * 此方法执行后应填充metaSources对象的processTasks属性的devices属性
	 * @param structInfo 排程使用的规则架构
	 * @param metaSources 元数据对象
	 * @param filter 排程的过滤器
	 * @throws Exception
	 */
	void allotMachine(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter) throws Exception, Throwable;
	
	/**
	 * 将allotMachine的任务按照分组规则重新排序
	 * @param structInfo 排程使用的规则架构
	 * @param metaSources 元数据对象
	 * @param filter 排程的过滤器
	 * @throws Exception
	 */
	void sort(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter) throws Exception;

	/**
	 * 将expandProcess的任务按照关联设备组、设备产能、工厂日历（满足交期的每日工作时间安排）进行任务单设置，并按照交期排序
	 * 此方法执行后应填充metaSources对象的processTasks属性的devices属性的productionCalendar属性
	 * @param structInfo 排程使用的规则架构
	 * @param metaSources 元数据对象
	 * @param filter 排程的过滤器
	 * @throws Exception
	 */
	void allotCapacity(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter) throws Exception;
	
	/**
	 * 将allotCapacity的任务按照关联设备组、设备产能、工厂日历（满足交期的每日工作时间安排）进行任务单设置，并按照交期排序
	 * 此方法执行后应按照metaSources对象的processTasks属性填充tasks属性
	 * @param structInfo 排程使用的规则架构
	 * @param metaSources 元数据对象
	 * @param filter 排程的过滤器
	 * @return 返回包含metaSources的ScheduleResult对象
	 * @throws Exception
	 */
	ScheduleResult publishTask(RuleStructInfo structInfo, MetaSources metaSources, IEngineFilter filter) throws Exception, Throwable;
	
	Result save(List<IScheduleTask> scheduleTasks, MetaSources metaSources);

}
