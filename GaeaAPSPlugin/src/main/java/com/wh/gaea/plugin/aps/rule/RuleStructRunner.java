package com.wh.gaea.plugin.aps.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.plugin.aps.rule.connector.RuleConnector;

public class RuleStructRunner {
	RuleStructInfo structInfo;
	
	Map<RulePart, Map<RuleType, List<Rule>>> ruleMap = new ConcurrentHashMap<>();
	public RuleStructRunner(RuleStructInfo structInfo) {
		this.structInfo = structInfo;
	}

	protected <T extends RuleConnector> void prepareScope(Map<String, T> map) {
		for (T connector : map.values()) {
			Rule rule = structInfo.ruleMap.get(connector.getID());
			Map<RuleType, List<Rule>> partMap = ruleMap.get(rule.part);
			if (partMap == null) {
				partMap = new ConcurrentHashMap<>();
				ruleMap.put(rule.part, partMap);
			}
			
			List<Rule> scopeRules = partMap.get(rule.type);
			if (scopeRules == null) {
				scopeRules = Collections.synchronizedList(new ArrayList<>());
				partMap.put(rule.type, scopeRules);
			}
			
			scopeRules.add(rule);
		}

	}
	
	public void prepare() {
		prepareScope(structInfo.structConnector.globalConnectorMap);
		prepareScope(structInfo.structConnector.bomConnectorMap);
		prepareScope(structInfo.structConnector.bomnodeConnectorMap);
	}
	
	public List<Rule> getRules(RulePart part, RuleType type){
		Map<RuleType, List<Rule>> partMap = ruleMap.get(part);
		if (partMap == null)
			return new ArrayList<>();
		
		List<Rule> result = partMap.get(type);
		if (result == null)
			return new ArrayList<>();
		else {
			return result;
		}
	}
}
