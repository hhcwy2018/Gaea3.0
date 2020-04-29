package com.wh.gaea.plugin.aps.interfaces;

import com.wh.gaea.plugin.aps.rule.*;
import org.json.JSONObject;

public interface IRule {

	/**
	 * 获取规则编号
	 * @return
	 */
	String getId();
	
	/**
	 * 获取规则名称
	 * @return
	 */
	String getName();
	
	/**
	 * 获取规则类别
	 * @return
	 */
	RuleType getRuleType();
	
	/**
	 * 获取规则作用域
	 * @return
	 */
	RuleScope getRuleScope();
	
	/**
	 * 获取规则应用阶段
	 * @return
	 */
	RulePart getRulePart();
	
	/**
	 * 获取规则级别，数字越小，级别越大
	 * @return
	 */
	int getLevel();
	
	/**
	 * 获取规则列表
	 * @return
	 */
	RuleExprs getRuleExprs();

	/**
	 * 获取规则表达式
	 * @return
	 */
	String getRuleExpr();

	/**
	 * 将规则对象保存为json对象
	 * @return
	 */
	JSONObject toJson();
	
	/**
	 * 用传入得json对象初始化规则对象
	 * @param data 包含规则对象信息得json
	 */
	void load(JSONObject data);

	<T> T compute(Rule.IComputeProc computeProc) throws Exception;
}
