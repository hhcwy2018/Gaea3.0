package com.wh.gaea.plugin.aps.info;

import java.io.File;
import java.util.List;

import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IScheduleTask;
import com.wh.gaea.plugin.aps.engine.EngineQuery;
import com.wh.gaea.plugin.aps.interfaces.IEngine;
import com.wh.gaea.plugin.aps.interfaces.IEngineFilter;
import com.wh.gaea.plugin.aps.interfaces.IEngine.Result;
import com.wh.gaea.plugin.aps.interfaces.IEngine.ScheduleResult;
import com.wh.gaea.plugin.aps.rule.OrderAllotType;
import com.wh.gaea.plugin.aps.rule.OrderTarget;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;
import com.wh.tools.DynamicLoadJar;
import com.wh.tools.FileHelp;

public class EngineInfo {
	public String name;
	public File engineFile;
	protected IEngine engine;

	protected ScheduleResult lastResult;

	public EngineInfo() {
	}

	public EngineInfo(String name) {
		this.name = name;
		engineFile = new File(EngineQuery.APS_ENGINE_PATH, name + ".jar");
	}

	public EngineInfo(File file) {
		this.name = FileHelp.removeExt(file.getName());
		engineFile = file;
	}

	public String toString() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public IEngine getEngine() throws Exception {
		if (engine == null) {
			DynamicLoadJar.addClassLoaderFile(engineFile);
			engine = DynamicLoadJar.instance("com.wh.gaea.aps.engine." + name);
		}
		return engine;
	}

	public ScheduleResult schedule(OrderAllotType orderAllotType, OrderTarget orderTarget, RuleStructInfo structInfo,
			IOrder[] orders, IEngineFilter filter) throws Throwable {
		IEngine engine = getEngine();
		MetaSources metaSources = engine.load(orderTarget, orderAllotType, structInfo, orders, filter);
		engine.allotProduction(orders, metaSources, filter);
		engine.allotProcess(metaSources, filter);
		engine.allotMachine(structInfo, metaSources, filter);
		engine.sort(structInfo, metaSources, filter);
		engine.allotCapacity(structInfo, metaSources, filter);
		ScheduleResult result = engine.publishTask(structInfo, metaSources, filter);
		if (result == null)
			throw new Exception("排程执行失败，请检查输入数据及排程规则是否正确！");
		return result;
	}

	public Result save() throws Exception {
		if (lastResult == null) {
			throw new Exception("请先执行一次[预排程]/[排程]后再试！");
		}
		return save(lastResult.metaSources.tasks, lastResult.metaSources);
	}

	public Result save(List<IScheduleTask> scheduleTasks, MetaSources metaSources) throws Exception {
		IEngine engine = getEngine();
		return engine.save(scheduleTasks, metaSources);
	}

}